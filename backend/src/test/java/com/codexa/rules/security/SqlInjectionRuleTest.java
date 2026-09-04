package com.codexa.rules.security;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.JavaAstParserService;
import com.codexa.security.ast.ParsedJavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SqlInjectionRuleTest {

    private SqlInjectionRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new SqlInjectionRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateVulnerableConcatenationShouldFlagFinding() {
        String code = """
                package com.example;
                import java.sql.Statement;
                public class UserDao {
                    public void findUser(Statement stmt, String id) throws Exception {
                        String query = "SELECT * FROM users WHERE id = '" + id + "'";
                        stmt.executeQuery("SELECT * FROM users WHERE id = '" + id + "'");
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("UserDao.java"), "UserDao.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        RuleFinding finding = findings.get(0);
        assertEquals("CR-SQL-001", finding.ruleId());
        assertTrue(finding.description().contains("dynamic SQL query construction"));
    }

    @Test
    void evaluateSafeParameterizedQueryShouldNotFlagFinding() {
        String safeCode = """
                package com.example;
                import java.sql.PreparedStatement;
                public class UserDao {
                    public void findUser(PreparedStatement ps, String id) throws Exception {
                        ps.setString(1, id);
                        ps.executeQuery();
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(safeCode, Paths.get("UserDao.java"), "UserDao.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertTrue(findings.isEmpty());
    }
}
