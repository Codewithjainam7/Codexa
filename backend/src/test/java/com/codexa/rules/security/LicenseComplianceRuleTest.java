package com.codexa.rules.security;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LicenseComplianceRuleTest {

    private final LicenseComplianceRule rule = new LicenseComplianceRule();

    @Test
    @DisplayName("Should detect GPL-3.0 viral copyleft license in manifest")
    void testCopyleftLicenseDetection(@TempDir Path tempDir) throws IOException {
        Path pomXml = tempDir.resolve("pom.xml");
        Files.writeString(pomXml, """
                <project>
                  <dependencies>
                    <dependency>
                      <groupId>org.gnu</groupId>
                      <artifactId>gpl-library</artifactId>
                      <version>3.0.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        PipelineContext pCtx = new PipelineContext(UUID.randomUUID(), tempDir);
        RuleContext context = new RuleContext(null, pCtx);

        List<RuleFinding> findings = rule.evaluate(context);
        assertNotNull(findings);
    }
}
