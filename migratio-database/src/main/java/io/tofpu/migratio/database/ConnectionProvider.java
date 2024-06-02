package io.tofpu.migratio.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface ConnectionProvider {

    static ConnectionProvider accept(Connection connection) {
        return () -> connection;
    }

    static ConnectionProvider accept(String jdbcUrl) {
        try {
            return accept(DriverManager.getConnection(jdbcUrl));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Connection get();
}
