package org.qortal.repository.hsqldb.transaction;

import org.qortal.data.transaction.BaseTransactionData;
import org.qortal.data.transaction.SellNameTransactionData;
import org.qortal.data.transaction.TransactionData;
import org.qortal.repository.DataException;
import org.qortal.repository.hsqldb.HSQLDBRepository;
import org.qortal.repository.hsqldb.HSQLDBSaver;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HSQLDBSellNameTransactionRepository extends HSQLDBTransactionRepository {

	public HSQLDBSellNameTransactionRepository(HSQLDBRepository repository) {
		this.repository = repository;
	}

	TransactionData fromBase(BaseTransactionData baseTransactionData) throws DataException {
		String sql = "SELECT name, amount, is_private_sale, sale_recipient FROM SellNameTransactions WHERE signature = ?";

		try (ResultSet resultSet = this.repository.checkedExecute(sql, baseTransactionData.getSignature())) {
			if (resultSet == null)
				return null;

			String name = resultSet.getString(1);
			long amount = resultSet.getLong(2);
			boolean isPrivateSale = resultSet.getBoolean(3);
			String saleRecipient = resultSet.getString(4);
			if (!isPrivateSale)
				saleRecipient = null;

			return new SellNameTransactionData(baseTransactionData, name, amount, isPrivateSale, saleRecipient);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch sell name transaction from repository", e);
		}
	}

	@Override
	public void save(TransactionData transactionData) throws DataException {
		SellNameTransactionData sellNameTransactionData = (SellNameTransactionData) transactionData;

		HSQLDBSaver saveHelper = new HSQLDBSaver("SellNameTransactions");

		saveHelper.bind("signature", sellNameTransactionData.getSignature()).bind("owner", sellNameTransactionData.getOwnerPublicKey())
				.bind("name", sellNameTransactionData.getName()).bind("amount", sellNameTransactionData.getAmount())
				.bind("is_private_sale", sellNameTransactionData.getIsPrivateSale()).bind("sale_recipient", sellNameTransactionData.getSaleRecipient());

		try {
			saveHelper.execute(this.repository);
		} catch (SQLException e) {
			throw new DataException("Unable to save sell name transaction into repository", e);
		}
	}

}
