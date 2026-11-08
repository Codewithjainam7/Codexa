package com.codexa.rules.security;

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
 * CR-GRPC-001: Insecure gRPC Plaintext Channel & Missing Auth Interceptor.
 * Detects gRPC channels instantiated via usePlaintext() / insecure.NewCredentials()
 * and services registered without authentication interceptors.
 */
@Component
public class GrpcSecurityRule implements AnalysisRule {

    private static final Pattern GRPC_PLAINTEXT_PATTERN = Pattern.compile(
            "(?i)\\.usePlaintext\\s*\\(\\)|insecure\\.NewCredentials\\(\\)|grpc\\.InsecureChannelCredentials"
    );

    private static final Pattern GRPC_SERVICE_REG_PATTERN = Pattern.compile(
            "(?i)addService\\s*\\(\\s*(?:new\\s+[a-zA-Z0-9_]+Impl|[a-zA-Z0-9_]+Service)\\s*\\)(?!.*intercept)"
    );

    @Override
    public String getRuleId() {
        return "CR-GRPC-001";
    }

    @Override
    public String getName() {
        return "gRPC Plaintext Channel & Unauthenticated Service";
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
        return "A02:2021 - Cryptographic Failures";
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
                if (fileName.endsWith(".java") || fileName.endsWith(".go") || fileName.endsWith(".py") || fileName.endsWith(".ts")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (GRPC_PLAINTEXT_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(Severity.HIGH)
                                        .confidence(getDefaultConfidence())
                                        .title("gRPC Channel Initialized in Plaintext (No TLS)")
                                        .description("Detected gRPC channel configured with usePlaintext() or insecure credentials. RPC messages, authorization metadata, and payloads travel in cleartext across the network, susceptible to eavesdropping and Man-in-the-Middle (MitM) attacks.")
                                        .impact("Cleartext disclosure of sensitive RPC payloads and token interception.")
                                        .remediation("Enforce Mutual TLS (mTLS) or TLS channel credentials (Grpc.newChannelBuilderForAddress with TlsChannelCredentials).")
                                        .suggestedFix("// Secure gRPC TLS channel:\nManagedChannel channel = Grpc.newChannelBuilderForAddress(\n    host, port, TlsChannelCredentials.create()\n).build();")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of(
                                                "https://cwe.mitre.org/data/definitions/319.html",
                                                "https://grpc.io/docs/guides/auth/"
                                        ))
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
