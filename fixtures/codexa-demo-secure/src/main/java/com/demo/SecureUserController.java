package com.demo;

import org.springframework.web.bind.annotation.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
@RequestMapping("/api/users")
public class SecureUserController {

    @GetMapping("/search")
    public String searchUser(Connection conn, @RequestParam String username) throws Exception {
        // SECURE: Parameterized SQL query
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return "Secure user query executed";
            }
        }
    }
}
