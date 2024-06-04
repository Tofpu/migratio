package io.tofpu.migratio.database;

public interface SyncMigratioDatabase {
    void migrate();
    String getCurrentVersion();
}
