package io.tofpu.migratio.database;

import io.tofpu.migratio.Migratio;
import io.tofpu.migratio.database.adapter.DatabaseVersionAdapter;
import io.tofpu.migratio.database.adapter.DefaultDatabaseVersionAdapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public interface MigratioDatabase {
    static Builder newBuilder(String packageName) {
        return new Builder(packageName);
    }

    class Builder extends Migratio.Builder<Builder> {
        protected Builder(String packageName) {
            super(packageName);
        }

        public SyncMigratioDatabase buildSync(ConnectionProvider connectionProvider) {
            return new SyncMigratioDatabaseImpl(findAndSortMigrations(DatabaseMigration.class), connectionProvider);
        }

        public AsyncMigratioDatabase buildAsync(ConnectionProvider connectionProvider, Executor executor) {
            return new AsyncMigratioDatabaseImpl(buildSync(connectionProvider), executor);
        }

        public AsyncMigratioDatabase buildAsync(ConnectionProvider connectionProvider) {
            return buildAsync(connectionProvider, null);
        }
    }

    class BaseMigratioDatabase extends Migratio<DatabaseMigration> {
        protected BaseMigratioDatabase(Collection<DatabaseMigration> migrations) {
            super(migrations);
        }
    }

    class SyncMigratioDatabaseImpl extends BaseMigratioDatabase implements SyncMigratioDatabase {
        private final ConnectionProvider connectionProvider;

        SyncMigratioDatabaseImpl(Collection<DatabaseMigration> migrations, ConnectionProvider connectionProvider) {
            super(migrations);
            this.connectionProvider = connectionProvider;
        }

        public void migrate() throws SQLException {
            final AtomicReference<Connection> connectionRef = new AtomicReference<>(connectionProvider.get());
            try {
                DatabaseVersionAdapter versionAdapter = new DefaultDatabaseVersionAdapter(connectionRef::get);
                String currentVersion = versionAdapter.readCurrentVersion();

                migrate(currentVersion, databaseMigration -> {
                    getNewConnectionIfClosed(connectionRef); // in-case the consumer accidentally closes the connection
                    databaseMigration.handle(new DatabaseMigrationContext(connectionRef.get()));
                    getNewConnectionIfClosed(connectionRef); // in-case the consumer accidentally closes the connection

                    versionAdapter.write(databaseMigration);
                });
            } finally {
                connectionRef.get().close();
            }
        }

        private void getNewConnectionIfClosed(AtomicReference<Connection> connection) {
            try {
                if (connection.get().isClosed()) {
                    connection.set(connectionProvider.get());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getCurrentVersion() throws SQLException {
            try (Connection connection = connectionProvider.get()) {
                DatabaseVersionAdapter versionAdapter = new DefaultDatabaseVersionAdapter(() -> connection);
                return versionAdapter.readCurrentVersion();
            }
        }
    }

    class AsyncMigratioDatabaseImpl implements AsyncMigratioDatabase {
        private final SyncMigratioDatabase delegate;
        private final Executor executor;

        public AsyncMigratioDatabaseImpl(SyncMigratioDatabase delegate, Executor executor) {
            this.delegate = delegate;
            this.executor = executor;
        }

        @Override
        public CompletableFuture<?> migrate() {
            return runAsync(() -> {
                try {
                    delegate.migrate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public CompletableFuture<String> getCurrentVersion() {
            return supplyAsync(() -> {
                try {
                    return delegate.getCurrentVersion();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private CompletableFuture<Void> runAsync(Runnable runnable) {
            if (executor == null) {
                return CompletableFuture.runAsync(runnable);
            }
            return CompletableFuture.runAsync(runnable, executor);
        }

        private <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
            if (executor == null) {
                return CompletableFuture.supplyAsync(supplier);
            }
            return CompletableFuture.supplyAsync(supplier, executor);
        }
    }
}
