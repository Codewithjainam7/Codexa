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
    private static final Pattern FALLBACK_SECRET_PATTERN = Pattern.compile("(?i)(?:process\\.env|import\\.meta\\.env)\\.[A-Z0-9_]*(?:SECRET|KEY|PASSWORD|TOKEN|API|MAILTRAP|RESEND|SENDGRID|GEMINI|OPENAI|INBOX|CLIENT_ID|AUTH)[A-Z0-9_]*\\s*(?:\\|\\|\\s*['\"]([^'\"]{4,})['\"]|\\?\\?\\s*['\"]([^'\"]{4,})['\"])");
    private static final Pattern HARDCODED_SECRET_PATTERN = Pattern.compile("(?i)(?:const|let|var|String|val)\\s+(?:jwtSecret|api_?key|secretKey|auth_?token|app_?secret|mailtrap_?token|mailtrapToken|token|apiKey|apiSecret|secretToken)\\s*=\\s*[\"'][a-zA-Z0-9_\\-+=]{8,}[\"']");
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

    // 10. Insecure Randomness in Security Contexts (Weak PINs & PRNG)
    private static final Pattern INSECURE_RANDOM_PATTERN = Pattern.compile(
            "(?i)(?:const|let|var)?\\s*[a-zA-Z0-9_]*(?:token|secret|password|nonce|otp|auth|salt|key|pin|code|accessPin|randomPin|passcode|invite|magic)\\w*\\s*[:=].*?Math\\.random"
    );
    private static final Pattern PREDICTABLE_TIMESTAMP_TOKEN_PATTERN = Pattern.compile(
            "(?i)(?:const|let|var)?\\s*[a-zA-Z0-9_]*(?:token|invite|magic|access|auth|code|session|link)\\w*\\s*[:=].*?(?:`[^`]*\\$\\{.*?Date\\.now\\(\\).*?\\}[^`]*`|Date\\.now\\(\\))"
    );
    private static final Pattern SECURITY_FN_CONTEXT_PATTERN = Pattern.compile(
            "(?i)(?:function|const|let|var|def)\\s+[a-zA-Z0-9_]*(?:token|secret|password|nonce|otp|auth|salt|key|pin|code|access|passcode|invite|magic)"
    );

    // 11. Insecure Deserialization
    private static final Pattern INSECURE_DESERIALIZATION_PATTERN = Pattern.compile("(?:pickle\\.loads|yaml\\.load\\s*\\([^,)]*Loader\\s*=\\s*yaml\\.Loader|unserialize\\s*\\(|java\\.io\\.ObjectInputStream)");

    // 12. Frontend Hardcoded Admin Authentication
    private static final Pattern HARDCODED_ADMIN_AUTH_PATTERN = Pattern.compile(
            "(?i)(?:[a-zA-Z0-9_.]*(?:adminId|username|user_?name|login|email)\\s*===?\\s*['\"][^'\"\\s]{2,}['\"]\\s*&&\\s*[a-zA-Z0-9_.]*(?:password|pass|secret|pin|adminKey)\\s*===?\\s*['\"][^'\"\\s]{2,}['\"]" +
            "|[a-zA-Z0-9_.]*(?:password|pass|secret|pin|adminKey)\\s*===?\\s*['\"][^'\"\\s]{2,}['\"]\\s*&&\\s*[a-zA-Z0-9_.]*(?:adminId|username|user_?name|login|email)\\s*===?\\s*['\"][^'\"\\s]{2,}['\"])"
    );

    // 13. Client-Side Secret Leak via Build Prefixes
    private static final Pattern CLIENT_SIDE_SECRET_LEAK_PATTERN = Pattern.compile(
            "(?i)(?:import\\.meta\\.env|process\\.env)\\.(?:VITE|NEXT_PUBLIC|REACT_APP|EXPO_PUBLIC)_[A-Z0-9_]*(?:SECRET|PRIVATE|API_TOKEN|MAILTRAP|RESEND|SENDGRID|GEMINI|OPENAI|ANTHROPIC|STRIPE_SECRET|DATABASE_URL|SERVICE_ROLE|ADMIN_KEY)"
    );
    private static final Pattern CLIENT_PREFIXED_SECRET_ASSIGN_PATTERN = Pattern.compile(
            "(?i)(?:VITE|NEXT_PUBLIC|REACT_APP|EXPO_PUBLIC)_[A-Z0-9_]*(?:SECRET|PRIVATE|API_TOKEN|MAILTRAP|RESEND|SENDGRID|GEMINI|OPENAI|ANTHROPIC|STRIPE_SECRET|SERVICE_ROLE|ADMIN_KEY)\\s*=\\s*['\"][^'\"\\s]{8,}['\"]"
    );

    // 14. Dev Server Middleware API Route (Vite configureServer 404 trap)
    private static final Pattern DEV_MIDDLEWARE_API_PATTERN = Pattern.compile(
            "(?i)(?:server\\.middlewares\\.use\\s*\\(|(?:req\\.(?:url|originalUrl)|url)\\s*(?:===?|\\?\\.startsWith|\\.startsWith|\\?\\.includes|\\.includes)\\s*\\(?\\s*['\"]/api/[a-zA-Z0-9_.-]*|req\\.url\\s*&&\\s*req\\.url\\.includes\\s*\\(\\s*['\"]/api/)"
    );

    // 15. Real Supabase / Edge Function Authorization Check
    private static final Pattern REAL_EDGE_AUTH_PATTERN = Pattern.compile(
            "(?i)(?:headers\\.get\\s*\\(\\s*['\"]authorization['\"]|auth\\.getUser|auth\\.getSession|verifyUser|verifyJwt|jwt\\.verify|supabaseClient\\.auth)"
    );

    // 16. Hardcoded Signed JWT Tokens in scripts or source
    private static final Pattern JWT_TOKEN_PATTERN = Pattern.compile(
            "eyJhbGciOi[a-zA-Z0-9_-]{10,}\\.[a-zA-Z0-9_-]{10,}\\.[a-zA-Z0-9_-]{10,}"
    );

    // 17. Insecure Database Scripts (RLS Disabled / Permissive Policy)
    private static final Pattern INSECURE_RLS_DISABLE_PATTERN = Pattern.compile(
            "(?i)(?:DISABLE\\s+ROW\\s+LEVEL\\s+SECURITY|CREATE\\s+POLICY.*?(?:USING|WITH\\s+CHECK)\\s*\\(\\s*true\\s*\\)|(?:USING|WITH\\s+CHECK)\\s*\\(\\s*true\\s*\\))"
    );

    // 18. Parameter Security & Input Validation Patterns
    // 18a. Mass Assignment / DTO Over-Posting
    private static final Pattern MASS_ASSIGNMENT_PATTERN = Pattern.compile(
            "(?i)(?:\\.(?:create|update|insert|save|upsert)\\s*\\(\\s*req\\.body|data\\s*:\\s*req\\.body|\\.\\.\\.req\\.body|prisma\\.[a-zA-Z0-9_]+\\.(?:create|update)\\s*\\(\\s*\\{\\s*data:\\s*req\\.body|supabase\\.from\\([^)]+\\)\\.(?:insert|update|upsert)\\s*\\(\\s*req\\.body)"
    );

    // 18b. IDOR on Resource Parameters
    private static final Pattern IDOR_PARAM_QUERY_PATTERN = Pattern.compile(
            "(?i)(?:(?:find(?:ByPk|ById|Unique|One)|delete(?:ById)?|remove(?:ById)?|findByIdAndDelete|findByIdAndUpdate)\\s*\\(\\s*(?:req\\.params|params)\\.[a-zA-Z0-9_]+|\\.eq\\s*\\(\\s*['\"]id['\"]\\s*,\\s*(?:req\\.params|params)\\.[a-zA-Z0-9_]+\\s*\\))"
    );

    // 18c. Open Redirect via Parameters
    private static final Pattern OPEN_REDIRECT_PARAM_PATTERN = Pattern.compile(
            "(?i)(?:res\\.redirect\\s*\\(\\s*(?:req\\.(?:query|params)|params|searchParams\\.get)\\.[a-zA-Z0-9_.]*(?:url|redirect|return|next|target|dest|goto|callback)|window\\.location\\.(?:href|replace|assign)\\s*=\\s*(?:new\\s+URLSearchParams\\([^)]*\\)\\.get|params\\.get|searchParams\\.get)\\s*\\(\\s*['\"](?:url|redirect|return|next|target|dest|goto|callback)['\"]|return\\s+['\"]redirect:\\s*['\"]\\s*\\+\\s*(?:redirectUrl|targetUrl|url|returnUrl))"
    );

    // 18d. Unbounded Pagination Parameter (DoS)
    private static final Pattern UNBOUNDED_PAGINATION_PATTERN = Pattern.compile(
            "(?i)(?:\\.(?:limit|take)\\s*\\(\\s*(?:parseInt|Number)?\\s*\\(?\\s*req\\.query\\.(?:limit|size|pageSize|count)|(?:const|let|var)\\s+[a-zA-Z0-9_]*(?:limit|size|pageSize)\\s*=\\s*(?:parseInt|Number)\\s*\\(\\s*(?:req\\.query\\.(?:limit|size|pageSize)|params\\.(?:limit|size))\\s*(?:\\|\\|\\s*\\d+)?\\s*\\)\\s*;?\\s*(?!.*Math\\.min))"
    );

    // 18e. Prototype Pollution via Parameter Merging
    private static final Pattern PROTOTYPE_POLLUTION_PATTERN = Pattern.compile(
            "(?i)(?:Object\\.assign\\s*\\(\\s*[^,)]+\\s*,\\s*req\\.(?:body|query|params)|(?:lodash|_)\\.(?:merge|extend|defaultsDeep)\\s*\\(\\s*[^,)]+\\s*,\\s*req\\.(?:body|query|params)|for\\s*\\(\\s*(?:const|let|var)?\\s*[a-zA-Z0-9_]+\\s+in\\s+req\\.(?:body|query)\\s*\\)\\s*\\{\\s*[a-zA-Z0-9_.]+\\[[a-zA-Z0-9_]+\\]\\s*=)"
    );

    // 19. Quality & Maintainability
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

        List<String> lines = readFileLines(file);
        if (lines.isEmpty()) {
            return;
        }

        String entireFileContent = String.join("\n", lines);

        // Edge Function Auth Check (Supabase / Deno / Edge workers)
        boolean isEdgeFunction = (relPath.contains("supabase/functions/") || relPath.contains("edge-functions/"))
                && (filename.endsWith(".ts") || filename.endsWith(".js"));
        if (isEdgeFunction) {
            boolean handlesRequests = entireFileContent.contains("serve(") || entireFileContent.contains("Deno.serve(") || entireFileContent.contains("req") || entireFileContent.contains("Request");
            boolean performsAction = entireFileContent.contains("json(") || entireFileContent.contains("fetch(") || entireFileContent.contains("email") ||
                    entireFileContent.contains("send") || entireFileContent.contains("resend") || entireFileContent.contains("mailtrap") ||
                    entireFileContent.contains("insert") || entireFileContent.contains("update") || entireFileContent.contains("delete");
            boolean checksAuth = REAL_EDGE_AUTH_PATTERN.matcher(entireFileContent).find();

            if (handlesRequests && performsAction && !checksAuth) {
                int targetLine = 1;
                for (int j = 0; j < lines.size(); j++) {
                    String l = lines.get(j);
                    if (l.contains("serve(") || l.contains("Deno.serve(") || l.contains("export default")) {
                        targetLine = j + 1;
                        break;
                    }
                }
                findings.add(RuleFinding.builder()
                        .ruleId("CR-EDGE-001")
                        .category(Category.SECURITY)
                        .severity(Severity.HIGH)
                        .confidence(Confidence.HIGH)
                        .title("Supabase Edge Function missing authorization verification (unauthenticated open relay)")
                        .description("Supabase Edge Function handles incoming requests and performs sensitive operations (such as sending emails or database mutations) without verifying the caller's JWT or Authorization header. Any anonymous caller can invoke this endpoint directly.")
                        .impact("Unauthenticated actors on the public internet can abuse this endpoint to send spam emails, exhaust API quotas, or trigger backend mutations without authorization.")
                        .remediation("Verify the caller's JWT from the Authorization header using supabaseClient.auth.getUser() before executing privileged logic.")
                        .owaspMapping("A01:2021-Broken Access Control")
                        .filePath(relPath)
                        .startLine(targetLine)
                        .endLine(targetLine)
                        .evidence(SecretMasker.maskSecrets(lines.get(targetLine - 1).trim()))
                        .suggestedFix("const authHeader = req.headers.get('Authorization');\nif (!authHeader) return new Response('Unauthorized', { status: 401 });\nconst { data: { user }, error } = await supabaseClient.auth.getUser(authHeader.replace('Bearer ', ''));\nif (error || !user) return new Response('Unauthorized', { status: 401 });")
                        .references(List.of("https://supabase.com/docs/guides/functions/auth"))
                        .build()
                );
            }
        }

        boolean isDevConfig = relPath.contains("vite.config") || relPath.contains("webpack.config")
                || entireFileContent.contains("configureServer") || entireFileContent.contains("server.middlewares");

        try {
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

                // 10. Check Insecure Randomness (Math.random in security/PIN contexts)
                boolean isRandomPin = INSECURE_RANDOM_PATTERN.matcher(line).find();
                if (!isRandomPin && line.contains("Math.random")) {
                    for (int k = Math.max(0, i - 10); k < i; k++) {
                        if (SECURITY_FN_CONTEXT_PATTERN.matcher(lines.get(k)).find()) {
                            isRandomPin = true;
                            break;
                        }
                    }
                }
                if (isRandomPin) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-RAND-001")
                            .category(Category.SECURITY)
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.HIGH)
                            .title("Cryptographically Weak Pseudo-Random Number Generator (PRNG)")
                            .description("Using Math.random() or random.random() for security tokens, access PINs, or keys generates predictable values.")
                            .impact("Predictable PINs and tokens enable unauthorized access, brute-force attacks, session hijacking, or cryptographic forgery.")
                            .remediation("Use a cryptographically secure random number generator (e.g. crypto.getRandomValues() in browsers, crypto.randomBytes() in Node.js, or secrets in Python).")
                            .owaspMapping("A02:2021-Cryptographic Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Cryptographically secure random PIN:\nconst pin = crypto.getRandomValues(new Uint32Array(1))[0] % 9000 + 1000;")
                            .references(List.of("https://owasp.org/Top10/A02_2021-Cryptographic_Failures/"))
                            .build()
                    );
                }

                // 10b. Check Predictable Timestamp Tokens (Date.now())
                boolean isTimestampToken = PREDICTABLE_TIMESTAMP_TOKEN_PATTERN.matcher(line).find();
                if (!isTimestampToken && line.contains("Date.now()")) {
                    for (int k = Math.max(0, i - 10); k < i; k++) {
                        if (SECURITY_FN_CONTEXT_PATTERN.matcher(lines.get(k)).find()) {
                            isTimestampToken = true;
                            break;
                        }
                    }
                }
                if (isTimestampToken) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-RAND-002")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Predictable security token generated using Date.now() timestamp")
                            .description("Security token, magic link, or invite token generated using Date.now() without cryptographic randomness. Timestamp-based tokens are completely predictable within small millisecond windows.")
                            .impact("Attackers can predict or brute-force valid tokens, bypass invitation flows, or gain unauthorized access to protected resources.")
                            .remediation("Use a cryptographically secure random token generator, such as crypto.randomUUID() or crypto.getRandomValues().")
                            .owaspMapping("A02:2021-Cryptographic Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("const token = `tok_${payload.schemeId}_${crypto.randomUUID()}`;")
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

                // 14b. Check Frontend Hardcoded Admin Authentication (Bypass)
                if (HARDCODED_ADMIN_AUTH_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-AUTH-002")
                            .category(Category.SECURITY)
                            .severity(Severity.CRITICAL)
                            .confidence(Confidence.HIGH)
                            .title("Hardcoded Administrator Credentials in Client-Side Code")
                            .description("Hardcoded administrator authentication logic comparing credentials to plaintext string literals directly in client-side code allows trivial authentication bypass.")
                            .impact("Attackers can inspect the client-side bundle to extract administrator credentials and gain unauthorized super-admin privileges.")
                            .remediation("Authenticate administrators against a secure backend API using bcrypt/Argon2 password hashing and secure HTTP-only session cookies or signed JWTs. Never verify passwords directly in frontend code.")
                            .owaspMapping("A07:2021-Identification and Authentication Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Authenticate via backend API endpoint:\nconst response = await api.post('/auth/admin/login', { adminId, password });")
                            .references(List.of("https://owasp.org/Top10/A07_2021-Identification_and_Authentication_Failures/"))
                            .build()
                    );
                }

                // 14c. Check Client-Side Secret Leak via Build Prefixes (VITE_, NEXT_PUBLIC_, REACT_APP_, EXPO_PUBLIC_)
                if (CLIENT_SIDE_SECRET_LEAK_PATTERN.matcher(line).find() || CLIENT_PREFIXED_SECRET_ASSIGN_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-LEAK-001")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Client-Exposed Private Service Secret via Build Prefix")
                            .description("Sensitive service secret or private API token accessed or assigned with a client build prefix (VITE_, NEXT_PUBLIC_, REACT_APP_, or EXPO_PUBLIC_). Any environment variable with these prefixes is bundled directly into public client-side JavaScript.")
                            .impact("Anyone inspecting public JavaScript bundles can steal third-party service credentials (e.g. Gemini, Mailtrap, OpenAI, Stripe), deplete API quotas, or access restricted services.")
                            .remediation("Remove the client prefix and proxy requests through a secure server-side backend or serverless edge function that keeps secrets hidden on the server.")
                            .owaspMapping("A01:2021-Broken Access Control")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Proxy third-party API calls via backend server instead of client-side:\nconst response = await fetch('/api/services/dispatch', { method: 'POST', body: JSON.stringify(payload) });")
                            .references(List.of("https://owasp.org/Top10/A01_2021-Broken_Access_Control/"))
                            .build()
                    );
                }

                // 14d. Check Dev Server Middleware API Route (Vite configureServer 404 trap)
                if (isDevConfig && DEV_MIDDLEWARE_API_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-ARCH-001")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Production API endpoint defined in dev server middleware (configureServer)")
                            .description("API route handling ('/api/...') is implemented inside Vite/dev-server 'configureServer' middleware. This middleware only executes during local 'vite dev' and is completely omitted during production builds ('vite build'), causing all production API requests to 404.")
                            .impact("Production functionality breaks entirely (HTTP 404 Not Found on API endpoints in production), and dev middleware may expose sensitive logic locally.")
                            .remediation("Move API endpoints to a dedicated production backend service (Express, Spring Boot, Fastify) or serverless cloud functions (Supabase Edge Functions, Vercel Functions).")
                            .owaspMapping("A05:2021-Security Misconfiguration")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Move API routes to dedicated backend server or serverless functions.\n// In vite.config.ts, only configure proxies for production targets:\nserver: { proxy: { '/api': 'http://localhost:8080' } }")
                            .references(List.of("https://vitejs.dev/guide/api-plugin.html#configureserver"))
                            .build()
                    );
                }

                // 14e. Check Hardcoded Signed JWT Tokens in scripts or source
                if (JWT_TOKEN_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-SEC-013")
                            .category(Category.SECURITY)
                            .severity(Severity.CRITICAL)
                            .confidence(Confidence.HIGH)
                            .title("Hardcoded JSON Web Token (JWT) or Service Role Key in script")
                            .description("A signed JWT bearer token (such as a Supabase service role key, anon token, or user session JWT) was found hardcoded in source control or script.")
                            .impact("Anyone with read access to the repository can use this token to authenticate against APIs or bypass database Row Level Security.")
                            .remediation("Revoke the exposed key immediately in your service dashboard and inject credentials via environment variables.")
                            .owaspMapping("A07:2021-Identification and Authentication Failures")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE_KEY);")
                            .references(List.of("https://owasp.org/Top10/A07_2021-Identification_and_Authentication_Failures/"))
                            .build()
                    );
                }

                // 14f. Check Insecure Database Scripts Disabling Row Level Security (RLS)
                if (INSECURE_RLS_DISABLE_PATTERN.matcher(line).find()) {
                    int startLine = lineNum;
                    int endLine = lineNum;
                    String evidenceText = line.trim();

                    // If line is a multi-line policy clause (e.g. USING (true) or WITH CHECK (true)), trace backwards to find CREATE POLICY
                    if (!line.toUpperCase().contains("CREATE POLICY") && !line.toUpperCase().contains("DISABLE")) {
                        for (int k = i - 1; k >= Math.max(0, i - 10); k--) {
                            String prev = lines.get(k).trim();
                            if (prev.toUpperCase().contains("CREATE POLICY")) {
                                startLine = k + 1;
                                evidenceText = prev + " ... " + line.trim();
                                break;
                            }
                            if (prev.endsWith(";")) {
                                break;
                            }
                        }
                    }

                    final int finalStartLine = startLine;
                    boolean alreadyFlagged = findings.stream().anyMatch(f ->
                            "CR-RLS-001".equals(f.ruleId()) &&
                            relPath.equals(f.filePath()) &&
                            f.startLine() == finalStartLine
                    );

                    if (!alreadyFlagged) {
                        findings.add(RuleFinding.builder()
                                .ruleId("CR-RLS-001")
                                .category(Category.SECURITY)
                                .severity(Severity.CRITICAL)
                                .confidence(Confidence.HIGH)
                                .title("Insecure Database Script Disabling Row Level Security (RLS Bypass)")
                                .description("Database migration or SQL script contains statements disabling Row Level Security ('DISABLE ROW LEVEL SECURITY') or creating globally permissive policies ('USING (true)' / 'WITH CHECK (true)').")
                                .impact("Bypasses multi-tenant data isolation and row-level authorization, allowing any anonymous or low-privileged user to read, modify, or delete arbitrary rows across the database.")
                                .remediation("Enable Row Level Security ('ENABLE ROW LEVEL SECURITY') and specify restrictive policy predicates based on auth.uid() or tenant identifiers.")
                                .owaspMapping("A01:2021-Broken Access Control")
                                .filePath(relPath)
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(SecretMasker.maskSecrets(evidenceText))
                                .suggestedFix("ALTER TABLE table_name ENABLE ROW LEVEL SECURITY;\nCREATE POLICY \"Users can view own data\" ON table_name FOR SELECT USING (auth.uid() = user_id);")
                                .references(List.of("https://supabase.com/docs/guides/auth/row-level-security"))
                                .build()
                        );
                    }
                }

                // 14g. Check Mass Assignment / DTO Over-Posting (CR-PARAM-001)
                if (MASS_ASSIGNMENT_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-PARAM-001")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Mass Assignment / DTO Over-Posting via Unfiltered Request Body")
                            .description("Passing unvalidated client request bodies (req.body) directly into database mutations allows attackers to inject sensitive or privileged attributes (e.g. role, is_admin, verified, balance).")
                            .impact("Privilege escalation, unauthorized role assignment, and alteration of sensitive internal data fields.")
                            .remediation("Sanitize and pick only permitted fields using a strict allowlist or schema validator (Zod, Joi, or DTO). Never pass raw req.body into database mutations.")
                            .owaspMapping("A01:2021-Broken Access Control")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Sanitize input with explicit field picking:\nconst { name, email, bio } = req.body;\nawait db.user.update({ where: { id }, data: { name, email, bio } });")
                            .references(List.of("https://cheatsheetseries.owasp.org/cheatsheets/Mass_Assignment_Cheat_Sheet.html", "https://cwe.mitre.org/data/definitions/915.html"))
                            .build()
                    );
                }

                // 14h. Check IDOR on Resource Parameters (CR-PARAM-002)
                if (IDOR_PARAM_QUERY_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-PARAM-002")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Insecure Direct Object Reference (IDOR) on Resource Parameter")
                            .description("Resource fetched, updated, or deleted directly by client-supplied route or query parameter ID without verifying that the requesting user owns the object or belongs to the target tenant.")
                            .impact("Attackers can read, modify, or delete arbitrary customer or tenant records simply by iterating or guessing numeric/UUID identifiers.")
                            .remediation("Scope database queries to the authenticated user's session or tenant ID (e.g. WHERE id = :id AND tenant_id = :authTenantId).")
                            .owaspMapping("A01:2021-Broken Access Control")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Scope query by user/tenant identity:\nawait db.record.delete({ where: { id: req.params.id, userId: req.user.id } });")
                            .references(List.of("https://cheatsheetseries.owasp.org/cheatsheets/Insecure_Direct_Object_References_Prevention_Cheat_Sheet.html", "https://cwe.mitre.org/data/definitions/639.html"))
                            .build()
                    );
                }

                // 14i. Check Open Redirect via Parameters (CR-PARAM-003)
                if (OPEN_REDIRECT_PARAM_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-PARAM-003")
                            .category(Category.SECURITY)
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.HIGH)
                            .title("Open Redirect via Unvalidated Destination Parameter")
                            .description("Application redirects browser navigation using an untrusted user-controlled parameter (e.g. ?redirectUrl=, ?next=, ?returnTo=) without validating against an allowlist or verifying it is a relative path.")
                            .impact("Phishing attacks where attackers construct legitimate-looking links under your domain that silently redirect victims to credential-harvesting websites.")
                            .remediation("Enforce relative redirects (e.g. ensuring target starts with a single '/' and not '//') or validate target URLs against a strict allowlist of authorized hostnames.")
                            .owaspMapping("A01:2021-Broken Access Control")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Validate relative URL:\nconst target = req.query.redirectUrl;\nconst safeRedirect = (target && target.startsWith('/') && !target.startsWith('//')) ? target : '/dashboard';\nres.redirect(safeRedirect);")
                            .references(List.of("https://cheatsheetseries.owasp.org/cheatsheets/Unvalidated_Redirects_and_Forwards_Cheat_Sheet.html", "https://cwe.mitre.org/data/definitions/601.html"))
                            .build()
                    );
                }

                // 14j. Check Unbounded Pagination Parameter (CR-PARAM-004)
                if (UNBOUNDED_PAGINATION_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-PARAM-004")
                            .category(Category.SECURITY)
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.HIGH)
                            .title("Unbounded Pagination Parameter (Potential Memory Denial of Service)")
                            .description("Pagination parameters (limit, size, pageSize) are parsed directly from client request query parameters without enforcing a maximum ceiling clamp (e.g. Math.min(limit, 100)). Attackers can request ?limit=10000000 to trigger severe database load and OutOfMemory (OOM) crashes.")
                            .impact("Denial of Service (DoS) through database query timeouts, thread pool starvation, and application server heap memory exhaustion.")
                            .remediation("Clamp user-requested page sizes to a safe upper bound (e.g. Math.min(Math.max(1, limit), 100)).")
                            .owaspMapping("A04:2021-Insecure Design")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Clamp pagination limit:\nconst requestedLimit = parseInt(req.query.limit || '20', 10);\nconst safeLimit = Math.min(Math.max(1, isNaN(requestedLimit) ? 20 : requestedLimit), 100);")
                            .references(List.of("https://cwe.mitre.org/data/definitions/770.html", "https://owasp.org/Top10/A04_2021-Insecure_Design/"))
                            .build()
                    );
                }

                // 14k. Check Prototype Pollution via Parameter Merging (CR-PARAM-005)
                if (PROTOTYPE_POLLUTION_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-PARAM-005")
                            .category(Category.SECURITY)
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .title("Prototype Pollution via Unsafe Request Parameter Merging")
                            .description("Unsafe merging of user-controlled request parameters (req.body / req.query) into existing objects using Object.assign, lodash.merge, or direct bracket indexing allows attackers to inject properties into Object.prototype (e.g. __proto__, constructor).")
                            .impact("Prototype pollution can bypass access controls, alter application-wide properties, cause Denial of Service, or lead to Remote Code Execution (RCE).")
                            .remediation("Do not recursively merge untrusted input. Use Object.create(null) for dictionary lookups, sanitize keys (blocking __proto__ and constructor), or validate payloads with strict schema validators.")
                            .owaspMapping("A03:2021-Injection")
                            .filePath(relPath)
                            .startLine(lineNum)
                            .endLine(lineNum)
                            .evidence(SecretMasker.maskSecrets(line.trim()))
                            .suggestedFix("// Prevent prototype pollution:\nconst safeData = Object.create(null);\nfor (const [key, val] of Object.entries(req.body)) {\n  if (key !== '__proto__' && key !== 'constructor' && key !== 'prototype') {\n    safeData[key] = val;\n  }\n}")
                            .references(List.of("https://cwe.mitre.org/data/definitions/1321.html", "https://owasp.org/www-community/attacks/Prototype_Pollution"))
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

    public static List<String> readFileLines(Path file) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file);
        } catch (IOException e) {
            return List.of();
        }

        if (bytes == null || bytes.length == 0) {
            return List.of();
        }

        String content;

        // 1. Detect UTF-16LE BOM (FF FE)
        if (bytes.length >= 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
            content = new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        }
        // 2. Detect UTF-16BE BOM (FE FF)
        else if (bytes.length >= 2 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
            content = new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }
        // 3. Detect UTF-8 BOM (EF BB BF)
        else if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            content = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        // 4. Detect UTF-16LE without BOM (ASCII characters with 0x00 at odd indices)
        else if (bytes.length >= 4 && bytes[1] == 0 && bytes[3] == 0 && bytes[0] != 0 && bytes[2] != 0) {
            content = new String(bytes, StandardCharsets.UTF_16LE);
        }
        // 5. Detect UTF-16BE without BOM (ASCII characters with 0x00 at even indices)
        else if (bytes.length >= 4 && bytes[0] == 0 && bytes[2] == 0 && bytes[1] != 0 && bytes[3] != 0) {
            content = new String(bytes, StandardCharsets.UTF_16BE);
        }
        // 6. Default: try UTF-8, then fallback to ISO-8859-1
        else {
            try {
                content = new String(bytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                content = new String(bytes, StandardCharsets.ISO_8859_1);
            }
        }

        // Clean any lingering null bytes from binary/corrupted streams
        if (content.indexOf('\0') != -1) {
            content = content.replace("\0", "");
        }

        return List.of(content.split("\\r?\\n", -1));
    }
}
