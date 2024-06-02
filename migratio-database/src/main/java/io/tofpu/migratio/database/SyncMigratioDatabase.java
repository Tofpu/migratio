package io.tofpu.migratio.database;

import java.sql.SQLException;

public interface SyncMigratioDatabase {
    void migrate() throws SQLException;
}
