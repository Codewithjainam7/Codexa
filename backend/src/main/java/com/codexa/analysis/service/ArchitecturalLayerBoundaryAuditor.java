package com.codexa.analysis.service;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * CR-ARCH-002: Architectural Layer Boundary Inversion & Controller-to-DAO Direct Coupling.
 * Audits enterprise codebase layering rules: Controllers bypassing Service layer to directly invoke Repositories/DAOs.
 */
@Service
public class ArchitecturalLayerBoundaryAuditor implements AnalysisRule {

    private static final Pattern CONTROLLER_ANNOTATION = Pattern.compile(
            "@(RestController|Controller)"
    );

    private static final Pattern DIRECT_REPOSITORY_IMPORT = Pattern.compile(
            "import\\s+[\\w.]+\\.(repository|dao)\\.[\\w]+;"
    );

    private static final Pattern DIRECT_REPOSITORY_INJECTION = Pattern.compile(
            "(?:private\\s+final\\s+[\\w]+(?:Repository|Dao)|@Autowired\\s+(?:private\\s+)?[\\w]+(?:Repository|Dao))"
    );

    @Override
    public String getRuleId() {
        return "CR-ARCH-002";
    }

    @Override
    public String getName() {
        return "Architectural Layer Boundary Inversion";
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
    public Confidence getConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getDescription() {
        return "Detects presentation layer controllers directly injecting and coupling to database repositories or DAOs, bypassing intermediate service domain logic and violating clean tiered architecture.";
    }

    @Override
    public String getRemediation() {
        return "Mediate database operations through a dedicated domain Service layer. Controllers should only interact with application and domain services.";
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
                 .forEach(path -> scanJavaFile(path, context, findings));
        } catch (IOException e) {
            // Ignore unreadable paths
        }

        return findings;
    }

    private void scanJavaFile(Path path, RuleContext context, List<RuleFinding> findings) {
        try {
            String content = Files.readString(path);
            if (!CONTROLLER_ANNOTATION.matcher(content).find() && !path.getFileName().toString().endsWith("Controller.java")) {
                return;
            }

            String relPath = context.getProjectDirectory().relativize(path).toString().replace('\\', '/');
            String[] lines = content.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (DIRECT_REPOSITORY_IMPORT.matcher(line).find() || DIRECT_REPOSITORY_INJECTION.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getConfidence())
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("Controller directly references and injects Repository/DAO layer, violating architectural separation of concerns.")
                            .remediation(getRemediation())
                            .build());
                }
            }
        } catch (IOException e) {
            // Ignore unreadable file
        }
    }
}
