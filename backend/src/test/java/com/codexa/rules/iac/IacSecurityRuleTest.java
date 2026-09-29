package com.codexa.rules.iac;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IacSecurityRuleTest {

    private final IacSecurityRule rule = new IacSecurityRule();

    @Test
    @DisplayName("Should detect root execution and latest tag in Dockerfile")
    void testDockerfileRootAndLatest(@TempDir Path tempDir) throws IOException {
        Path dockerfile = tempDir.resolve("Dockerfile");
        Files.writeString(dockerfile, """
                FROM node:latest
                WORKDIR /app
                COPY . .
                USER root
                CMD ["node", "server.js"]
                """);

        PipelineContext pCtx = new PipelineContext(UUID.randomUUID(), tempDir);
        RuleContext context = new RuleContext(null, pCtx);

        List<RuleFinding> findings = rule.evaluate(context);
        assertFalse(findings.isEmpty(), "Expected IaC findings for Dockerfile");

        boolean hasRoot = findings.stream().anyMatch(f -> "CR-IAC-001".equals(f.ruleId()));
        boolean hasLatest = findings.stream().anyMatch(f -> "CR-IAC-002".equals(f.ruleId()));

        assertTrue(hasRoot, "Should flag running container as root (CR-IAC-001)");
        assertTrue(hasLatest, "Should flag untagged :latest base image (CR-IAC-002)");
    }

    @Test
    @DisplayName("Should detect privileged container in Kubernetes YAML")
    void testKubernetesPrivilegedContainer(@TempDir Path tempDir) throws IOException {
        Path k8sYaml = tempDir.resolve("deployment.yaml");
        Files.writeString(k8sYaml, """
                apiVersion: apps/v1
                kind: Deployment
                spec:
                  template:
                    spec:
                      containers:
                      - name: web
                        image: nginx:1.25
                        securityContext:
                          privileged: true
                """);

        PipelineContext pCtx = new PipelineContext(UUID.randomUUID(), tempDir);
        RuleContext context = new RuleContext(null, pCtx);

        List<RuleFinding> findings = rule.evaluate(context);
        assertTrue(findings.stream().anyMatch(f -> "CR-IAC-003".equals(f.ruleId())), "Should flag privileged: true (CR-IAC-003)");
    }
}
