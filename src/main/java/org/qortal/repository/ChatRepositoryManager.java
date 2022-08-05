package org.qortal.repository;

import java.util.concurrent.TimeoutException;

// TODO: extend RepositoryManager, but only after moving away from static methods
public class ChatRepositoryManager {

    private static RepositoryFactory repositoryFactory = null;

    /** null if no checkpoint requested, TRUE for quick checkpoint, false for slow/full checkpoint. */
    private static Boolean quickCheckpointRequested = null;

    public static RepositoryFactory getRepositoryFactory() {
        return repositoryFactory;
    }

    public static void setRepositoryFactory(RepositoryFactory newRepositoryFactory) {
        repositoryFactory = newRepositoryFactory;
    }

    public static boolean wasPristineAtOpen() throws DataException {
        if (repositoryFactory == null)
            throw new DataException("No chat repository available");

        return repositoryFactory.wasPristineAtOpen();
    }

    public static Repository getRepository() throws DataException {
        if (repositoryFactory == null)
            throw new DataException("No chat repository available");

        return repositoryFactory.getRepository();
    }

    public static Repository tryRepository() throws DataException {
        if (repositoryFactory == null)
            throw new DataException("No chat repository available");

        return repositoryFactory.tryRepository();
    }

    public static void closeRepositoryFactory() throws DataException {
        repositoryFactory.close();
        repositoryFactory = null;
    }

    public static void backup(boolean quick, String name, Long timeout) throws TimeoutException {
        // Backups currently unsupported for chat repository
    }

    public static boolean archive(Repository repository) {
        // Archiving not supported
        return false;
    }

    public static boolean prune(Repository repository) {
        // Pruning not supported
        return false;
    }

    public static void setRequestedCheckpoint(Boolean quick) {
        quickCheckpointRequested = quick;
    }

    public static Boolean getRequestedCheckpoint() {
        return quickCheckpointRequested;
    }

}
