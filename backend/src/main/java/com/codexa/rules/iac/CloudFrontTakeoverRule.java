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
 * CR-IAC-011: S3 Public Website Hosting & Insecure CloudFront Viewer Protocol.
 * Detects viewer_protocol_policy = "allow-all" permitting plain HTTP traffic,
 * and public-read S3 bucket ACLs vulnerable to subdomain takeover.
 */
@Component
public class CloudFrontTakeoverRule implements AnalysisRule {

    private static final Pattern VIEWER_ALLOW_ALL = Pattern.compile(
            "(?i)viewer_protocol_policy\\s*=\\s*[\"']allow-all[\"']"
    );

    private static final Pattern S3_PUBLIC_ACL = Pattern.compile(
            "(?i)acl\\s*=\\s*[\"']public-read(?:-write)?[\"']"
    );

    private static final Pattern S3_WEBSITE_ENDPOINT = Pattern.compile(
            "(?i)aws_s3_bucket_website_configuration"
    );

    @Override
    public String getRuleId() {
        return "CR-IAC-011";
    }

    @Override
    public String getName() {
        return "S3 Public Website Hosting & CloudFront Insecure Protocol";
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
        return "Detects CloudFront distributions allowing insecure plain HTTP communication and S3 buckets exposed via public website hosting without Origin Access Control (OAC), risking eavesdropping and subdomain takeovers.";
    }

    @Override
    public String getRemediation() {
        return "Configure viewer_protocol_policy = \"redirect-to-https\" and restrict S3 bucket access exclusively through CloudFront Origin Access Control (OAC) with public access blocks enabled.";
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
        return filename.endsWith(".tf") || filename.endsWith(".json") || filename.endsWith(".yaml");
    }

    private void scanFile(Path path, RuleContext context, List<RuleFinding> findings) {
        try {
            String content = Files.readString(path);
            String relPath = context.getProjectDirectory().relativize(path).toString().replace('\\', '/');

            String[] lines = content.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (VIEWER_ALLOW_ALL.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getConfidence())
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("CloudFront viewer_protocol_policy is set to 'allow-all', enabling unencrypted HTTP transport.")
                            .remediation("Set viewer_protocol_policy = \"redirect-to-https\" or \"https-only\".")
                            .build());
                } else if (S3_PUBLIC_ACL.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(Severity.CRITICAL)
                            .confidence(Confidence.HIGH)
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("S3 Bucket ACL is configured with public-read or public-read-write permissions.")
                            .remediation("Use private ACL and restrict access via IAM policies and CloudFront OAC.")
                            .build());
                }
            }
        } catch (IOException e) {
            // Ignore unreadable file
        }
    }
}
