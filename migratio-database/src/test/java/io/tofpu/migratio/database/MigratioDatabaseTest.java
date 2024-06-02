package io.tofpu.migratio.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void migrate_test() {
        Supplier<Connection> connectionSupplier = () -> {
            try {
                return DriverManager.getConnection("jdbc:sqlite:test-results/temp.db");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        MigratioDatabase migratioDatabase = MigratioDatabase.newBuilder("io.tofpu.migratio")
                .build(connectionSupplier);

        try {
            migratioDatabase.migrate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = connectionSupplier.get()) {
            assertTrue(connection.createStatement().execute("SELECT name FROM persons WHERE name = 'Tofpu' AND age = '99'"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
