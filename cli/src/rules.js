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
  }
];

module.exports = {
  RULES,
  getRuleById: (id) => RULES.find(r => r.id === id),
  getRulesForExtension: (ext) => RULES.filter(r => r.fileExtensions.includes(ext.toLowerCase()))
};
