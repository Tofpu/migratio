package io.tofpu.migratio.database;

import java.sql.SQLException;

public interface SyncMigratioDatabase {
    void migrate();
    String getCurrentVersion() throws SQLException;
}
