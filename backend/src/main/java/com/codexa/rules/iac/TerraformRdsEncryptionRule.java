package com.codexa.rules.iac;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * CR-IAC-010: Terraform Unencrypted RDS Storage & Disabled KMS Rotation.
 * Detects aws_db_instance without storage_encrypted = true, publicly_accessible = true,
 * or aws_kms_key with enable_key_rotation = false.
 */
@Component
public class TerraformRdsEncryptionRule implements AnalysisRule {

    private static final Pattern UNENCRYPTED_RDS = Pattern.compile(
            "(?i)storage_encrypted\\s*=\\s*false"
    );

    private static final Pattern PUBLIC_RDS = Pattern.compile(
            "(?i)publicly_accessible\\s*=\\s*true"
    );

    private static final Pattern KMS_NO_ROTATION = Pattern.compile(
            "(?i)enable_key_rotation\\s*=\\s*false"
    );

    @Override
    public String getRuleId() {
        return "CR-IAC-010";
    }

    @Override
    public String getName() {
        return "Terraform RDS Unencrypted Storage & KMS Key Rotation Disabled";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.HIGH;
    }

    @Override
    public Confidence getConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getDescription() {
        return "Detects unencrypted RDS database storage, publicly accessible database instances, and KMS customer managed keys with annual automatic rotation disabled in Terraform configurations.";
    }

    @Override
    public String getRemediation() {
        return "Set storage_encrypted = true and associate a customer-managed KMS key. Set publicly_accessible = false and set enable_key_rotation = true on aws_kms_key.";
    }

    @Override
    public List<RuleFinding> analyze(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        if (context == null || context.getProjectDirectory() == null) {
            return findings;
        }

        try (Stream<Path> paths = Files.walk(context.getProjectDirectory())) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isTerraformFile)
                 .forEach(path -> scanFile(path, context, findings));
        } catch (IOException e) {
            // Ignore unreadable paths
        }

        return findings;
    }

    private boolean isTerraformFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.endsWith(".tf") || filename.endsWith(".tfvars");
    }

    private void scanFile(Path path, RuleContext context, List<RuleFinding> findings) {
        try {
            String content = Files.readString(path);
            String relPath = context.getProjectDirectory().relativize(path).toString().replace('\\', '/');

            String[] lines = content.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (UNENCRYPTED_RDS.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getConfidence())
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("RDS database instance explicitly disables storage encryption.")
                            .remediation("Set storage_encrypted = true with a valid kms_key_id.")
                            .build());
                } else if (PUBLIC_RDS.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(Severity.CRITICAL)
                            .confidence(Confidence.HIGH)
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("RDS database instance is configured to be publicly accessible from the internet.")
                            .remediation("Set publicly_accessible = false and place database inside private VPC subnets.")
                            .build());
                } else if (KMS_NO_ROTATION.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.HIGH)
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("AWS KMS Key has automatic key rotation disabled.")
                            .remediation("Set enable_key_rotation = true to ensure annual cryptographic key rotation.")
                            .build());
                }
            }
        } catch (IOException e) {
            // Ignore unreadable file
        }
    }
}
