package com.codexa.rules.universal;

import com.codexa.ai.mask.SecretMasker;
import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Universal Multi-Language rule scanner for TypeScript, JavaScript, Python, Go, C++, PHP, and Config files.
 */
@Component
public class UniversalMultiLanguageRule implements AnalysisRule {

    private static final Pattern EVAL_PATTERN = Pattern.compile("(?<![a-zA-Z0-9_.])(?:eval|Function)\\s*\\(");
    private static final Pattern CMD_EXEC_PATTERN = Pattern.compile("(?<![a-zA-Z0-9_.])(?:child_process\\.(?:exec|execSync)|os\\.system|subprocess\\.(?:call|Popen|run)\\s*\\([^,)]*shell\\s*=\\s*True)");
    private static final Pattern DOM_XSS_PATTERN = Pattern.compile("(?i)dangerouslySetInnerHTML|\\.innerHTML\\s*=|document\\.write\\s*\\(");
    private static final Pattern SENSITIVE_LOG_PATTERN = Pattern.compile("(?i)(?:console\\.log|console\\.info|print|logging\\.info)\\s*\\(.*?(?:password|token|secret|apiKey|api_key|jwt|private_key).*?\\)");
    private static final Pattern INSECURE_CORS_PATTERN = Pattern.compile("(?i)cors\\s*\\(\\s*\\{\\s*origin\\s*:\\s*[\"']\\*[\"']|Access-Control-Allow-Origin\\s*:\\s*\\*");

    @Override
    public String getRuleId() {
        return "CR-MULTI-001";
    }

    @Override
    public String getName() {
        return "Universal Multi-Language Security & Quality Rule";
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
        return "A03:2021-Injection";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        Path stagingDir = context.getStagingDirectory();

        if (stagingDir == null || !Files.exists(stagingDir)) {
            return findings;
        }

        try (Stream<Path> paths = Files.walk(stagingDir)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> scanFile(path, stagingDir, findings));
        } catch (IOException e) {
            // Ignore walk errors
        }

        return findings;
    }

    private void scanFile(Path file, Path stagingDir, List<RuleFinding> findings) {
        String filename = file.getFileName().toString().toLowerCase();
        
        // Exclude binary, git, and build artifacts
        if (filename.endsWith(".class") || filename.endsWith(".jar") || filename.endsWith(".png") ||
            filename.endsWith(".jpg") || filename.endsWith(".zip") || filename.endsWith(".lock")) {
            return;
        }

        String relPath = stagingDir.relativize(file).toString().replace("\\", "/");
        if (relPath.startsWith(".git") || relPath.contains("node_modules") || relPath.contains(".next") || relPath.contains("dist")) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                int lineNum = i + 1;

                // 1. Check DOM XSS (React / Next.js / Vanilla JS)
                if (DOM_XSS_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-XSS-001")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Unsafe DOM HTML injection (dangerouslySetInnerHTML / innerHTML)")
                            .description("Direct injection of raw HTML into the DOM without sanitization allows Cross-Site Scripting (XSS).")
                            .impact("Attackers can execute malicious JavaScript in user sessions to steal cookies, session tokens, or perform unauthorized actions.")
                            .remediation("Use safe JSX data-binding expressions or sanitize untrusted HTML using DOMPurify before injection.")
                            .owaspMapping("A03:2021-Injection")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("import DOMPurify from 'dompurify';\n<div dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(cleanHtml) }} />")
                            .references(List.of("https://owasp.org/Top10/A03_2021-Injection/"))
                            .build()
                    );
                }

                // 2. Check Dynamic Eval & Code Execution
                if (EVAL_PATTERN.matcher(line).find() || CMD_EXEC_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-CMD-001")
                            .category(Category.SECURITY)
                            .severity(Severity.CRITICAL)
                            .confidence(Confidence.HIGH)
                            .title("Dangerous dynamic code or process execution (eval / exec)")
                            .description("Dynamic execution of arbitrary code or system processes using 'eval', 'Function', or 'exec' enables Remote Code Execution (RCE).")
                            .impact("Total server or client compromise through arbitrary command execution.")
                            .remediation("Avoid 'eval' and shell execution. Use structured APIs and safe JSON parsers.")
                            .owaspMapping("A03:2021-Injection")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Replace eval with safe JSON.parse or strict lookup maps:\nconst data = JSON.parse(input);")
                            .references(List.of("https://owasp.org/Top10/A03_2021-Injection/"))
                            .build()
                    );
                }

                // 3. Check Sensitive Logging
                if (SENSITIVE_LOG_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-LOG-001")
                            .category(Category.SECURITY)
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.MEDIUM)
                            .title("Sensitive credential or token logged to console/output")
                            .description("Writing sensitive information (passwords, tokens, keys) to application logs exposes secrets to unauthorized viewers.")
                            .impact("Credential theft and access token exfiltration via log aggregation tools.")
                            .remediation("Mask or redact sensitive fields before logging.")
                            .owaspMapping("A09:2021-Security Logging & Monitoring")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("console.log('Action performed for user:', userId);")
                            .references(List.of("https://owasp.org/Top10/A09_2021-Security_Logging_and_Monitoring_Failures/"))
                            .build()
                    );
                }

                // 4. Check Insecure CORS
                if (INSECURE_CORS_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-CONFIG-001")
                            .category(Category.SECURITY)
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.HIGH)
                            .title("Permissive CORS policy allows all origins (*)")
                            .description("Setting 'Access-Control-Allow-Origin: *' allows any malicious third-party site to send authenticated requests to your API.")
                            .impact("Cross-site data theft and unauthorized cross-origin requests.")
                            .remediation("Explicitly whitelist trusted production domain origins.")
                            .owaspMapping("A05:2021-Security Misconfiguration")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("cors({ origin: ['https://yourdomain.com'] })")
                            .references(List.of("https://owasp.org/Top10/A05_2021-Security_Misconfiguration/"))
                            .build()
                    );
                }
            }
        } catch (Exception e) {
            // Ignore unreadable files
        }
    }
}
