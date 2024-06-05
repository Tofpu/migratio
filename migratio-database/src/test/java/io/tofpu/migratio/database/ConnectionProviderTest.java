package io.tofpu.migratio.database;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConnectionProviderTest {
    @Test
    void jdbc_url_test() {
        ConnectionProvider connectionProvider = ConnectionProvider.accept("jdbc:sqlite::memory:");
        try (Connection connection = connectionProvider.get()) {
            assertNotNull(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
