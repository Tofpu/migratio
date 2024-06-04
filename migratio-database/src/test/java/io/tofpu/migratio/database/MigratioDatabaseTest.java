package io.tofpu.migratio.database;

import io.tofpu.migratio.annotation.MigrationExclude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class MigratioDatabaseTest {

    File directory = new File("test-results");

    @BeforeEach
    void setUp() throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                Files.delete(file.toPath());
            }
        }
        Files.deleteIfExists(directory.toPath());

        directory.mkdir();
    }

    @Test
    void sync_migrate_test() {
        ConnectionProvider connectionProvider = ConnectionProvider.accept("jdbc:sqlite:test-results/temp.db");
        SyncMigratioDatabase migratioDatabase = MigratioDatabase.newBuilder("io.tofpu.migratio")
                .buildSync(connectionProvider);

        migratioDatabase.migrate();

        try (Connection connection = connectionProvider.get()) {
            assertTrue(connection.createStatement().execute("SELECT name FROM persons WHERE name = 'Tofpu' AND age = '99'"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        assertEquals("1.1", migratioDatabase.getCurrentVersion());
    }

    @Test
    void async_migrate_test() throws ExecutionException, InterruptedException {
        ConnectionProvider connectionProvider = ConnectionProvider.accept("jdbc:sqlite:test-results/temp.db");
        AsyncMigratioDatabase migratioDatabase = MigratioDatabase.newBuilder("io.tofpu.migratio")
                .buildAsync(connectionProvider);

        try {
            migratioDatabase.migrate().get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = connectionProvider.get()) {
            assertTrue(connection.createStatement().execute("SELECT name FROM persons WHERE name = 'Tofpu' AND age = '99'"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        assertEquals("1.1", migratioDatabase.getCurrentVersion().get());
    }

    @Test
    void connection_reopen_test() {
        SimpleDatabaseMigration closeDbMigration = createDatabaseMigration("1.0", (context) -> {
            try {
                System.out.println("1.0 called");
                context.connection().close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        AtomicBoolean wasConnectionReopened = new AtomicBoolean(false);
        SimpleDatabaseMigration verifyConnectionIsOpenMigration = createDatabaseMigration("1.1", (context) -> {
            try {
                System.out.println("1.1 called");
                boolean open = context.connection().createStatement().execute("SELECT 1");
                wasConnectionReopened.set(open);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        ConnectionProvider connectionProvider = ConnectionProvider.accept("jdbc:sqlite:test-results/temp.db");
        SyncMigratioDatabase migratioDatabase = MigratioDatabase.newBuilder("io.tofpu.migratio")
                .setClassesSupplier(() -> new Class[0]) // we don't want to obtain other migration classes
                .setMigrationSupplier(() -> Arrays.asList(closeDbMigration, verifyConnectionIsOpenMigration).toArray(new DatabaseMigration[0]))
                .buildSync(connectionProvider);

        migratioDatabase.migrate();

        assertTrue(wasConnectionReopened.get());
    }

    private SimpleDatabaseMigration createDatabaseMigration(String version, Consumer<DatabaseMigrationContext> migrationContextConsumer) {
        return new SimpleDatabaseMigration(version, migrationContextConsumer);
    }

    @MigrationExclude
    public static class SimpleDatabaseMigration implements DatabaseMigration {
        private final String version;
        private final Consumer<DatabaseMigrationContext> migrationContextConsumer;

        public SimpleDatabaseMigration(String version, Consumer<DatabaseMigrationContext> migrationContextConsumer) {
            this.version = version;
            this.migrationContextConsumer = migrationContextConsumer;
        }

        @Override
        public String version() {
            return version;
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public void handle(DatabaseMigrationContext context) {
            migrationContextConsumer.accept(context);
        }
    }
}
