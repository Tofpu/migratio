package io.tofpu.migratio.database;

import io.tofpu.migratio.Migratio;
import io.tofpu.migratio.database.adapter.DatabaseVersionAdapter;
import io.tofpu.migratio.database.adapter.DefaultDatabaseVersionAdapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

// todo: add support for running this asynchronously
public class MigratioDatabase extends Migratio<DatabaseMigration> {
    private final ConnectionProvider connectionProvider;

    MigratioDatabase(Collection<DatabaseMigration> migrations, ConnectionProvider connectionProvider) {
        super(migrations);
        this.connectionProvider = connectionProvider;
    }

    public static Builder newBuilder(String packageName) {
        return new Builder(packageName);
    }

    public void migrate() throws SQLException {
        try (Connection connection = connectionProvider.get()) {
            DatabaseMigrationContext context = new DatabaseMigrationContext(connection);

            DatabaseVersionAdapter versionAdapter = new DefaultDatabaseVersionAdapter(connection);
            String currentVersion = versionAdapter.readCurrentVersion();

            migrate(currentVersion, databaseMigration -> {
                databaseMigration.handle(context);
                versionAdapter.write(databaseMigration);
            });
        }
    }

    public static class Builder extends Migratio.Builder<Builder> {
        protected Builder(String packageName) {
            super(packageName);
        }

        public MigratioDatabase build(ConnectionProvider connectionProvider) {
            return new MigratioDatabase(findAndSortMigrations(DatabaseMigration.class), connectionProvider);
        }
    }
}
