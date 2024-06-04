package io.tofpu.migratio.database;

import io.tofpu.migratio.Migratio;
import io.tofpu.migratio.database.adapter.DatabaseVersionAdapter;
import io.tofpu.migratio.database.adapter.DefaultDatabaseVersionAdapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MigratioDatabase {
    static Builder newBuilder(String packageName) {
        return new Builder(packageName);
    }

    class Builder extends Migratio.Builder<Builder, DatabaseMigration> {
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

        static class ReuseableConnectionProvider implements ConnectionProvider {
            private final ConnectionProvider connectionProvider;
            private Connection connection;

            ReuseableConnectionProvider(ConnectionProvider connectionProvider) {
                this.connectionProvider = connectionProvider;
            }

            @Override
            public Connection get() {
                if (isNotAvailable()) {
                    connection = connectionProvider.get();
                }
                return connection;
            }

            private boolean isNotAvailable() {
                try {
                    return connection == null || connection.isClosed();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            private boolean isAvailable() {
                return !isNotAvailable();
            }

            public void ifPresent(ThrowableConsumer<Connection> connectionConsumer) {
                if (isAvailable()) {
                    connectionConsumer.accept(connection);
                }
            }
        }

        interface ThrowableConsumer<T> extends Consumer<T> {
            @Override
            default void accept(T value) {
                try {
                    acceptThrows(value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            void acceptThrows(T value) throws Exception;
        }

        public void migrate() {
            final ReuseableConnectionProvider connectionProvider = new ReuseableConnectionProvider(this.connectionProvider);
            try {
                DatabaseVersionAdapter versionAdapter = new DefaultDatabaseVersionAdapter(connectionProvider::get);
                String currentVersion = versionAdapter.readCurrentVersion();

                migrate(currentVersion, databaseMigration -> {
                    databaseMigration.handle(new DatabaseMigrationContext(connectionProvider.get()));

                    versionAdapter.write(databaseMigration);
                });
            } finally {
                connectionProvider.ifPresent(Connection::close);
            }
        }

        @Override
        public String getCurrentVersion() {
            try {
                try (Connection connection = connectionProvider.get()) {
                    DatabaseVersionAdapter versionAdapter = new DefaultDatabaseVersionAdapter(() -> connection);
                    return versionAdapter.readCurrentVersion();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
            return runAsync(delegate::migrate);
        }

        @Override
        public CompletableFuture<String> getCurrentVersion() {
            return supplyAsync(delegate::getCurrentVersion);
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
