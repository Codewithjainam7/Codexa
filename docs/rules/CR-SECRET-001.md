# Rule: CR-SECRET-001 — Hardcoded Credentials & API Keys

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-SECRET-001` |
| **Category** | `SECURITY` |
| **Severity** | `CRITICAL` |
| **Confidence** | `HIGH` |
| **CWE** | [CWE-798: Use of Hard-coded Credentials](https://cwe.mitre.org/data/definitions/798.html) |
| **OWASP Top 10** | [A07:2021 - Identification and Authentication Failures](https://owasp.org/Top10/2021/A07_2021-Identification_and_Authentication_Failures/) |

---

## 1. Description

Detects high-entropy cryptographic secrets, API keys, database passwords, private keys, and authorization tokens committed directly into source code repositories. Hardcoded secrets are exposed to anyone with read access to the repository or build artifacts and cannot be revoked without code redeployment.

---

## 2. Monitored Token Signatures

- **Cloud & Service Tokens**:
  - AWS Access Key ID (`AKIA[0-9A-Z]{16}`) and Secret Access Key
  - GitHub Personal Access Tokens (`ghp_[a-zA-Z0-9]{36}`, `gho_...`)
  - Slack Tokens (`xox[baprs]-[0-9]{10,13}-...`)
  - OpenAI / OpenRouter API Keys (`sk-[a-zA-Z0-9]{32,}`)
  - Stripe Secret Keys (`sk_live_[0-9a-zA-Z]{24}`)
- **Asymmetric Private Keys**:
  - `-----BEGIN PRIVATE KEY-----` / `-----BEGIN RSA PRIVATE KEY-----`
- **Database Connection URIs**:
  - `postgres://user:password@host:5432/db`
  - `mongodb+srv://admin:secret@cluster0...`

---

## 3. Vulnerable Code Example

```java
public class CloudStorageClient {
    // VULNERABLE: Secret hardcoded directly in compiled byte code
    private static final String AWS_ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
    private static final String AWS_SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    public void init() {
        ...
    }
}
```

---

## 4. Remediated Code Example

```java
public class CloudStorageClient {
    // SECURE: Externalized to environment variables or secret manager
    private final String accessKey;
    private final String secretKey;

    public CloudStorageClient() {
        this.accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        this.secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        if (this.accessKey == null || this.secretKey == null) {
            throw new IllegalStateException("AWS credentials missing from environment configuration");
        }
    }
}
```

---

## 5. Remediation & Incident Response

1. **Immediate Revocation**: Any committed secret must be assumed compromised and revoked immediately via the provider console.
2. **Git History Purge**: Use tools like `git-filter-repo` or BFG Repo-Cleaner to strip secrets from commit history.
3. **Pre-commit Scanning**: Integrate secret detection pre-commit hooks to block secrets before git staging.
