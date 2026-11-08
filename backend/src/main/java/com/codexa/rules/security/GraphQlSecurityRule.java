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
 * CR-GRAPHQL-001: GraphQL Query Depth Limiting & Production Introspection Exposure.
 * Flags GraphQL configurations lacking query depth limiting (DoS risk) or exposing schema introspection in production.
 */
@Component
public class GraphQlSecurityRule implements AnalysisRule {

    private static final Pattern GRAPHQL_SERVER_PATTERN = Pattern.compile(
            "(?i)(?:ApolloServer|GraphQLSchema|buildSchema|GraphQLModule|createYoga|graphqlHTTP|GraphQLJava|graphql-spring-boot)"
    );

    private static final Pattern INTROSPECTION_ENABLED_PATTERN = Pattern.compile(
            "(?i)introspection\\s*:\\s*true|spring\\.graphql\\.schema\\.inspection\\.enabled\\s*=\\s*true"
    );

    private static final Pattern DEPTH_LIMIT_PATTERN = Pattern.compile(
            "(?i)(?:depthLimit|maxQueryDepth|MaxDepthValidation|costLimit|queryComplexity)"
    );

    @Override
    public String getRuleId() {
        return "CR-GRAPHQL-001";
    }

    @Override
    public String getName() {
        return "GraphQL Query Depth Limiting & Introspection Exposure";
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
        return "A04:2021 - Insecure Design";
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
                if (fileName.endsWith(".js") || fileName.endsWith(".ts") || fileName.endsWith(".java") ||
                    fileName.endsWith(".py") || fileName.endsWith(".yml") || fileName.endsWith(".properties")) {
                    try {
                        String content = Files.readString(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        if (GRAPHQL_SERVER_PATTERN.matcher(content).find()) {
                            // Check for missing depth limiting
                            if (!DEPTH_LIMIT_PATTERN.matcher(content).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(Severity.HIGH)
                                        .confidence(getDefaultConfidence())
                                        .title("Missing GraphQL Query Depth / Complexity Limiter")
                                        .description("Detected GraphQL server configuration without query depth limiting or query complexity analysis. Attackers can execute deeply nested cyclical queries (e.g. author -> books -> author -> books) causing severe CPU and memory exhaustion (Denial of Service).")
                                        .impact("Severe server thread starvation, CPU spike, and denial of service across all API clients.")
                                        .remediation("Install a query depth limit plugin (e.g. graphql-depth-limit for Node.js, MaxQueryDepthInstrumentation for Java) and clamp maximum depth to 5-7 levels.")
                                        .suggestedFix("// Secure remediation (Node.js):\nimport depthLimit from 'graphql-depth-limit';\nconst server = new ApolloServer({\n  schema,\n  validationRules: [depthLimit(6)]\n});")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(1)
                                        .endLine(1)
                                        .evidence("GraphQL server initialization without depthLimit")
                                        .references(List.of(
                                                "https://cwe.mitre.org/data/definitions/770.html",
                                                "https://cheatsheetseries.owasp.org/cheatsheets/GraphQL_Cheat_Sheet.html"
                                        ))
                                        .build());
                            }

                            // Check for explicit introspection enabled in production
                            if (INTROSPECTION_ENABLED_PATTERN.matcher(content).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(Severity.MEDIUM)
                                        .confidence(getDefaultConfidence())
                                        .title("GraphQL Schema Introspection Enabled")
                                        .description("Detected GraphQL introspection enabled. Allowing arbitrary external clients to query __schema and __type leaks internal database models, unpublished mutations, and administrative queries.")
                                        .impact("Information disclosure of complete GraphQL schema and attack surface.")
                                        .remediation("Disable introspection in production environments: introspection: process.env.NODE_ENV !== 'production'.")
                                        .suggestedFix("introspection: process.env.NODE_ENV !== 'production'")
                                        .owaspMapping("A01:2021 - Broken Access Control")
                                        .filePath(relPath)
                                        .startLine(1)
                                        .endLine(1)
                                        .evidence("introspection: true")
                                        .references(List.of("https://cwe.mitre.org/data/definitions/200.html"))
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
