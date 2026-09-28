package com.codexa.rules.security;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Severity;
import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.analysis.service.SbomDependencyService;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyRiskRuleTest {

    private DependencyRiskRule rule;

    @BeforeEach
    void setUp() {
        rule = new DependencyRiskRule(new SbomDependencyService());
    }

    @Test
    void shouldVerifyRuleMetadata() {
        assertThat(rule.getRuleId()).isEqualTo("CR-DEP-001");
        assertThat(rule.getName()).isEqualTo("Vulnerable or Outdated Dependency");
        assertThat(rule.getCategory()).isEqualTo(Category.SECURITY);
        assertThat(rule.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(rule.getOwaspMapping()).contains("Software Supply Chain Security");
    }

    @Test
    void shouldDetectVulnerableLog4jInPomXml(@TempDir Path tempDir) throws IOException {
        String pomXml = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>org.apache.logging.log4j</groupId>
                        <artifactId>log4j-core</artifactId>
                        <version>2.14.1</version>
                    </dependency>
                </dependencies>
            </project>
            """;
        Files.writeString(tempDir.resolve("pom.xml"), pomXml);

        PipelineContext pipelineContext = new PipelineContext(UUID.randomUUID(), tempDir);
        RuleContext ruleContext = new RuleContext(null, pipelineContext);

        List<RuleFinding> findings = rule.evaluate(ruleContext);

        assertThat(findings).isNotEmpty();
        RuleFinding finding = findings.get(0);
        assertThat(finding.ruleId()).isEqualTo("CR-DEP-001");
        assertThat(finding.severity()).isEqualTo(Severity.CRITICAL);
        assertThat(finding.title()).contains("log4j-core:2.14.1");
        assertThat(finding.evidence()).contains("CVE-2021-44228");
        assertThat(finding.remediation()).contains("2.17.1");
    }

    @Test
    void shouldPassCleanSecureDependencies(@TempDir Path tempDir) throws IOException {
        String pomXml = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>org.apache.logging.log4j</groupId>
                        <artifactId>log4j-core</artifactId>
                        <version>2.20.0</version>
                    </dependency>
                </dependencies>
            </project>
            """;
        Files.writeString(tempDir.resolve("pom.xml"), pomXml);

        PipelineContext pipelineContext = new PipelineContext(UUID.randomUUID(), tempDir);
        RuleContext ruleContext = new RuleContext(null, pipelineContext);

        List<RuleFinding> findings = rule.evaluate(ruleContext);

        assertThat(findings).isEmpty();
    }
}
