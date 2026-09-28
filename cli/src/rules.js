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
  }
];

module.exports = {
  RULES,
  getRuleById: (id) => RULES.find(r => r.id === id),
  getRulesForExtension: (ext) => RULES.filter(r => r.fileExtensions.includes(ext.toLowerCase()))
};
