package io.tofpu.migratio.database;

import io.tofpu.migratio.MigrationContext;

import java.sql.Connection;

public class DatabaseMigrationContext extends MigrationContext {
    private final Connection connection;

    public DatabaseMigrationContext(Connection connection) {
        this.connection = connection;
    }

    public Connection connection() {
        return connection;
    }
}
