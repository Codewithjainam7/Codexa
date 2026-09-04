package com.demo;

import org.springframework.web.bind.annotation.*;
import java.sql.Connection;
import java.sql.Statement;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/search")
    public String searchUser(Connection conn, @RequestParam String username) throws Exception {
        Statement stmt = conn.createStatement();
        // VULNERABLE: CR-SQL-001 (SQL Injection)
        stmt.executeQuery("SELECT * FROM users WHERE username = '" + username + "'");
        return "User query executed";
    }

    @PostMapping("/runDiagnostic")
    public String runDiagnostic(@RequestParam String host) throws Exception {
        // VULNERABLE: CR-CMD-001 (Command Injection)
        Runtime.getRuntime().exec("ping -c 4 " + host);
        return "Ping executed";
    }
}
