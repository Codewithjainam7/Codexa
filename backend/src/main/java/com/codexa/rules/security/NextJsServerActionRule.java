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
 * CR-RSC-001: Insecure React Server Components & Next.js Server Action Mutations.
 * Detects 'use server' actions performing mutating database or service operations without session verification.
 */
@Component
public class NextJsServerActionRule implements AnalysisRule {

    private static final Pattern USE_SERVER_PATTERN = Pattern.compile("['\"]use server['\"]");
    private static final Pattern MUTATING_PATTERN = Pattern.compile(
            "(?i)(?:prisma\\.[a-zA-Z0-9_]+\\.(?:create|update|delete|upsert)|supabase\\.from\\([^)]+\\)\\.(?:insert|update|delete)|db\\.(?:insert|update|delete)|stripe\\.)"
    );
    private static final Pattern AUTH_CHECK_PATTERN = Pattern.compile(
            "(?i)(?:auth\\(\\)|getServerSession|getSession|verifySession|currentUser|supabase\\.auth\\.getUser|protect\\(\\))"
    );

    @Override
    public String getRuleId() {
        return "CR-RSC-001";
    }

    @Override
    public String getName() {
        return "Unauthenticated Next.js Server Action Mutation";
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
        return "A01:2021 - Broken Access Control";
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
                String fileName = path.getFileName().toString();
                if (fileName.endsWith(".ts") || fileName.endsWith(".tsx") || fileName.endsWith(".js") || fileName.endsWith(".jsx")) {
                    try {
                        String content = Files.readString(path);
                        if (USE_SERVER_PATTERN.matcher(content).find() && MUTATING_PATTERN.matcher(content).find()) {
                            if (!AUTH_CHECK_PATTERN.matcher(content).find()) {
                                String relPath = stagingDir.relativize(path).toString().replace('\\', '/');
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(getSeverity())
                                        .confidence(getDefaultConfidence())
                                        .title("Next.js Server Action with Missing Authentication Guard")
                                        .description("File contains a 'use server' action executing mutating database or payment actions without an explicit session or authentication verification call (e.g. auth(), getServerSession()). Anyone can invoke this endpoint directly via HTTP POST.")
                                        .impact("Unauthorized state mutation, payment tampering, or database compromise via unauthenticated RPC invocation.")
                                        .remediation("Validate user session using auth() or verifySession() inside every exported Server Action prior to performing state mutations.")
                                        .suggestedFix("// Secure Server Action:\n'use server';\nimport { auth } from '@/auth';\nexport async function updateRecord(formData: FormData) {\n  const session = await auth();\n  if (!session?.user) throw new Error('Unauthorized');\n  await db.record.update(...);\n}")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(1)
                                        .endLine(1)
                                        .evidence("'use server' directive with unauthenticated mutation")
                                        .references(List.of(
                                                "https://cwe.mitre.org/data/definitions/306.html",
                                                "https://nextjs.org/docs/app/building-your-application/data-fetching/server-actions-and-mutations#security"
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
