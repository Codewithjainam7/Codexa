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
    private static final Pattern FALLBACK_SECRET_PATTERN = Pattern.compile("(?i)(?:process\\.env\\.[A-Z0-9_]*(?:SECRET|KEY|PASSWORD|TOKEN)|os\\.getenv\\([\"'][A-Z0-9_]*(?:SECRET|KEY|PASSWORD)[\"']\\))\\s*\\|\\|\\s*[\"']([^\"']{4,})[\"']");
    private static final Pattern HARDCODED_SECRET_PATTERN = Pattern.compile("(?i)(?:const|let|var|String|val)\\s+(?:jwtSecret|api_?key|secretKey|auth_?token|app_?secret)\\s*=\\s*[\"'][a-zA-Z0-9_\\-+=]{8,}[\"']");
    private static final Pattern EMPTY_CATCH_PATTERN = Pattern.compile("(?:catch\\s*\\([a-zA-Z0-9_\\s]*\\)|except(?:\\s+[a-zA-Z0-9_]+)?\\s*:)\\s*\\{\\s*\\}");
    private static final Pattern DEBUG_CONSOLE_PATTERN = Pattern.compile("(?<![a-zA-Z0-9_.])console\\.(?:log|debug|trace)\\s*\\(");
    private static final Pattern TECH_DEBT_PATTERN = Pattern.compile("(?:\\/\\/|#|\\/\\*)\\s*(?:TODO|FIXME|HACK|XXX):?\\s*(.+)");

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

                // 5. Check Fallback or Hardcoded Secrets
                if (FALLBACK_SECRET_PATTERN.matcher(line).find() || HARDCODED_SECRET_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-SEC-002")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Hardcoded fallback secret or authentication token detected")
                            .description("Defaulting to a hardcoded string when an environment variable is missing allows attackers to bypass cryptographic signatures or forge session tokens.")
                            .impact("Session token forgery, authentication bypass, and sensitive data decryption.")
                            .remediation("Enforce strict secret loading and throw a startup configuration error when required secret environment variables are missing.")
                            .owaspMapping("A07:2021-Identification and Authentication Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("const secret = process.env.SESSION_SECRET;\nif (!secret) throw new Error('SESSION_SECRET is required in production');")
                            .references(List.of("https://owasp.org/Top10/A07_2021-Identification_and_Authentication_Failures/"))
                            .build()
                    );
                }

                // 6. Check Empty / Swallowed Catch Blocks (Code Quality)
                if (EMPTY_CATCH_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-QUAL-001")
                            .category(Category.QUALITY)
                            .severity(Severity.LOW)
                            .confidence(Confidence.HIGH)
                            .title("Swallowed exception with empty catch block")
                            .description("Silently swallowing exceptions prevents proper error recovery and hides critical runtime failures.")
                            .impact("Application fails silently without diagnostic logs, leading to unpredictable system behavior.")
                            .remediation("Log the exception or re-throw an appropriate domain error.")
                            .owaspMapping("A09:2021-Security Logging & Monitoring")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("catch (error) {\n  logger.error('Operation failed', error);\n  throw error;\n}")
                            .references(List.of("https://cwe.mitre.org/data/definitions/390.html"))
                            .build()
                    );
                }

                // 7. Check Console Debug Statements in Production Source (Code Quality)
                if (DEBUG_CONSOLE_PATTERN.matcher(line).find() && !SENSITIVE_LOG_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-QUAL-002")
                            .category(Category.QUALITY)
                            .severity(Severity.INFO)
                            .confidence(Confidence.HIGH)
                            .title("Production console logging statement")
                            .description("Leaving console.log/console.debug in production code pollutes browser and server logs.")
                            .impact("Cluttered logs, slight performance penalty, and accidental exposure of internal data structures.")
                            .remediation("Use a structured logger with configurable log levels or strip console logs in production builds.")
                            .owaspMapping("A09:2021-Security Logging & Monitoring")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Use a structured logger:\nlogger.debug('State:', state);")
                            .references(List.of("https://eslint.org/docs/latest/rules/no-console"))
                            .build()
                    );
                }

                // 8. Check TODO / Technical Debt markers (Maintainability)
                var matcher = TECH_DEBT_PATTERN.matcher(line);
                if (matcher.find()) {
                    String debtItem = matcher.group(1).trim();
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-MAINT-001")
                            .category(Category.QUALITY)
                            .severity(Severity.INFO)
                            .confidence(Confidence.MEDIUM)
                            .title("Technical Debt / Unresolved TODO: " + (debtItem.length() > 60 ? debtItem.substring(0, 57) + "..." : debtItem))
                            .description("Unresolved TODO/FIXME comments indicate incomplete features or pending maintenance tasks.")
                            .impact("Unfinished code paths or potential latent bugs in edge cases.")
                            .remediation("Resolve the pending item or track it in your issue management backlog.")
                            .owaspMapping("A04:2021-Insecure Design")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Resolved item or link to tracking issue #123")
                            .references(List.of("https://cwe.mitre.org/data/definitions/546.html"))
                            .build()
                    );
                }
            }
        } catch (Exception e) {
            // Ignore unreadable files
        }
    }
}
