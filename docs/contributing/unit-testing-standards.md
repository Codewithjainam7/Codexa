# Codexa Backend Testing Conventions, Quality Gates & Regression Standards

This specification establishes the unit and integration testing standards, testing pyramid hierarchy, mock fixture requirements, and regression gating policies enforced in **Codexa**.

---

## 1. Testing Pyramid & Performance SLAs

Codexa enforces a strict testing pyramid to ensure fast developer feedback and deterministic CI execution:

```
                  / \
                 /   \
                / E2E \       < 5 Integration Tests
               /       \      (Full Pipeline Execution: Zip to Report)
              /---------\
             /   Slice   \    < 25 Slice Tests
            /  Component  \   (@WebMvcTest, @DataJpaTest, In-Memory DB)
           /---------------\
          /      Unit       \  105+ Fast In-Memory Unit Tests
         /  AST / Rules / Sec\ (JUnit 5, JavaParser, Zero Spring Context)
        /---------------------\
```

### Performance SLAs:
- **Total Test Suite Duration**: The complete 135+ test suite must execute in $< 45\text{ seconds}$ on standard developer machines.
- **Unit Test Execution Speed**: Individual static rule tests must execute in $< 50\text{ ms}$.
- **Zero Flakiness Invariant**: Tests must be 100% deterministic; relying on `Thread.sleep()`, wall-clock timestamps, or unpinned random generators is strictly prohibited.

---

## 2. Rule Testing Contract: Positive & Negative Fixtures

Every static analysis rule (`AnalysisRule`) must be validated with both positive vulnerability tests and negative clean code tests:

```java
class SqlInjectionRuleTest {

    private final SqlInjectionRule rule = new SqlInjectionRule();
    private final JavaParser parser = new JavaParser();

    @Test
    void shouldDetectVulnerableConcatenation() {
        String code = """
            public class UserDao {
                public void find(String id) {
                    String sql = "SELECT * FROM users WHERE id = '" + id + "'";
                    stmt.executeQuery(sql);
                }
            }
            """;
        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ParsedJavaFile file = new ParsedJavaFile("UserDao.java", code, cu, List.of());
        RuleContext context = new RuleContext(file, null);

        List<RuleFinding> findings = rule.evaluate(context);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).ruleId()).isEqualTo("CR-SQL-001");
        assertThat(findings.get(0).severity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void shouldPassCleanPreparedStatement() {
        String code = """
            public class UserDao {
                public void find(String id) {
                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
                    ps.setString(1, id);
                    ps.executeQuery();
                }
            }
            """;
        CompilationUnit cu = parser.parse(code).getResult().orElseThrow();
        ParsedJavaFile file = new ParsedJavaFile("UserDao.java", code, cu, List.of());
        RuleContext context = new RuleContext(file, null);

        List<RuleFinding> findings = rule.evaluate(context);

        assertThat(findings).isEmpty(); // Negative test: zero false positives
    }
}
```

---

## 3. False-Positive Regression Prevention (`ScannerFalsePositiveRegressionTest`)

To guarantee that new rules or regex updates do not introduce false alarms:
1. **Comment & Docstring Exclusion**: Validates that vulnerable patterns appearing inside code comments (`// SELECT * FROM users WHERE id = ' + id`) are never flagged as findings.
2. **Test File / Mock Filtering**: Validates that files in `src/test/` or files ending in `Test.java` / `Mock.java` are appropriately contextualized.
3. **Entropy Calibration**: Verifies that standard test tokens (e.g. `TEST_SECRET_ABC123`) do not trigger false positive credential alerts.

---

## 4. File Ingestion & Staging Testing with JUnit 5 `@TempDir`

Tests that involve file extraction, ZIP archives, or directory traversal must use JUnit 5 `@TempDir` for isolated filesystem lifecycle management:

```java
class SecureZipExtractorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDetectZipSlipDirectoryTraversal() throws Exception {
        byte[] zipBytes = createMaliciousZip("../../evil.sh");
        ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);

        assertThatThrownBy(() -> extractor.extract(bais, tempDir))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("ZIP_SLIP_DETECTED");

        // Verify no files escaped outside tempDir
        assertThat(Files.exists(tempDir.resolve("../../evil.sh"))).isFalse();
    }
}
```

---

## 5. JaCoCo Code Coverage Quality Gates

Codexa enforces automated code coverage thresholds via the `jacoco-maven-plugin`:

| Code Layer | Minimum Instruction Coverage | Minimum Branch Coverage |
|:---|:---:|:---:|
| **Security Rules (`com.codexa.rules.security`)** | **85%** | **80%** |
| **Ingestion & Sanitization (`com.codexa.ingestion`)** | **80%** | **75%** |
| **Scoring Engine (`com.codexa.scoring`)** | **90%** | **85%** |
| **Overall Platform Target** | **75%** | **70%** |

### Execution Command:
```bash
# Run unit tests with coverage report generation
cd backend && mvn test jacoco:report
```
The generated HTML report is available at `target/site/jacoco/index.html`.
