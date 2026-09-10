package com.codexa.rules.operations;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.AstSnippetExtractor;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnboundedThreadPoolRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-OPS-003";
    }

    @Override
    public String getName() {
        return "Unbounded Thread Pool Creation";
    }

    @Override
    public Category getCategory() {
        return Category.OPERATIONS;
    }

    @Override
    public Severity getSeverity() {
        return Severity.MEDIUM;
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
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) return findings;

        CompilationUnit cu = parsedFile.getCompilationUnit().get();
        String filename = parsedFile.getRelativePath();

        for (MethodCallExpr call : cu.findAll(MethodCallExpr.class)) {
            if ("newCachedThreadPool".equals(call.getNameAsString())) {
                boolean isExecutors = call.getScope()
                        .map(s -> "Executors".equals(s.toString()))
                        .orElse(false);
                if (isExecutors) {
                    int startLine = call.getBegin().map(p -> p.line).orElse(1);
                    int endLine = call.getEnd().map(p -> p.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Unbounded Cached Thread Pool Creation")
                            .description("Executors.newCachedThreadPool() creates an unbounded pool that spawns new threads without limit under burst load, causing OutOfMemoryError or thread exhaustion.")
                            .impact("Application denial-of-service, JVM heap/thread exhaustion, and server unresponsiveness under traffic spikes.")
                            .remediation("Use a bounded ThreadPoolExecutor or Executors.newFixedThreadPool(n) with a capacity-constrained BlockingQueue.")
                            .suggestedFix("ExecutorService pool = new ThreadPoolExecutor(4, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(500));")
                            .owaspMapping(getOwaspMapping())
                            .filePath(filename)
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of("https://wiki.sei.cmu.edu/confluence/display/java/TPS01-J.+Do+not+execute+unbounded+numbers+of+threads"))
                            .build());
                }
            }
        }

        return findings;
    }
}
