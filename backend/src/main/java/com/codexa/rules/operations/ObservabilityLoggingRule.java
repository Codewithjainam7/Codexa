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
public class ObservabilityLoggingRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-OPS-002";
    }

    @Override
    public String getName() {
        return "Unstructured Standard Console Output";
    }

    @Override
    public Category getCategory() {
        return Category.OPERATIONS;
    }

    @Override
    public Severity getSeverity() {
        return Severity.LOW;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "Operational Readiness";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String fullCall = call.toString();
            if (fullCall.startsWith("System.out.print") || fullCall.startsWith("System.err.print") || fullCall.equals("printStackTrace()")) {
                int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("Unstructured console output ('" + call.getNameAsString() + "')")
                        .description("Detected direct console output (" + fullCall + "). Production Spring Boot services should utilize structured SLF4J loggers rather than standard out/err streams.")
                        .impact("Loss of log correlation, log level filtering, timestamps, and centralized ingestion in log shippers (e.g. Datadog, ELK).")
                        .remediation("Replace standard console printing with SLF4J Logger (e.g. private static final Logger log = LoggerFactory.getLogger(...)).")
                        .suggestedFix("log.info(\"Message: {}\", data);")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://www.slf4j.org/manual.html"))
                        .build());
            }
        });

        return findings;
    }
}
