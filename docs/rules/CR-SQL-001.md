# Rule: CR-SQL-001 — SQL Injection Vulnerability

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-SQL-001` |
| **Category** | `SECURITY` |
| **Severity** | `CRITICAL` |
| **Confidence** | `HIGH` |
| **CWE** | [CWE-89: SQL Injection](https://cwe.mitre.org/data/definitions/89.html) |
| **OWASP Top 10** | [A03:2021 - Injection](https://owasp.org/Top10/2021/A03_2021-Injection/) |

---

## 1. Description

Detects SQL statements constructed through unescaped string concatenation or interpolation containing untrusted input variables. When user-controlled data is spliced directly into SQL commands, an attacker can alter SQL query semantics, bypassing authentication, reading arbitrary database contents, or executing administrative database commands.

---

## 2. Detection Patterns & Taint Sinks

The rule inspects both Java AST nodes (`BinaryExpr.Operator.PLUS` inside JDBC calls) and multi-language regex tokenizers:

- `Statement.executeQuery("SELECT ... " + userInput)`
- `Statement.executeUpdate("UPDATE ... " + userInput)`
- `JdbcTemplate.query("SELECT ... WHERE id = " + id, ...)`
- Python: `cursor.execute("SELECT ... WHERE user = '%s'" % user)` or `f"SELECT ... {user}"`
- Node.js: `db.query("SELECT ... WHERE id = " + req.query.id)`

---

## 3. Vulnerable Code Example (Java)

```java
public User findUser(String username) throws SQLException {
    Connection conn = dataSource.getConnection();
    Statement stmt = conn.createStatement();
    // VULNERABLE: Dynamic SQL string concatenation
    String query = "SELECT * FROM users WHERE username = '" + username + "'";
    ResultSet rs = stmt.executeQuery(query);
    if (rs.next()) {
        return mapUser(rs);
    }
    return null;
}
```

---

## 4. Remediated Code Example (Java)

```java
public User findUser(String username) throws SQLException {
    String query = "SELECT * FROM users WHERE username = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        // SECURE: Strongly-typed query parameterization
        pstmt.setString(1, username);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return mapUser(rs);
            }
        }
    }
    return null;
}
```

---

## 5. Automated Fix Recipe

1. Replace `Statement` with `PreparedStatement`.
2. Replace dynamic string literals (`+ userVal`) with positional parameter placeholders (`?`) or named parameters (`:param`).
3. Bind inputs via type-specific setters (`setString()`, `setLong()`, `setUUID()`).
