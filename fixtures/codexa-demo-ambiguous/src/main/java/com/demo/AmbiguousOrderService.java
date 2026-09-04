package com.demo;

import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class AmbiguousOrderService {

    public List<String> searchOrders(Connection conn, String filter, String status) throws SQLException {
        // AMBIGUOUS: Custom sanitizer used instead of standard PreparedStatement parameterized queries.
        // Static analysis flags dynamic SQL construction, but manual review is required to verify if custom sanitization is safe.
        String sanitizedFilter = customSanitize(filter);
        String sql = "SELECT order_id, total FROM orders WHERE status = '" + status + "' AND description LIKE '%" + sanitizedFilter + "%'";
        
        List<String> results = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(rs.getString("order_id") + ": " + rs.getDouble("total"));
            }
        }
        return results;
    }

    private String customSanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "''").replace(";", "");
    }
}
