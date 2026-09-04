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

    // 1. Dynamic Eval & RCE
    private static final Pattern EVAL_PATTERN = Pattern.compile("(?<![a-zA-Z0-9_.])(?:eval|Function)\\s*\\(");
    private static final Pattern CMD_EXEC_PATTERN = Pattern.compile("(?<![a-zA-Z0-9_.])(?:child_process\\.(?:exec|execSync)|os\\.system|subprocess\\.(?:call|Popen|run)\\s*\\([^,)]*shell\\s*=\\s*True)");

    // 2. DOM XSS
    private static final Pattern DOM_XSS_PATTERN = Pattern.compile("(?i)dangerouslySetInnerHTML|\\.innerHTML\\s*=|document\\.write\\s*\\(");

    // 3. Sensitive Logging
    private static final Pattern SENSITIVE_LOG_PATTERN = Pattern.compile("(?i)(?:console\\.log|console\\.info|print|logging\\.info|logger\\.info|log\\.info)\\s*\\(.*?(?:password|token|secret|apiKey|api_key|jwt|private_key|auth_header).*?\\)");

    // 4. Insecure CORS & Permissive Configs
    private static final Pattern INSECURE_CORS_PATTERN = Pattern.compile("(?i)cors\\s*\\(\\s*\\{\\s*origin\\s*:\\s*[\"']\\*[\"']|Access-Control-Allow-Origin\\s*:\\s*\\*");
    private static final Pattern TLS_VERIFY_DISABLED_PATTERN = Pattern.compile("(?i)rejectUnauthorized\\s*:\\s*false|verify\\s*=\\s*False|InsecureSkipVerify\\s*:\\s*true|NODE_TLS_REJECT_UNAUTHORIZED\\s*=\\s*['\"]?0['\"]?");

    // 5. Secrets & Fallback Secrets
    private static final Pattern FALLBACK_SECRET_PATTERN = Pattern.compile("(?i)(?:process\\.env\\.[A-Z0-9_]*(?:SECRET|KEY|PASSWORD|TOKEN)|os\\.getenv\\([\"'][A-Z0-9_]*(?:SECRET|KEY|PASSWORD)[\"']\\))\\s*\\|\\|\\s*[\"']([^\"']{4,})[\"']");
    private static final Pattern HARDCODED_SECRET_PATTERN = Pattern.compile("(?i)(?:const|let|var|String|val)\\s+(?:jwtSecret|api_?key|secretKey|auth_?token|app_?secret)\\s*=\\s*[\"'][a-zA-Z0-9_\\-+=]{8,}[\"']");
    private static final Pattern PEM_PRIVATE_KEY_PATTERN = Pattern.compile("-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----");
    private static final Pattern DB_CONN_STRING_SECRET_PATTERN = Pattern.compile("(?:mongodb(?:\\+srv)?|postgres(?:ql)?|mysql|redis):\\/\\/[a-zA-Z0-9_.-]+:[a-zA-Z0-9_.-]+@[a-zA-Z0-9_.-]+");

    // 6. SQL Injection in Multi-Language code
    private static final Pattern MULTI_SQL_INJECTION_PATTERN = Pattern.compile("(?i)(?:db|client|pool|connection|cursor|conn|knex|sequelize)\\.(?:query|raw|execute|executeRaw)\\s*\\(\\s*(?:`[^`]*\\$\\{[^}]+\\}[^`]*`|f[\"'][^\"']*\\{[^}]+\\}[^\"']*[\"']|[\"'][^\"']*(?:SELECT|INSERT|UPDATE|DELETE)[^\"']*[\"']\\s*\\+)");

    // 7. Path Traversal & Unvalidated File Operations
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(?i)(?:fs\\.(?:readFile|readFileSync|createReadStream|writeFile|writeFileSync)|open|FileInputStream|file_get_contents)\\s*\\(\\s*(?:req\\.(?:query|params|body)|request\\.(?:args|GET|POST|form)|\".*\"\\s*\\+\\s*(?:req|request|input|param))");

    // 8. Server-Side Request Forgery (SSRF)
    private static final Pattern SSRF_PATTERN = Pattern.compile("(?i)(?:axios\\.(?:get|post|put|delete|request)|fetch|urllib\\.request\\.urlopen|requests\\.(?:get|post)|http\\.get)\\s*\\(\\s*(?:req\\.(?:query|params|body|url)|request\\.(?:args|GET|POST)|userUrl|targetUrl|inputUrl)");

    // 9. ReDoS (Catastrophic Regular Expression Backtracking)
    private static final Pattern REDOS_PATTERN = Pattern.compile("RegExp\\s*\\([\"'][^\"']*(?:\\([^)]+\\+\\)\\+|\\([^)]+\\*\\)\\*|\\([^)]+\\+\\)\\*)[^\"']*[\"']\\)|/(?:\\([^)]+\\+\\)\\+|\\([^)]+\\*\\)\\*|\\([^)]+\\+\\)\\*)/");

    // 10. Insecure Randomness in Security Contexts
    private static final Pattern INSECURE_RANDOM_PATTERN = Pattern.compile("(?i)(?:token|secret|password|nonce|otp|auth|salt|key)\\s*[:=].*?(?:Math\\.random\\(\\)|random\\.random\\(\\)|rand\\.Int\\(\\))");

    // 11. Insecure Deserialization
    private static final Pattern INSECURE_DESERIALIZATION_PATTERN = Pattern.compile("(?:pickle\\.loads|yaml\\.load\\s*\\([^,)]*Loader\\s*=\\s*yaml\\.Loader|unserialize\\s*\\(|java\\.io\\.ObjectInputStream)");

    // 12. Quality & Maintainability
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
            filename.endsWith(".jpg") || filename.endsWith(".zip") || filename.endsWith(".lock") ||
            filename.endsWith(".svg") || filename.endsWith(".ico") || filename.endsWith(".woff2")) {
            return;
        }

        String relPath = stagingDir.relativize(file).toString().replace("\\", "/");
        if (relPath.startsWith(".git") || relPath.contains("node_modules") || relPath.contains(".next") || relPath.contains("dist") || relPath.contains("build")) {
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

                // 3. Check Multi-Language SQL Injection
                if (MULTI_SQL_INJECTION_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-SQL-002")
                            .category(Category.SECURITY)
                            .severity(Severity.CRITICAL)
                            .confidence(Confidence.HIGH)
                            .title("Multi-Language dynamic SQL injection via string template interpolation")
                            .description("Dynamic SQL query construction detected using template literals or string interpolation inside database query calls.")
                            .impact("Database breach, unauthorized modification, authentication bypass, or data exfiltration.")
                            .remediation("Use parameterized queries or prepared statements ($1, ?, :param). Never interpolate user input directly into queries.")
                            .owaspMapping("A03:2021-Injection")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Secure parameterized query:\nconst result = await db.query('SELECT * FROM users WHERE id = $1', [userId]);")
                            .references(List.of("https://owasp.org/Top10/A03_2021-Injection/"))
                            .build()
                    );
                }

                // 4. Check Path Traversal
                if (PATH_TRAVERSAL_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-PATH-001")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Unrestricted Path Traversal in File Access Operation")
                            .description("User-controlled inputs passed directly to file read/write operations can allow attackers to traverse directory boundaries (e.g. '../../etc/passwd').")
                            .impact("Unauthorized access to sensitive server configuration files, private keys, or arbitrary file overwrite.")
                            .remediation("Sanitize input paths with path.normalize(), path.resolve(), and verify the target path stays within the designated root directory.")
                            .owaspMapping("A01:2021-Broken Access Control")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("const safePath = path.resolve(BASE_DIR, path.normalize(req.query.file));\nif (!safePath.startsWith(BASE_DIR)) throw new Error('Access Denied');")
                            .references(List.of("https://owasp.org/Top10/A01_2021-Broken_Access_Control/"))
                            .build()
                    );
                }

                // 5. Check SSRF (Server-Side Request Forgery)
                if (SSRF_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-SSRF-001")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Server-Side Request Forgery (SSRF) via unvalidated HTTP request")
                            .description("Outgoing HTTP requests constructed using untrusted user input allow attackers to probe internal network endpoints or cloud metadata services (169.254.169.254).")
                            .impact("Access to cloud instance metadata, internal microservices, and network scan reconnaissance.")
                            .remediation("Validate requested URLs against an allowlist of trusted domains and block loopback/private IP ranges (127.0.0.1, 10.0.0.0/8, 169.254.169.254).")
                            .owaspMapping("A10:2021-Server-Side Request Forgery")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("const parsed = new URL(userUrl);\nif (!ALLOWED_HOSTS.includes(parsed.hostname)) throw new Error('Disallowed Host');")
                            .references(List.of("https://owasp.org/Top10/A10_2021-Server-Side_Request_Forgery_%28SSRF%29/"))
                            .build()
                    );
                }

                // 6. Check Insecure Cryptographic Keys & Certificates
                if (PEM_PRIVATE_KEY_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-CRYPTO-002")
                            .category(Category.SECURITY)
                            .severity(Severity.CRITICAL)
                            .confidence(Confidence.HIGH)
                            .title("Hardcoded Private Key / Certificate Block detected in repository")
                            .description("An unencrypted private cryptographic key (RSA/EC/SSH) is hardcoded in source control.")
                            .impact("Complete compromise of TLS integrity, digital signatures, or server SSH access.")
                            .remediation("Remove private keys from repository history immediately and inject keys via AWS Secrets Manager, Vault, or secure environment variables.")
                            .owaspMapping("A02:2021-Cryptographic Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Load key securely at runtime from secrets vault:\nconst privateKey = await secretsClient.getSecret('APP_PRIVATE_KEY');")
                            .references(List.of("https://owasp.org/Top10/A02_2021-Cryptographic_Failures/"))
                            .build()
                    );
                }

                // 7. Check Database Connection String with Credentials
                if (DB_CONN_STRING_SECRET_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-SEC-003")
                            .category(Category.SECURITY)
                            .severity(Severity.CRITICAL)
                            .confidence(Confidence.HIGH)
                            .title("Hardcoded Database Connection URI with embedded credentials")
                            .description("A database connection string containing embedded plaintext username and password was found in source code.")
                            .impact("Direct database access by unauthorized parties resulting in data loss or exfiltration.")
                            .remediation("Move database credentials to secure environment variables (.env not committed) or a managed secret store.")
                            .owaspMapping("A07:2021-Identification and Authentication Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("const dbUrl = process.env.DATABASE_URL;\nif (!dbUrl) throw new Error('DATABASE_URL environment variable missing');")
                            .references(List.of("https://owasp.org/Top10/A07_2021-Identification_and_Authentication_Failures/"))
                            .build()
                    );
                }

                // 8. Check Disabled TLS Verification
                if (TLS_VERIFY_DISABLED_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-CONFIG-002")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Disabled SSL/TLS Certificate Verification (rejectUnauthorized: false)")
                            .description("Disabling TLS verification or setting rejectUnauthorized: false disables certificate validation, allowing Man-in-the-Middle (MitM) attacks.")
                            .impact("Attackers on the network path can intercept and modify sensitive HTTPS communications in transit.")
                            .remediation("Enable strict certificate validation and install valid root CA certificates instead of disabling verification.")
                            .owaspMapping("A02:2021-Cryptographic Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("const agent = new https.Agent({ rejectUnauthorized: true });")
                            .references(List.of("https://owasp.org/Top10/A02_2021-Cryptographic_Failures/"))
                            .build()
                    );
                }

                // 9. Check Insecure Deserialization
                if (INSECURE_DESERIALIZATION_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-DESER-001")
                            .category(Category.SECURITY)
                            .severity(Severity.CRITICAL)
                            .confidence(Confidence.HIGH)
                            .title("Unsafe Object Deserialization (pickle / unsafe yaml)")
                            .description("Deserializing untrusted data with pickle or unconstrained YAML loaders allows arbitrary Python code execution.")
                            .impact("Remote Code Execution (RCE) on the host machine during payload unpacking.")
                            .remediation("Use safe serialization formats like JSON (json.loads) or yaml.safe_load.")
                            .owaspMapping("A08:2021-Software and Data Integrity Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("import yaml\ndata = yaml.safe_load(untrusted_stream)")
                            .references(List.of("https://owasp.org/Top10/A08_2021-Software_and_Data_Integrity_Failures/"))
                            .build()
                    );
                }

                // 10. Check Insecure Randomness
                if (INSECURE_RANDOM_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-RAND-001")
                            .category(Category.SECURITY)
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.HIGH)
                            .title("Cryptographically Weak Pseudo-Random Number Generator (PRNG)")
                            .description("Using Math.random() or random.random() for security tokens or keys generates predictable values.")
                            .impact("Predictable tokens enable session hijacking, password reset hijacking, or cryptographic forgery.")
                            .remediation("Use a cryptographically secure random number generator (e.g. crypto.randomBytes() in Node.js or secrets in Python).")
                            .owaspMapping("A02:2021-Cryptographic Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("import crypto from 'crypto';\nconst token = crypto.randomBytes(32).toString('hex');")
                            .references(List.of("https://owasp.org/Top10/A02_2021-Cryptographic_Failures/"))
                            .build()
                    );
                }

                // 11. Check ReDoS
                if (REDOS_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-REDOS-001")
                            .category(Category.QUALITY)
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.MEDIUM)
                            .title("Catastrophic Regular Expression Backtracking (ReDoS)")
                            .description("Nested or overlapping quantifiers in regular expressions cause exponential backtracking on non-matching inputs.")
                            .impact("CPU exhaustion leading to Denial of Service (DoS) for all application threads.")
                            .remediation("Simplify regex quantifiers, avoid nested repetition groups, or use non-backtracking regex engines.")
                            .owaspMapping("A04:2021-Insecure Design")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Refactor regex to avoid overlapping quantifiers:\nconst safeRegex = /^[a-zA-Z0-9_-]+$/;")
                            .references(List.of("https://owasp.org/www-community/attacks/Regular_expression_Denial_of_Service_-_ReDoS"))
                            .build()
                    );
                }

                // 12. Check Sensitive Logging
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

                // 13. Check Insecure CORS
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

                // 14. Check Fallback or Hardcoded Secrets
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

                // 15. Check Empty / Swallowed Catch Blocks (Code Quality)
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

                // 16. Check Console Debug Statements in Production Source (Code Quality)
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

                // 17. Check TODO / Technical Debt markers (Maintainability)
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
