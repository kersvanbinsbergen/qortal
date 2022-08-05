package org.qortal.repository.hsqldb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qortal.controller.Controller;
import org.qortal.gui.SplashFrame;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


// TODO: extend HSQLDBDatabaseUpdates, but only after moving away from static methods
public class HSQLDBChatDatabaseUpdates {

	private static final Logger LOGGER = LogManager.getLogger(HSQLDBChatDatabaseUpdates.class);

	/**
	 * Apply any incremental changes to database schema.
	 *
	 * @return true if database was non-existent/empty, false otherwise
	 * @throws SQLException
	 */
	public static boolean updateDatabase(Connection connection) throws SQLException {
		final boolean wasPristine = fetchDatabaseVersion(connection) == 0;

		SplashFrame.getInstance().updateStatus("Upgrading chat database, please wait...");

		while (databaseUpdating(connection, wasPristine))
			incrementDatabaseVersion(connection);

		String text = String.format("Starting Qortal Core v%s...", Controller.getInstance().getVersionStringWithoutPrefix());
		SplashFrame.getInstance().updateStatus(text);

		return wasPristine;
	}

	/**
	 * Increment database's schema version.
	 *
	 * @throws SQLException
	 */
	private static void incrementDatabaseVersion(Connection connection) throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("UPDATE DatabaseInfo SET version = version + 1");
			connection.commit();
		}
	}

	/**
	 * Fetch current version of database schema.
	 *
	 * @return database version, or 0 if no schema yet
	 * @throws SQLException
	 */
	protected static int fetchDatabaseVersion(Connection connection) throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			if (stmt.execute("SELECT version FROM DatabaseInfo"))
				try (ResultSet resultSet = stmt.getResultSet()) {
					if (resultSet.next())
						return resultSet.getInt(1);
				}
		} catch (SQLException e) {
			// empty database
		}

		return 0;
	}

	/**
	 * Incrementally update database schema, returning whether an update happened.
	 * 
	 * @return true - if a schema update happened, false otherwise
	 * @throws SQLException
	 */
	private static boolean databaseUpdating(Connection connection, boolean wasPristine) throws SQLException {
		int databaseVersion = fetchDatabaseVersion(connection);

		try (Statement stmt = connection.createStatement()) {

			switch (databaseVersion) {
				case 0:
					// create from new
					// FYI: "UCC" in HSQLDB means "upper-case comparison", i.e. case-insensitive
					stmt.execute("SET DATABASE SQL NAMES TRUE"); // SQL keywords cannot be used as DB object names, e.g. table names
					stmt.execute("SET DATABASE SQL SYNTAX MYS TRUE"); // Required for our use of INSERT ... ON DUPLICATE KEY UPDATE ... syntax
					stmt.execute("SET DATABASE SQL RESTRICT EXEC TRUE"); // No multiple-statement execute() or DDL/DML executeQuery()
					stmt.execute("SET DATABASE TRANSACTION CONTROL MVCC"); // Use MVCC over default two-phase locking, a-k-a "LOCKS"
					stmt.execute("SET DATABASE DEFAULT TABLE TYPE CACHED");
					stmt.execute("SET DATABASE COLLATION SQL_TEXT NO PAD"); // Do not pad strings to same length before comparison

					stmt.execute("CREATE COLLATION SQL_TEXT_UCC_NO_PAD FOR SQL_TEXT FROM SQL_TEXT_UCC NO PAD");
					stmt.execute("CREATE COLLATION SQL_TEXT_NO_PAD FOR SQL_TEXT FROM SQL_TEXT NO PAD");

					stmt.execute("SET FILES SPACE TRUE"); // Enable per-table block space within .data file, useful for CACHED table types
					// Slow down log fsync() calls from every 500ms to reduce I/O load
					stmt.execute("SET FILES WRITE DELAY 5"); // only fsync() every 5 seconds

					stmt.execute("CREATE TABLE DatabaseInfo ( version INTEGER NOT NULL )");
					stmt.execute("INSERT INTO DatabaseInfo VALUES ( 0 )");

					stmt.execute("CREATE TYPE Signature AS VARBINARY(64)");
					stmt.execute("CREATE TYPE QortalAddress AS VARCHAR(36)");
					stmt.execute("CREATE TYPE MessageData AS VARBINARY(4000)");

					break;

				case 1:
					// Chat messages
					stmt.execute("CREATE TABLE ChatMessages (signature Signature, reference Signature,"
							+ "created_when EpochMillis NOT NULL, tx_group_id GroupID NOT NULL, "
							+ "sender QortalAddress NOT NULL, nonce INT NOT NULL, recipient QortalAddress, "
							+ "is_text BOOLEAN NOT NULL, is_encrypted BOOLEAN NOT NULL, data MessageData NOT NULL, "
							+ "PRIMARY KEY (signature))");
					// For finding chat messages by sender
					stmt.execute("CREATE INDEX ChatMessagesSenderIndex ON ChatMessages (sender)");
					// For finding chat messages by recipient
					stmt.execute("CREATE INDEX ChatMessagesRecipientIndex ON ChatMessages (recipient, sender)");
					break;

				default:
					// nothing to do
					return false;
			}
		}

		// database was updated
		LOGGER.info(() -> String.format("HSQLDB chat repository updated to version %d", databaseVersion + 1));
		return true;
	}

}
