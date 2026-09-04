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
public class ErrorHandlingRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-QUAL-006";
    }

    @Override
    public String getName() {
        return "Empty or Swallowed Exception Handling";
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
        return "Error Handling & Observability";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(CatchClause.class).forEach(clause -> {
            boolean isEmpty = clause.getBody().isEmpty() || clause.getBody().getStatements().isEmpty();
            String paramType = clause.getParameter().getTypeAsString();
            boolean isBroad = paramType.equals("Exception") || paramType.equals("Throwable");

            if (isEmpty || (isBroad && !containsLoggingOrRethrow(clause))) {
                int startLine = clause.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = clause.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(clause, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title(isEmpty ? "Empty catch block silently swallowing errors" : "Broad catch(" + paramType + ") without logging or rethrow")
                        .description("Detected catch block that silently suppresses exceptions or catches top-level " + paramType + " without adequate error logging or recovery. Swallowed exceptions mask critical operational errors.")
                        .impact("Silent failures in production, unpredictable state corruption, and severe debugging impediments.")
                        .remediation("Log the exception with stack trace (log.error(\"Operation failed\", e)) or rethrow as a custom domain RuntimeException.")
                        .suggestedFix("} catch (" + paramType + " e) {\n    log.error(\"Operation failed: {}\", e.getMessage(), e);\n    throw new ServiceException(\"Failed to process request\", e);\n}")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://cheatsheetseries.owasp.org/cheatsheets/Error_Handling_Cheat_Sheet.html"))
                        .build());
            }
        });

        return findings;
    }

    private boolean containsLoggingOrRethrow(CatchClause clause) {
        String body = clause.getBody().toString().toLowerCase();
        return body.contains("log.") || body.contains("logger.") || body.contains("throw ") || body.contains("printstacktrace");
    }
}
