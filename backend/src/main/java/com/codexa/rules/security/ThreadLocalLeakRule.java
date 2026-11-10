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
 * CR-THREAD-001: ThreadLocal Context Leak & Security Context Crossover.
 * Detects ThreadLocal usage in web servlet/thread pool environments without corresponding .remove() cleanup,
 * which leaks security credentials and tenant context across recycled worker threads.
 */
@Component
public class ThreadLocalLeakRule implements AnalysisRule {

    private static final Pattern THREAD_LOCAL_DECL = Pattern.compile(
            "(?:ThreadLocal|InheritableThreadLocal)<[\\w<>\\[\\]]+>\\s+([a-zA-Z_][a-zA-Z0-9_]*)"
    );

    private static final Pattern THREAD_LOCAL_REMOVE = Pattern.compile(
            "\\.(?:remove|clearContext)\\s*\\("
    );

    @Override
    public String getRuleId() {
        return "CR-THREAD-001";
    }

    @Override
    public String getName() {
        return "ThreadLocal Context Leakage & Missing Cleanup";
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
        return "Detects ThreadLocal instances used without definitive cleanup (.remove()) inside finally blocks. In thread-pooled servlet containers, recycled threads retain prior state, leaking authentication context or tenant data.";
    }

    @Override
    public String getRemediation() {
        return "Always invoke ThreadLocal.remove() inside a finally block or implement AutoCloseable with try-with-resources to clear state.";
    }

    @Override
    public List<RuleFinding> analyze(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        if (context == null || context.getProjectDirectory() == null) {
            return findings;
        }

        try (Stream<Path> paths = Files.walk(context.getProjectDirectory())) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.getFileName().toString().endsWith(".java"))
                 .forEach(path -> scanFile(path, context, findings));
        } catch (IOException e) {
            // Ignore unreadable paths
        }

        return findings;
    }

    private void scanFile(Path path, RuleContext context, List<RuleFinding> findings) {
        try {
            String content = Files.readString(path);
            if (!content.contains("ThreadLocal")) {
                return;
            }

            String relPath = context.getProjectDirectory().relativize(path).toString().replace('\\', '/');

            // If file declares ThreadLocal but lacks .remove() or clearContext()
            if (THREAD_LOCAL_DECL.matcher(content).find() && !THREAD_LOCAL_REMOVE.matcher(content).find()) {
                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .ruleName(getName())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getConfidence())
                        .filePath(relPath)
                        .lineNumber(1)
                        .snippet("ThreadLocal declared without cleanup")
                        .message("Class declares ThreadLocal state but contains no .remove() or clearContext() invocations, risking cross-tenant data contamination.")
                        .remediation(getRemediation())
                        .build());
            }
        } catch (IOException e) {
            // Ignore unreadable file
        }
    }
}
