package com.codexa.rules.api;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OWASP API Security Top 10 (2023) Rule.
 * Audits REST controllers for Mass Assignment (BOPLA), Missing Pagination (Resource Exhaustion),
 * and Broken Function Level Authorization (BFLA).
 */
@Component
public class ApiSecurityTop10Rule implements AnalysisRule {

    @Override
    public String getRuleId() {
        return "CR-API-001";
    }

    @Override
    public String getName() {
        return "OWASP API Security Flaw (BOPLA, BFLA, Resource Consumption)";
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
        return "API3:2023 - Broken Object Property Level Authorization";
    }

    @Override
    public String getDescription() {
        return "Detects Mass Assignment vulnerabilities, unpaginated bulk database exports, and unauthenticated administrative routes.";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile javaFile = context.getParsedJavaFile();
        if (javaFile == null) return findings;

        var cuOpt = javaFile.getCompilationUnit();
        if (cuOpt.isEmpty()) return findings;
        CompilationUnit cu = cuOpt.get();

        for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            boolean isRestController = clazz.getAnnotations().stream()
                    .anyMatch(a -> a.getNameAsString().equals("RestController") || a.getNameAsString().equals("Controller"));

            if (!isRestController) continue;

            for (MethodDeclaration method : clazz.getMethods()) {
                boolean isPublic = method.isPublic();
                if (!isPublic) continue;

                // 1. Check Mass Assignment / BOPLA (CR-API-001)
                boolean isMutating = method.getAnnotations().stream().anyMatch(a ->
                        a.getNameAsString().equals("PostMapping") ||
                        a.getNameAsString().equals("PutMapping") ||
                        a.getNameAsString().equals("PatchMapping"));

                if (isMutating) {
                    for (Parameter param : method.getParameters()) {
                        boolean hasRequestBody = param.getAnnotations().stream().anyMatch(a -> a.getNameAsString().equals("RequestBody"));
                        String paramType = param.getTypeAsString();

                        // If parameter type is named Entity, or does not end with DTO / Request / Form, and is a domain object
                        if (hasRequestBody && (paramType.endsWith("Entity") || paramType.equals("User") || paramType.equals("Account") || paramType.equals("Order"))) {
                            findings.add(RuleFinding.builder()
                                    .ruleId("CR-API-001")
                                    .category(Category.SECURITY)
                                    .severity(Severity.HIGH)
                                    .confidence(Confidence.HIGH)
                                    .filePath(javaFile.getRelativePath())
                                    .startLine(param.getBegin().map(p -> p.line).orElse(1))
                                    .endLine(param.getEnd().map(p -> p.line).orElse(1))
                                    .title("API Mass Assignment (BOPLA) via Raw Entity Binding: " + paramType)
                                    .description("Method '" + method.getNameAsString() + "' binds request body directly to JPA entity '" + paramType + "', exposing internal entity properties to unauthorized client tampering.")
                                    .impact("Attacker can overwrite privileged database columns (e.g. role, balance, is_admin) via HTTP payload.")
                                    .remediation("Replace raw entity binding with an explicit, immutable Data Transfer Object (DTO) e.g. '" + paramType + "Request'.")
                                    .suggestedFix("public ResponseEntity<?> " + method.getNameAsString() + "(@Valid @RequestBody " + paramType + "Request request)")
                                    .evidence("@RequestBody " + paramType + " " + param.getNameAsString())
                                    .owaspMapping("API3:2023 - Broken Object Property Level Authorization")
                                    .references(List.of("https://owasp.org/API-Security/editions/2023/en/0xa3-broken-object-property-level-authorization/"))
                                    .build());
                        }
                    }
                }

                // 2. Check Missing Pagination / Resource Exhaustion (CR-API-002)
                boolean isGetRoute = method.getAnnotations().stream().anyMatch(a -> a.getNameAsString().equals("GetMapping"));
                if (isGetRoute) {
                    String returnType = method.getTypeAsString();
                    if (returnType.startsWith("List<") || returnType.startsWith("Set<") || returnType.startsWith("Collection<")) {
                        boolean hasPageable = method.getParameters().stream().anyMatch(p ->
                                p.getTypeAsString().equals("Pageable") || p.getNameAsString().equalsIgnoreCase("limit") || p.getNameAsString().equalsIgnoreCase("page"));

                        if (!hasPageable) {
                            findings.add(RuleFinding.builder()
                                    .ruleId("CR-API-002")
                                    .category(Category.OPERATIONS)
                                    .severity(Severity.MEDIUM)
                                    .confidence(Confidence.HIGH)
                                    .filePath(javaFile.getRelativePath())
                                    .startLine(method.getBegin().map(p -> p.line).orElse(1))
                                    .endLine(method.getBegin().map(p -> p.line).orElse(1))
                                    .title("Unrestricted API Resource Consumption: Missing Pagination on " + method.getNameAsString())
                                    .description("GET endpoint returns unbounded collection (" + returnType + ") without Pageable contract, risking JVM heap exhaustion and DoS.")
                                    .impact("Querying millions of database records in a single request causes OutOfMemoryError and service degradation.")
                                    .remediation("Adopt Spring Data Pageable pagination: return Page<T> or slice queries with bounded limits.")
                                    .suggestedFix("public ResponseEntity<Page<T>> " + method.getNameAsString() + "(Pageable pageable)")
                                    .evidence("public " + returnType + " " + method.getNameAsString() + "(...)")
                                    .owaspMapping("API4:2023 - Unrestricted Resource Consumption")
                                    .references(List.of("https://owasp.org/API-Security/editions/2023/en/0xa4-unrestricted-resource-consumption/"))
                                    .build());
                        }
                    }
                }

                // 3. Check Broken Function Level Authorization (BFLA) (CR-API-003)
                boolean isDestructive = method.getAnnotations().stream().anyMatch(a -> a.getNameAsString().equals("DeleteMapping"));
                String methodName = method.getNameAsString().toLowerCase();
                if (isDestructive || methodName.contains("admin") || methodName.contains("deleteall") || methodName.contains("purge")) {
                    boolean hasPreAuthorize = method.getAnnotations().stream().anyMatch(a ->
                            a.getNameAsString().equals("PreAuthorize") || a.getNameAsString().equals("Secured") || a.getNameAsString().equals("RolesAllowed"));

                    boolean classHasPreAuthorize = clazz.getAnnotations().stream().anyMatch(a ->
                            a.getNameAsString().equals("PreAuthorize") || a.getNameAsString().equals("Secured"));

                    if (!hasPreAuthorize && !classHasPreAuthorize) {
                        findings.add(RuleFinding.builder()
                                .ruleId("CR-API-003")
                                .category(Category.SECURITY)
                                .severity(Severity.HIGH)
                                .confidence(Confidence.HIGH)
                                .filePath(javaFile.getRelativePath())
                                .startLine(method.getBegin().map(p -> p.line).orElse(1))
                                .endLine(method.getBegin().map(p -> p.line).orElse(1))
                                .title("Broken Function Level Authorization (BFLA) on " + method.getNameAsString())
                                .description("Sensitive administrative or destructive endpoint '" + method.getNameAsString() + "' lacks role-based access control (@PreAuthorize).")
                                .impact("Regular authenticated users can invoke privileged actions without administrative authorization.")
                                .remediation("Add `@PreAuthorize(\"hasRole('ADMIN')\")` to enforce function-level authorization.")
                                .suggestedFix("@PreAuthorize(\"hasRole('ADMIN')\")\n" + method.getDeclarationAsString())
                                .evidence(method.getDeclarationAsString())
                                .owaspMapping("API5:2023 - Broken Function Level Authorization")
                                .references(List.of("https://owasp.org/API-Security/editions/2023/en/0xa5-broken-function-level-authorization/"))
                                .build());
                    }
                }
            }
        }

        return findings;
    }
}
