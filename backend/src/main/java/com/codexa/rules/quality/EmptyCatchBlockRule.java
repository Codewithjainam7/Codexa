package com.codexa.rules.quality;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.AstSnippetExtractor;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmptyCatchBlockRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-QUAL-006";
    }

    @Override
    public String getName() {
        return "Empty Catch Block (Suppressed Exception)";
    }

    @Override
    public Category getCategory() {
        return Category.QUALITY;
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
        return "A09:2021 - Security Logging and Monitoring Failures";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) return findings;

        CompilationUnit cu = parsedFile.getCompilationUnit().get();
        String filename = parsedFile.getRelativePath();

        for (CatchClause cc : cu.findAll(CatchClause.class)) {
            if (cc.getBody().getStatements().isEmpty()) {
                int startLine = cc.getBegin().map(p -> p.line).orElse(1);
                int endLine = cc.getEnd().map(p -> p.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(cc, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("Empty Catch Block: " + cc.getParameter().getNameAsString())
                        .description("Empty catch block silently suppresses exceptions without logging or rethrowing. This masks critical system failures and security errors.")
                        .impact("Silent data corruption, unhandled failure modes, and impeded incident response triage.")
                        .remediation("Log the exception with context (log.error(...)) or rethrow as a domain-specific RuntimeException.")
                        .suggestedFix("catch (Exception e) {\n    log.warn(\"Operation failed\", e);\n}")
                        .owaspMapping(getOwaspMapping())
                        .filePath(filename)
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://owasp.org/Top10/2021/A09_2021-Security_Logging_and_Monitoring_Failures/"))
                        .build());
            }
        }

        return findings;
    }
}
