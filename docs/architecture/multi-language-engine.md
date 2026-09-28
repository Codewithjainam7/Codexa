# Codexa Multi-Language Static Analysis & Polyglot Engine Architecture

This document specifies the multi-language static analysis architecture, polyglot grammar parsers, AST traversal strategies, and configuration auditing mechanisms implemented in **Codexa**.

---

## 1. Architectural Philosophy: Tiered Polyglot Parsing

Modern software systems are inherently polyglot: a typical microservice may comprise a Java/Spring Boot backend, a TypeScript/React frontend, Python data science scripts, Go network proxies, and Docker/Kubernetes deployment manifests.

Codexa satisfies this operational reality through a **Three-Tier Parsing Architecture**:

```
                              [ Repository Workspace Files ]
                                             │
                       ┌─────────────────────┼─────────────────────┐
                       ▼                     ▼                     ▼
               Tier 1: Deep AST       Tier 2: Universal      Tier 3: Manifest
                 Compilation          Token Stream Scan       & Config Audit
               (Java / Kotlin)     (TS, JS, Py, Go, Rust) (Docker, K8s, YAML)
                       │                     │                     │
                       ▼                     ▼                     ▼
               Full Syntactic AST      Lexical Grammar        Structural Node
               Symbolic Resolution     Pattern Matcher        Tree Traversal
                       │                     │                     │
                       └─────────────────────┼─────────────────────┘
                                             │
                                             ▼
                                  [ Unified Rule Engine ]
                                  (Standard FindingEntity)
```

---

## 2. Language Support Matrix

| Language / Format | Parsing Tier | Engine / Parser | Covered Vulnerability Categories |
|:---|:---:|:---|:---|
| **Java** (8 – 21 LTS) | Tier 1 | JavaParser (AST Compilation Unit) | SQLi, RCE, Path Traversal, Deserialization, CSRF, Cryptography, Complexity |
| **Kotlin / Scala** | Tier 1 / 2 | JVM Class & Universal Tokenizer | Weak Crypto, Hardcoded Secrets, Sensitive Logging |
| **TypeScript / JavaScript** | Tier 2 | Lexical Scanner & JSX AST | DOM XSS, Prototype Pollution, Insecure Exec, Hardcoded Secrets, CORS |
| **Python** (3.8 – 3.12) | Tier 2 | Python Lexer & AST Patterns | Insecure Deserialization (`pickle`), Command Injection, Weak Hashes, SSRF |
| **Go** | Tier 2 | Go Lexer & Syntax Analyzer | Command Injection, Hardcoded Credentials, Race Conditions |
| **Docker & Dockerfile** | Tier 3 | Dockerfile AST Line Parser | Running as root, untagged images (`:latest`), secret leaks in build layers |
| **Kubernetes / Helm** | Tier 3 | YAML Manifest Parser | Privileged containers, missing resource limits, missing securityContext |
| **Environment / Config** | Tier 3 | Key-Value Property Parser | Exposed production secrets, debug modes enabled, wildcard CORS |

---

## 3. Tier 1: Java Deep AST Engine (`JavaAstParserService`)

For Java source code, Codexa compiles files into full in-memory Abstract Syntax Trees using JavaParser:

```java
// MethodDeclaration, ClassOrInterfaceDeclaration, MethodCallExpr
CompilationUnit cu = javaParser.parse(fileContent).getResult().orElseThrow();

// Traversal with Visitor pattern:
cu.accept(new VoidVisitorAdapter<Void>() {
    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);
        if (isVulnerableMethod(n)) {
            emitFinding(n);
        }
    }
}, null);
```

### Capabilities:
- **Symbolic Scope Traversal**: Resolves local variable assignments, method parameter flows, and return type hierarchies.
- **Cognitive & Cyclomatic Complexity Calculation**: Computes exact cyclomatic branching paths:
  $$M = E - N + 2P$$
- **Nesting Depth Analysis**: Traverses control-flow blocks (`if`, `for`, `while`, `switch`, `try`) to detect architectural degradation exceeding 6 nesting levels.

---

## 4. Tier 2: Universal Multi-Language Scanner (`UniversalMultiLanguageRule`)

For Python, JavaScript, TypeScript, Go, and Rust, Codexa utilizes a high-throughput lexical engine that scans token streams in parallel across worker threads:

### Detection Pattern Matrix:

#### Python Security Patterns:
- **Unsafe Deserialization**: Detects `pickle.loads()`, `yaml.load(Loader=Loader)` (unsafe YAML deserialization).
- **Command Injection**: Detects `os.system()`, `subprocess.Popen(..., shell=True)`.
- **Insecure Hashes**: Identifies `hashlib.md5()` and `hashlib.sha1()`.

#### JavaScript / TypeScript Patterns:
- **Client-Side XSS**: Flags `innerHTML`, `outerHTML`, and React `dangerouslySetInnerHTML`.
- **Dynamic Code Execution**: Flags `eval()`, `new Function()`, and `setTimeout("string")`.
- **Prototype Pollution**: Identifies recursive property merges on `__proto__` and `constructor.prototype`.

#### Go Security Patterns:
- **Command Injection**: Identifies `exec.Command("sh", "-c", userInput)`.
- **Insecure Cryptography**: Flags `crypto/md5` and `crypto/sha1`.

---

## 5. Tier 3: Infrastructure-as-Code (IaC) & Configuration Scanner

Codexa evaluates security misconfigurations in DevOps configuration manifests:

```yaml
# Evaluates security anti-patterns in Kubernetes PodSpecs:
securityContext:
  privileged: true          # CR-CONFIG-001: Privileged container forbidden
  runAsUser: 0              # CR-CONFIG-001: Root execution detected
```

- **Dockerfiles**: Flags `USER root`, `ADD http://...` (vulnerable to MiTM), and missing healthcheck directives.
- **Environment Files (`.env`, `.env.production`)**: Identifies committed API secrets, plaintext passwords, and private SSH keys using Shannon entropy analysis.

---

## 6. Concurrent Polyglot Pipeline Execution

To achieve sub-second scan speeds across multi-thousand file repositories, the multi-language engine executes within a managed Java `ForkJoinPool`:

1. **Classification Phase**: Files are classified into Language Tiers during the initial directory crawl.
2. **Parallel Distribution**: Tier 1 and Tier 2 engines process files concurrently across available CPU cores.
3. **Finding Harmonization**: Findings from all tiers are normalized into the unified `FindingEntity` format with normalized line numbers, snippet excerpts, and MITRE CWE references.
