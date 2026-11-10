/**
 * Codexa Static Analysis Rules Catalog for CLI & LSP
 * Mapped to OWASP Top 10 (2021) and CWE Standards.
 */

const RULES = [
  {
    id: "CR-SQL-001",
    title: "SQL Injection via Dynamic String Concatenation",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-89",
    fileExtensions: [".java", ".py", ".js", ".ts", ".go"],
    check: (line, lineNum) => {
      const sqlPattern = /(?:SELECT|INSERT|UPDATE|DELETE|FROM|WHERE)\s+.*(?:\+|%s|\${|f"|`.*\$\{)/i;
      const executePattern = /(?:executeQuery|executeUpdate|execute|cursor\.execute|db\.query)\s*\(/;
      if (sqlPattern.test(line) || (executePattern.test(line) && line.includes("+"))) {
        return {
          description: "Detected dynamic string concatenation in SQL statement constructing unparameterized query.",
          remediation: "Replace dynamic string concatenation with parameterized PreparedStatement or ORM query binding."
        };
      }
      return null;
    }
  },
  {
    id: "CR-CMD-001",
    title: "OS Command Injection",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-78",
    fileExtensions: [".java", ".py", ".js", ".ts", ".go"],
    check: (line, lineNum) => {
      const patterns = [
        /Runtime\.getRuntime\(\)\.exec\s*\(/,
        /ProcessBuilder\s*\([^)]*\+/,
        /child_process\.(?:exec|execSync)\s*\(/,
        /os\.system\s*\(/,
        /subprocess\.(?:call|Popen|run)\s*\([^)]*shell\s*=\s*True/
      ];
      if (patterns.some(p => p.test(line))) {
        return {
          description: "Direct invocation of operating system shell with dynamic arguments can lead to arbitrary code execution.",
          remediation: "Use structured argument arrays without invoking an underlying system shell, and validate input against strict whitelists."
        };
      }
      return null;
    }
  },
  {
    id: "CR-SECRET-001",
    title: "Hardcoded API Key, Token, or Credential",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-798",
    fileExtensions: [".java", ".py", ".js", ".ts", ".go", ".json", ".yml", ".yaml", ".env"],
    check: (line, lineNum) => {
      // Avoid matching template variables or examples
      if (line.includes("YOUR_") || line.includes("EXAMPLE") || line.includes("process.env")) {
        return null;
      }
      const awsRegex = /(?:AKIA|ASIA)[0-9A-Z]{16}/;
      const genericSecretRegex = /(?:password|secret|api_key|apikey|private_key|token)\s*[:=]\s*["']([A-Za-z0-9_\-.~+/=]{16,})["']/i;
      const jwtRegex = /eyJ[A-Za-z0-9-_=]+\.[A-Za-z0-9-_=]+\.?[A-Za-z0-9-_.+/=]*/;
      const cloudTokenRegex = /(?:sk-proj-[A-Za-z0-9_\-]{32,}|sk-ant-[A-Za-z0-9_\-]{32,}|AIzaSy[A-Za-z0-9_\-]{33}|(?:sk|rk)_live_[A-Za-z0-9]{24,}|xox[baprs]-[A-Za-z0-9\-]{20,}|ghp_[A-Za-z0-9]{36})/;

      if (cloudTokenRegex.test(line)) {
        return {
          description: "Detected exposed high-entropy Cloud Provider API token (OpenAI/Anthropic/Gemini/Stripe/Slack/GitHub).",
          remediation: "Immediately revoke exposed token and migrate to environment variables or Cloud Secrets Manager."
        };
      }

      if (awsRegex.test(line)) {
        return {
          description: "Detected hardcoded AWS Access Key ID.",
          remediation: "Extract AWS credentials into environment variables, AWS Secrets Manager, or IAM instance roles."
        };
      }
      if (genericSecretRegex.test(line)) {
        return {
          description: "Detected plaintext secret assignment in source code.",
          remediation: "Externalize sensitive credentials into an environment configuration or a secure secret vault."
        };
      }
      if (jwtRegex.test(line) && line.length > 50) {
        return {
          description: "Detected hardcoded JSON Web Token (JWT).",
          remediation: "Obtain JWT tokens dynamically via authentication flows instead of embedding them in source code."
        };
      }
      return null;
    }
  },
  {
    id: "CR-PATH-001",
    title: "Path Traversal & Insecure File Access",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-22",
    fileExtensions: [".java", ".py", ".js", ".ts", ".go"],
    check: (line, lineNum) => {
      const patterns = [
        /new\s+File\s*\([^)]*(?:req|param|path|input)/i,
        /fs\.(?:readFile|readFileSync|createReadStream)\s*\([^)]*(?:req|param|query)/i,
        /open\s*\([^)]*(?:request|params|user_input)/i
      ];
      if (patterns.some(p => p.test(line))) {
        return {
          description: "Unsanitized user input utilized in file system path resolution allows directory traversal (`../`).",
          remediation: "Validate paths against a normalized canonical base directory with `path.resolve()` or `Path.normalize()`."
        };
      }
      return null;
    }
  },
  {
    id: "CR-DESER-001",
    title: "Insecure Object Deserialization",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-502",
    fileExtensions: [".java", ".py"],
    check: (line, lineNum) => {
      const patterns = [
        /new\s+ObjectInputStream\s*\(/,
        /\.readObject\s*\(\)/,
        /pickle\.loads\s*\(/,
        /yaml\.load\s*\([^,)]*\)(?!\s*,\s*Loader\s*=\s*SafeLoader)/
      ];
      if (patterns.some(p => p.test(line))) {
        return {
          description: "Deserialization of untrusted byte streams can lead to arbitrary remote code execution via gadget chains.",
          remediation: "Use safe data serialization formats such as JSON or Protocol Buffers, or enforce ObjectInputFilter / yaml.SafeLoader."
        };
      }
      return null;
    }
  },
  {
    id: "CR-CRYPTO-001",
    title: "Broken or Deprecated Cryptographic Algorithm",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-327",
    fileExtensions: [".java", ".py", ".js", ".ts", ".go"],
    check: (line, lineNum) => {
      const weakAlgPattern = /(?:getInstance\s*\(\s*["'](?:MD5|SHA-1|DES|RC4)["']|hashlib\.(?:md5|sha1)\s*\(|crypto\.createHash\s*\(\s*["'](?:md5|sha1)["'])/i;
      if (weakAlgPattern.test(line)) {
        return {
          description: "Detected use of collision-vulnerable or deprecated cryptographic algorithm (MD5/SHA-1/DES/RC4).",
          remediation: "Upgrade to SHA-256 / SHA-512 for hashing, or AES-GCM (256-bit) for symmetric encryption."
        };
      }
      return null;
    }
  },
  {
    id: "CR-RAND-001",
    title: "Insecure Pseudorandom Number Generator",
    category: "SECURITY",
    severity: "MEDIUM",
    cwe: "CWE-338",
    fileExtensions: [".java", ".py", ".js", ".ts"],
    check: (line, lineNum) => {
      const patterns = [
        /new\s+Random\s*\(\)/,
        /Math\.random\s*\(\)/,
        /random\.random\s*\(/
      ];
      if (patterns.some(p => p.test(line)) && (line.includes("token") || line.includes("key") || line.includes("salt") || line.includes("auth"))) {
        return {
          description: "Standard PRNG used in security-sensitive context (tokens/keys/salts) is predictable.",
          remediation: "Use a cryptographically secure pseudorandom number generator (CSPRNG) such as java.security.SecureRandom or crypto.randomBytes()."
        };
      }
      return null;
    }
  },
  {
    id: "CR-CORS-001",
    title: "Permissive Wildcard CORS Configuration",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-942",
    fileExtensions: [".java", ".js", ".ts", ".py"],
    check: (line, lineNum) => {
      if (/allowedOrigins\s*=\s*["']\*["']/.test(line) || /origin\s*:\s*["']\*["']/.test(line) || /cors\(.*origin:\s*["']\*["']\)/i.test(line)) {
        return {
          description: "Wildcard Access-Control-Allow-Origin ('*') allows any unauthorized third-party origin to interact with the API.",
          remediation: "Restrict allowed origins to an explicit, authenticated domain whitelist."
        };
      }
      return null;
    }
  },
  {
    id: "CR-QUAL-006",
    title: "Empty or Swallowed Catch Block",
    category: "QUALITY",
    severity: "LOW",
    cwe: "CWE-390",
    fileExtensions: [".java", ".js", ".ts", ".py"],
    check: (line, lineNum) => {
      if (/catch\s*\([^)]*\)\s*\{\s*\}/.test(line) || /except\s*(?:Exception)?\s*:\s*pass/.test(line)) {
        return {
          description: "Empty catch/except block silently swallows exceptions, masking failures and complicating diagnostic triage.",
          remediation: "Log the exception with full stack trace or propagate it wrapped in a domain exception."
        };
      }
      return null;
    }
  },
  {
    id: "CR-XXE-001",
    title: "XML External Entity (XXE) Injection",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-611",
    fileExtensions: [".java", ".py", ".js", ".ts"],
    check: (line, lineNum) => {
      const patterns = [
        /(?:DocumentBuilderFactory|SAXParserFactory|XMLInputFactory|TransformerFactory)\.newInstance\s*\(\)/,
        /etree\.parse\s*\(/,
        /xml\.etree\.ElementTree\.parse\s*\(/,
        /xml2js\.parseString\s*\(/
      ];
      if (patterns.some(p => p.test(line))) {
        return {
          description: "XML parser initialized without explicitly disabling DOCTYPE declarations or external entity resolution (XXE).",
          remediation: "Set feature 'http://apache.org/xml/features/disallow-doctype-decl' to true or configure secure parser features."
        };
      }
      return null;
    }
  },
  {
    id: "CR-SSTI-001",
    title: "Server-Side Template Injection (SSTI)",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-1336",
    fileExtensions: [".java", ".py", ".js", ".ts"],
    check: (line, lineNum) => {
      const patterns = [
        /(?:render_template_string|Template|jinja2\.Template)\s*\([^)]*(?:\+|req|param|input)/,
        /nunjucks\.renderString\s*\([^)]*(?:\+|req|param|input)/,
        /handlebars\.compile\s*\([^)]*(?:\+|req|param|input)/,
        /pug\.compile\s*\([^)]*(?:\+|req|param|input)/,
        /cfg\.getTemplate\s*\([^)]*\+/
      ];
      if (patterns.some(p => p.test(line))) {
        return {
          description: "Dynamic string concatenation in template rendering leads to Server-Side Template Injection (SSTI) and Remote Code Execution.",
          remediation: "Load static template files and pass dynamic user parameters exclusively through isolated template data models."
        };
      }
      return null;
    }
  },
  {
    id: "CR-JWT-001",
    title: "Insecure JWT Verification / None Algorithm Acceptance",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-347",
    fileExtensions: [".java", ".py", ".js", ".ts", ".go"],
    check: (line, lineNum) => {
      const patterns = [
        /parseClaimsJwt\s*\(/,
        /jwt\.decode\s*\([^,)]*\)(?!\s*,\s*verify\s*=\s*True)/,
        /verify\s*:\s*false/i,
        /verify_signature\s*=\s*False/i,
        /algorithms\s*:\s*\[.*['"]none['"].*\]/i
      ];
      if (patterns.some(p => p.test(line))) {
        return {
          description: "Detected disabled JWT signature verification or acceptance of the insecure 'none' algorithm.",
          remediation: "Enforce cryptographic signature validation and specify whitelist algorithms (e.g. RS256, HS256)."
        };
      }
      return null;
    }
  },
  {
    id: "CR-REDOS-001",
    title: "Regular Expression Denial of Service (ReDoS)",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-1333",
    fileExtensions: [".java", ".py", ".js", ".ts", ".go"],
    check: (line, lineNum) => {
      const redosRegex = /(?:\([^)]+[+*]\)[+*]|\([^)]+\|[^)]+\)[+*]|(?:\\[a-zA-Z]|\.)[+*][+*])/;
      if (redosRegex.test(line) && (line.includes("RegExp") || line.includes("compile") || line.includes("match") || line.includes("/"))) {
        return {
          description: "Catastrophic backtracking regular expression containing nested quantifiers risks freezing the process thread (ReDoS).",
          remediation: "Refactor regex with atomic grouping or possessive quantifiers, and clamp input length prior to regex evaluation."
        };
      }
      return null;
    }
  },
  {
    id: "CR-RSC-001",
    title: "Unauthenticated Next.js Server Action Mutation",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-306",
    fileExtensions: [".js", ".ts", ".jsx", ".tsx"],
    check: (line, lineNum) => {
      if (line.includes("'use server'") || line.includes('"use server"')) {
        return {
          description: "Next.js 'use server' action declared. Ensure all exported server actions strictly enforce caller session checks (auth() / getServerSession()).",
          remediation: "Wrap server action execution with an authentication guard and validate request parameters with Zod schema parsing."
        };
      }
      return null;
    }
  },
  {
    id: "CR-IAC-007",
    title: "Overly Permissive IAM Wildcard Policy",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-732",
    fileExtensions: [".tf", ".json", ".yml", ".yaml"],
    check: (line, lineNum) => {
      if (/(?:["']Action["']\s*:\s*(?:\[\s*["']\*["']\s*\]|["']\*["'])|action\s*=\s*\[?["']\*["']\]?)/i.test(line)) {
        return {
          description: "IAM policy grants wildcard permissions ('*'), violating the principle of least privilege.",
          remediation: "Scope IAM actions to specific API operations required by the service."
        };
      }
      return null;
    }
  },
  {
    id: "CR-IAC-008",
    title: "Public Cloud Storage Bucket Exposure",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-284",
    fileExtensions: [".tf", ".json", ".yml", ".yaml"],
    check: (line, lineNum) => {
      if (/(?:acl\s*=\s*["']public-read["']|block_public_acls\s*=\s*false)/i.test(line)) {
        return {
          description: "Storage bucket configured with public-read permissions or disabled S3 block public access.",
          remediation: "Enforce aws_s3_bucket_public_access_block with all block flags set to true."
        };
      }
      return null;
    }
  },
  {
    id: "CR-LLM-003",
    title: "Insecure AI Agent Autonomous Tool Execution",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-74",
    fileExtensions: [".py", ".js", ".ts", ".java"],
    check: (line, lineNum) => {
      const patterns = [
        /(?:BashProcess|PythonREPL|TerminalTool|ShellTool|SystemTool|execute_shell|run_bash|eval_code)\s*\(/i,
        /(?:child_process\.exec|os\.system|subprocess\.call)\s*\([^)]*(?:response|completion|message|tool_call)/i
      ];
      if (patterns.some(p => p.test(line))) {
        return {
          description: "Autonomous AI Agent binds LLM completion outputs directly into operating system shells or execution REPLs without human approval.",
          remediation: "Require Human-in-the-Loop (HITL) approval gates before tool invocation and sandbox execution in ephemeral micro-containers."
        };
      }
      return null;
    }
  },
  {
    id: "CR-CRYPTO-002",
    title: "Insecure Cipher Mode (ECB) or Static IV",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-326",
    fileExtensions: [".java", ".py", ".js", ".ts", ".go"],
    check: (line, lineNum) => {
      if (/(?:AES\/ECB|DES\/ECB|Blowfish\/ECB)/i.test(line)) {
        return {
          description: "Detected use of ECB cipher mode which preserves plaintext data patterns across blocks.",
          remediation: "Upgrade to AES-GCM (AES/GCM/NoPadding) authenticated encryption with fresh random IV."
        };
      }
      if (/new\s+IvParameterSpec\s*\(\s*(?:new\s+byte\[|STATIC_IV)/i.test(line)) {
        return {
          description: "Static initialization vector (IV) compromises semantic ciphertext confidentiality.",
          remediation: "Generate a fresh cryptographically random IV per encryption operation via SecureRandom."
        };
      }
      return null;
    }
  },
  {
    id: "CR-GRAPHQL-001",
    title: "GraphQL Depth Limiting & Introspection Exposure",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-770",
    fileExtensions: [".js", ".ts", ".java", ".py"],
    check: (line, lineNum) => {
      if (/(?:ApolloServer|createYoga|graphqlHTTP)\s*\(/.test(line)) {
        return {
          description: "GraphQL server declared. Ensure query depth limiting (graphql-depth-limit) is configured to prevent recursive query DoS.",
          remediation: "Add validationRules: [depthLimit(6)] and disable schema introspection in production."
        };
      }
      if (/introspection\s*:\s*true/i.test(line)) {
        return {
          description: "GraphQL introspection enabled. Disclose entire internal API schema to unauthenticated clients.",
          remediation: "Disable introspection in production (e.g. process.env.NODE_ENV !== 'production')."
        };
      }
      return null;
    }
  },
  {
    id: "CR-WEBSOCKET-001",
    title: "Insecure WebSocket Origin Validation (CSWSH)",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-1385",
    fileExtensions: [".js", ".ts", ".java", ".go"],
    check: (line, lineNum) => {
      if (/(?:setAllowedOrigins\s*\(\s*["']\*["']\)|origin\s*:\s*["']\*["']|checkOrigin\s*=\s*nil)/i.test(line)) {
        return {
          description: "WebSocket connection allows wildcard origin ('*'), exposing users to Cross-Site WebSocket Hijacking (CSWSH).",
          remediation: "Validate the incoming Origin header against an explicit domain allowlist during handshake."
        };
      }
      return null;
    }
  },
  {
    id: "CR-GRPC-001",
    title: "gRPC Plaintext Channel & Unauthenticated Service",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-319",
    fileExtensions: [".java", ".go", ".py", ".ts"],
    check: (line, lineNum) => {
      if (/(?:\.usePlaintext\s*\(\)|insecure\.NewCredentials\(\))/i.test(line)) {
        return {
          description: "gRPC channel configured without TLS encryption (usePlaintext / insecure credentials).",
          remediation: "Enforce TLS channel credentials or Mutual TLS (mTLS) for all gRPC communications."
        };
      }
      return null;
    }
  },
  {
    id: "CR-REDIS-001",
    title: "Redis Command & Lua Script Injection",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-94",
    fileExtensions: [".java", ".js", ".ts", ".py"],
    check: (line, lineNum) => {
      if (/(?:redis|jedis|client)\.(?:eval|evalsha)\s*\([^)]*(?:\+|`.*\$\{)/i.test(line)) {
        return {
          description: "Dynamic string concatenation in Redis eval() enables Lua injection and cache poisoning.",
          remediation: "Pass variables via dedicated KEYS and ARGV argument arrays instead of string concatenation."
        };
      }
      return null;
    }
  },
  {
    id: "CR-NOSQL-001",
    title: "NoSQL Operator Injection & $where Execution",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-943",
    fileExtensions: [".js", ".ts", ".py", ".java"],
    check: (line, lineNum) => {
      if (/\$where\s*:\s*(?:`[^`]*\$\{|f["'][^"']*\{|["'][^"']*["']\s*\+)/i.test(line)) {
        return {
          description: "Dynamic string concatenation inside MongoDB $where clause allows arbitrary server-side JavaScript execution.",
          remediation: "Never concatenate strings into $where. Use standard declarative query operators."
        };
      }
      if (/(?:\.findOne|\.find|\.deleteMany)\s*\(\s*req\.body\b/i.test(line)) {
        return {
          description: "Raw req.body passed directly to NoSQL query allows operator injection ({ $ne: null }) to bypass auth.",
          remediation: "Explicitly extract validated fields or sanitize objects with express-mongo-sanitize."
        };
      }
      return null;
    }
  },
  {
    id: "CR-RACE-001",
    title: "TOCTOU Race Condition on Shared Resources",
    category: "SECURITY",
    severity: "MEDIUM",
    cwe: "CWE-367",
    fileExtensions: [".js", ".ts", ".java", ".py"],
    check: (line, lineNum) => {
      if (/(?:existsSync|exists\(\)|os\.path\.exists)/i.test(line)) {
        return {
          description: "File or resource presence check immediately before write risks Time-of-Check to Time-of-Use (TOCTOU) race condition.",
          remediation: "Perform atomic create operations (e.g. O_EXCL / 'wx' flags) and catch collision errors."
        };
      }
      return null;
    }
  },
  {
    id: "CR-MEMORY-001",
    title: "Unclosed Stream or Native Resource Leak",
    category: "QUALITY",
    severity: "MEDIUM",
    cwe: "CWE-775",
    fileExtensions: [".java", ".py", ".go", ".js", ".ts"],
    check: (line, lineNum) => {
      if (/(?:new\s+FileInputStream|new\s+FileOutputStream|open\([^)]+\))\s*(?!.*with)/i.test(line)) {
        return {
          description: "Stream resource opened without try-with-resources or 'with' statement risks file descriptor leakage.",
          remediation: "Wrap file stream operations in a scoped context manager or try-with-resources statement."
        };
      }
      return null;
    }
  },
  {
    id: "CR-K8S-001",
    title: "Kubernetes HostPath Mount & Node Escapes",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-284",
    fileExtensions: [".yaml", ".yml"],
    check: (line, lineNum) => {
      if (/hostPath\s*:\s*(?:\{|\n)?/i.test(line) || /path\s*:\s*["']?\/(?:etc|var\/run\/docker\.sock|proc|sys)/i.test(line)) {
        return {
          description: "Kubernetes hostPath volume mount exposes worker node host filesystem directly to container.",
          remediation: "Replace hostPath with dedicated PersistentVolumeClaims (PVC) and block host access."
        };
      }
      return null;
    }
  },
  {
    id: "CR-HELM-001",
    title: "Hardcoded Secrets in Helm values.yaml",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-312",
    fileExtensions: [".yaml", ".yml"],
    check: (line, lineNum) => {
      if (/(?:password|dbPassword|rootPassword)\s*:\s*["'][a-zA-Z0-9_\-+=]{6,}["']/i.test(line) && !line.includes("{{")) {
        return {
          description: "Plaintext database password or token declared in Helm values configuration.",
          remediation: "Reference external Kubernetes Secrets or utilize helm-secrets (Mozilla SOPS)."
        };
      }
      return null;
    }
  },
  {
    id: "CR-SUPPLY-001",
    title: "Dependency Confusion & Supply Chain Insecurity",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-829",
    fileExtensions: [".conf", ".txt", ".npmrc", ".json"],
    check: (line, lineNum) => {
      if (/extra-index-url\s*=\s*https?:\/\/pypi\.org/i.test(line)) {
        return {
          description: "Using extra-index-url with public PyPI risks Dependency Confusion package substitution.",
          remediation: "Use a single unified internal package repository proxy instead of extra-index-url."
        };
      }
      return null;
    }
  },
  {
    id: "CR-DEAD-001",
    title: "Dead Code & Unreachable Branch Detection",
    category: "QUALITY",
    severity: "LOW",
    cwe: "CWE-561",
    fileExtensions: [".js", ".ts", ".java", ".py", ".go"],
    check: (line, lineNum) => {
      if (/if\s*\(\s*(?:false|0\s*==\s*1)\s*\)/i.test(line)) {
        return {
          description: "Branch guarded by constant false condition will never execute (Dead Code).",
          remediation: "Remove dead branch and unreachable statements."
        };
      }
      return null;
    }
  },
  {
    id: "CR-HTTP-001",
    title: "HTTP Request Smuggling & Header Desync",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-444",
    fileExtensions: [".js", ".ts", ".java", ".conf"],
    check: (line, lineNum) => {
      if (/setHeader\s*\(\s*["'](?:Transfer-Encoding|Content-Length)["']/i.test(line) || /proxy_set_header\s+Connection\s+["']?\$http_connection/i.test(line)) {
        return {
          description: "Manual Transfer-Encoding / Content-Length setting or unvalidated hop-by-hop forwarding risks HTTP Request Smuggling.",
          remediation: "Let web server framework handle chunking and framing deterministically; enforce HTTP/2 end-to-end."
        };
      }
      return null;
    }
  },
  {
    id: "CR-PROTO-001",
    title: "Server-Side Prototype Pollution via Dynamic Key Assignment",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-1321",
    fileExtensions: [".js", ".ts"],
    check: (line, lineNum) => {
      if (/(?:\[['"]__proto__['"]\]|target\[k\]\s*=\s*source\[k\])/i.test(line) && !line.includes("hasOwn")) {
        return {
          description: "Unsanitized dynamic bracket property assignment allows prototype pollution into Object.prototype.",
          remediation: "Filter out '__proto__' and 'constructor' keys before recursive assignment, or use Object.create(null)."
        };
      }
      return null;
    }
  },
  {
    id: "CR-OAUTH-001",
    title: "OAuth2 Open Redirect & Missing PKCE",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-601",
    fileExtensions: [".js", ".ts", ".java", ".py"],
    check: (line, lineNum) => {
      if (/redirect_uri\s*=\s*(?:req\.(?:query|params)|request\.(?:args|GET)|["'][^"']*\*["'])/i.test(line)) {
        return {
          description: "OAuth2 redirect_uri dynamically resolved from untrusted input allows authorization code theft.",
          remediation: "Enforce exact string matching against pre-registered client redirect URIs and require PKCE."
        };
      }
      return null;
    }
  },
  {
    id: "CR-DESER-002",
    title: "Polyglot Insecure Deserialization (Python/Node)",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-502",
    fileExtensions: [".py", ".js", ".ts"],
    check: (line, lineNum) => {
      if (/(?:pickle\.loads?|yaml\.load\s*\([^,)]*\)(?!.*SafeLoader)|serialize\.unserialize)/i.test(line)) {
        return {
          description: "Insecure object deserialization allows arbitrary remote code execution via gadget chains.",
          remediation: "Use safe data formats (JSON) or enforce yaml.safe_load."
        };
      }
      return null;
    }
  },
  {
    id: "CR-CORS-002",
    title: "Insecure CORS Policy & Credential Leakage",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-942",
    fileExtensions: [".js", ".ts", ".java", ".py", ".go", ".cs"],
    check: (line, lineNum) => {
      if (/Access-Control-Allow-Origin["']\s*,\s*(?:req\.headers\[?['"]origin['"]?\]?|request\.getHeader\(['"]Origin['"]\))/i.test(line) ||
          /(?:origin\.includes|origin\.indexOf|origin\.endsWith)\s*\(['"][a-zA-Z0-9.-]+\.[a-z]{2,}['"]\)/i.test(line)) {
        return {
          description: "CORS configuration reflects arbitrary Origin header or uses weak suffix matching, risking cross-origin credential theft.",
          remediation: "Enforce strict domain allowlists with anchored exact-match regex or explicit set lookups."
        };
      }
      return null;
    }
  },
  {
    id: "CR-K8S-002",
    title: "Kubernetes Secret Ingestion via Environment Variables",
    category: "SECURITY",
    severity: "MEDIUM",
    cwe: "CWE-526",
    fileExtensions: [".yaml", ".yml"],
    check: (line, lineNum) => {
      if (/(?:secretKeyRef|secretRef)\s*:/i.test(line)) {
        return {
          description: "Secret referenced in container environment variables, exposing secrets to child processes, crash dumps, and /proc.",
          remediation: "Mount sensitive credentials as read-only volume mounts (mode 0400) or use CSI Secret Store driver."
        };
      }
      return null;
    }
  },
  {
    id: "CR-IAC-009",
    title: "Dockerfile SUID Escalation & Sudo Package Installation",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-250",
    fileExtensions: ["dockerfile", ".dockerfile"],
    check: (line, lineNum) => {
      if (/chmod\s+(?:[ug]\+s|[1-7]?[4-7][0-7]{2})/i.test(line) || /(?:apt-get|apk|yum)\s+.*install.*sudo/i.test(line)) {
        return {
          description: "Granting SUID bits or installing sudo inside container images violates least privilege and allows privilege escalation.",
          remediation: "Strip SUID permissions, remove sudo package, and switch to non-root USER."
        };
      }
      return null;
    }
  },
  {
    id: "CR-IAC-010",
    title: "Terraform RDS Unencrypted Storage & KMS Rotation Disabled",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-311",
    fileExtensions: [".tf", ".tfvars"],
    check: (line, lineNum) => {
      if (/storage_encrypted\s*=\s*false/i.test(line) || /publicly_accessible\s*=\s*true/i.test(line) || /enable_key_rotation\s*=\s*false/i.test(line)) {
        return {
          description: "Terraform configuration disables database storage encryption, exposes RDS publicly, or disables KMS key rotation.",
          remediation: "Enable storage_encrypted, set publicly_accessible = false, and enable KMS rotation."
        };
      }
      return null;
    }
  },
  {
    id: "CR-IAC-011",
    title: "S3 Public Website & Insecure CloudFront Viewer Protocol",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-319",
    fileExtensions: [".tf", ".json", ".yaml"],
    check: (line, lineNum) => {
      if (/viewer_protocol_policy\s*=\s*["']allow-all["']/i.test(line) || /acl\s*=\s*["']public-read(?:-write)?["']/i.test(line)) {
        return {
          description: "CloudFront viewer protocol allows plain HTTP or S3 bucket is configured with public-read ACL.",
          remediation: "Enforce viewer_protocol_policy = 'redirect-to-https' and private S3 ACL with Origin Access Control."
        };
      }
      return null;
    }
  },
  {
    id: "CR-CICD-001",
    title: "CI/CD Pipeline Script Injection & Untrusted Checkout",
    category: "SECURITY",
    severity: "CRITICAL",
    cwe: "CWE-94",
    fileExtensions: [".yml", ".yaml"],
    check: (line, lineNum) => {
      if (/\$\{\{\s*github\.event\.(?:issue|pull_request|comment)\.(?:title|body|head\.ref)/i.test(line)) {
        return {
          description: "Untrusted GitHub Actions event context interpolated directly into script block allows command injection.",
          remediation: "Map the event expression to an intermediate environment variable (env:) and refer to it as $VAR."
        };
      }
      return null;
    }
  },
  {
    id: "CR-ARCH-002",
    title: "Architectural Layer Boundary Inversion",
    category: "QUALITY",
    severity: "MEDIUM",
    cwe: "CWE-1061",
    fileExtensions: [".java", ".ts", ".js"],
    check: (line, lineNum) => {
      if (/import\s+.*(?:repository|dao)\./i.test(line) && /Controller/i.test(line)) {
        return {
          description: "Controller directly imports Repository or DAO component, bypassing service domain boundary.",
          remediation: "Encapsulate data access within Service classes and inject Services into Controllers."
        };
      }
      return null;
    }
  },
  {
    id: "CR-THREAD-001",
    title: "ThreadLocal Context Leakage & Missing Cleanup",
    category: "SECURITY",
    severity: "HIGH",
    cwe: "CWE-404",
    fileExtensions: [".java"],
    check: (line, lineNum) => {
      if (/(?:ThreadLocal|InheritableThreadLocal)<.*>\s+\w+\s*=\s*new/i.test(line)) {
        return {
          description: "ThreadLocal variable initialized without explicit try-finally .remove() lifecycle cleanup.",
          remediation: "Always clean up ThreadLocal state using finally { threadLocal.remove(); } in thread-pool architectures."
        };
      }
      return null;
    }
  }
];

module.exports = {
  RULES,
  getRuleById: (id) => RULES.find(r => r.id === id),
  getRulesForExtension: (ext) => RULES.filter(r => r.fileExtensions.includes(ext.toLowerCase()))
};
