package com.demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Secure data access repository demonstrating parameterized SQL queries.
 */
public class SecureUserRepository {

    private final Connection connection;

    public SecureUserRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean validateUser(String username, String passwordHash) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
