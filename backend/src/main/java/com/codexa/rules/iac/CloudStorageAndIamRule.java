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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Cloud Storage & IAM IaC Security Rule:
 * CR-IAC-007: Overly Permissive IAM Policy (Wildcard Action: "*")
 * CR-IAC-008: Public Cloud Storage Bucket (S3/GCS Public Ingress)
 */
@Component
public class CloudStorageAndIamRule implements AnalysisRule {

    private static final Pattern WILDCARD_IAM_PATTERN = Pattern.compile(
            "(?i)[\"']Action[\"']\\s*:\\s*(?:\\[\\s*[\"']\\*[\"']\\s*\\]|[\"']\\*[\"'])|action\\s*=\\s*\\[?[\"']\\*[\"']\\]?"
    );

    private static final Pattern PUBLIC_BUCKET_PATTERN = Pattern.compile(
            "(?i)acl\\s*=\\s*[\"']public-read[\"']|block_public_acls\\s*=\\s*false|block_public_policy\\s*=\\s*false|ignore_public_acls\\s*=\\s*false|restrict_public_buckets\\s*=\\s*false"
    );

    @Override
    public String getRuleId() {
        return "CR-IAC-007";
    }

    @Override
    public String getName() {
        return "Cloud Infrastructure Permissive IAM & Storage Policies";
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
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "A05:2021 - Security Misconfiguration";
    }

    @Override
    public boolean isRepositoryWide() {
        return true;
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        Path stagingDir = context.getStagingDirectory();

        if (stagingDir == null || !Files.exists(stagingDir)) {
            return findings;
        }

        try (Stream<Path> stream = Files.walk(stagingDir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                if (fileName.endsWith(".tf") || fileName.endsWith(".json") || fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);

                            if (WILDCARD_IAM_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId("CR-IAC-007")
                                        .category(getCategory())
                                        .severity(Severity.CRITICAL)
                                        .confidence(Confidence.HIGH)
                                        .title("Overly Permissive Cloud IAM Policy with Wildcard Action")
                                        .description("Detected IAM policy granting wildcard actions ('*'). This grants administrative privileges across all cloud service APIs, violating the Principle of Least Privilege.")
                                        .impact("Total privilege escalation, unauthorized cloud resource modification, data exfiltration, or infrastructure deletion.")
                                        .remediation("Scope IAM permissions to only the explicit API actions required (e.g. ['s3:GetObject', 's3:PutObject']).")
                                        .suggestedFix("// Secure IAM Action restriction:\nAction = [\"s3:GetObject\", \"s3:PutObject\"]\nResource = [aws_s3_bucket.app_data.arn]")
                                        .owaspMapping("A01:2021 - Broken Access Control")
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html#grant-least-privilege"))
                                        .build());
                            }

                            if (PUBLIC_BUCKET_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId("CR-IAC-008")
                                        .category(getCategory())
                                        .severity(Severity.CRITICAL)
                                        .confidence(Confidence.HIGH)
                                        .title("Public Cloud Storage Bucket Exposure")
                                        .description("Detected cloud storage configuration with public read ACL or disabled S3 Block Public Access controls. Cloud storage buckets containing application data must never be exposed publicly.")
                                        .impact("Mass confidential data breach and unintended cloud egress cost spikes.")
                                        .remediation("Enable all four S3 Block Public Access flags and restrict access via authenticated IAM and pre-signed URLs.")
                                        .suggestedFix("resource \"aws_s3_bucket_public_access_block\" \"secure\" {\n  bucket = aws_s3_bucket.data.id\n  block_public_acls = true\n  block_public_policy = true\n  ignore_public_acls = true\n  restrict_public_buckets = true\n}")
                                        .owaspMapping("A05:2021 - Security Misconfiguration")
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://cwe.mitre.org/data/definitions/732.html"))
                                        .build());
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        } catch (IOException ignored) {
        }

        return findings;
    }
}
