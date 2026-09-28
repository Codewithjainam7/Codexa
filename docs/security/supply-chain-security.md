# Codexa Software Supply Chain Security, SBOM & SLSA Compliance

This specification defines the software supply chain defense framework, Software Bill of Materials (SBOM) generation standards, Sigstore cryptographic signing protocols, and SLSA Level 3 compliance controls implemented by **Codexa**.

---

## 1. Supply Chain Threat Landscape & Strategic Objectives

Modern application vulnerabilities increasingly originate within external dependencies, build system pipelines, and container registry infrastructure rather than first-party code:
- **Malicious Dependency Injection**: Typosquatting and compromised open-source maintainer accounts.
- **Transitive CVE Vulnerabilities**: Hidden vulnerabilities in nested sub-dependencies (e.g. Log4Shell, Spring4Shell).
- **Tampered Container Images**: Man-in-the-middle attacks injecting backdoors into release container images.
- **Compromised Build CI Runners**: Tampering with binaries during GitHub Actions execution.

### Codexa Supply Chain Invariants:
1. **SLSA Level 3 Alignment**: All production artifacts are produced via hermetic, authenticated, ephemeral CI build runners with verifiable provenance attestations.
2. **Deterministic Cryptographic Verification**: Releases are cryptographically signed using Sigstore Cosign with keyless OIDC transparency logs.
3. **Comprehensive SBOM Publishing**: Every release includes machine-readable CycloneDX v1.5 and SPDX v2.3 SBOM manifests.

---

## 2. Supply-Chain Levels for Software Artifacts (SLSA) Matrix

Codexa achieves compliance across the SLSA Level 3 security framework:

| SLSA Level 3 Requirement | Implementation Mechanism in Codexa | Verification Status |
|:---|:---|:---:|
| **Source: Version Controlled** | Every change tracked in Git with required peer review and signed commits. | PASS |
| **Source: Retained History** | Full immutable Git commit history retained indefinitely on GitHub. | PASS |
| **Build: Build Service** | Builds run strictly in GitHub-hosted ephemeral runners (no developer machine builds). | PASS |
| **Build: Build as Code** | Build workflows defined exclusively in `.github/workflows/ci.yml`. | PASS |
| **Build: Ephemeral Environment** | Clean isolated VM container spun up per job and wiped immediately on exit. | PASS |
| **Build: Isolated Environment** | No cross-job persistence; isolated secrets injected via GitHub Actions Secrets. | PASS |
| **Provenance: Non-Falsifiable** | Generated via GitHub Actions trusted runner attestation (`actions/attest-build-provenance`). | PASS |

---

## 3. Software Bill of Materials (SBOM) Generation

Every build generates standard machine-readable SBOMs for both the Java backend and the React frontend:

### Backend: CycloneDX Maven Plugin
```xml
<plugin>
    <groupId>org.cyclonedx</groupId>
    <artifactId>cyclonedx-maven-plugin</artifactId>
    <version>2.8.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>makeAggregateBom</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <projectType>application</projectType>
        <schemaVersion>1.5</schemaVersion>
        <includeBomSerialNumber>true</includeBomSerialNumber>
    </configuration>
</plugin>
```

### Frontend: CycloneDX Node
```bash
npx @cyclonedx/cyclonedx-npm --output-file bom.json --spec-version 1.5
```

### Published SBOM Artifacts:
- `codexa-backend-1.3.0-cyclonedx.json` (CycloneDX v1.5)
- `codexa-backend-1.3.0-spdx.json` (SPDX v2.3)
- `codexa-frontend-1.3.0-cyclonedx.json` (CycloneDX v1.5)

---

## 4. Container Image Signing & Verification (Sigstore Cosign)

Production Docker images pushed to the GitHub Container Registry (`ghcr.io/codewithjainam7/codexa`) are cryptographically signed using Sigstore Cosign in keyless mode with GitHub OIDC:

### GitHub Actions Signing Workflow:
```yaml
- name: Install Cosign
  uses: sigstore/cosign-installer@v3

- name: Sign Container Image
  run: |
    cosign sign --yes \
      ghcr.io/codewithjainam7/codexa:${{ github.sha }}
```

### Verification Command for Enterprise Operators:
Before deploying Codexa to production Kubernetes clusters, verify the cryptographic signature against the public Rekor transparency log:

```bash
cosign verify \
  --certificate-identity-regexp "https://github.com/Codewithjainam7/Codexa/.*" \
  --certificate-oidc-issuer "https://token.actions.githubusercontent.com" \
  ghcr.io/codewithjainam7/codexa:latest
```

---

## 5. Automated Dependency Auditing & Vulnerability Gates

1. **OWASP Dependency-Check**:
   The backend Maven build executes OWASP Dependency-Check scanning all JAR dependencies against the National Vulnerability Database (NVD):
   ```bash
   mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7.0
   ```
   If any transitive dependency contains an unpatched vulnerability with CVSS $\ge 7.0$, the build fails immediately.
2. **Dependabot & Renovate Bot**:
   Automated pull requests are dispatched weekly to bump dependencies, patch CVEs, and keep libraries on latest LTS releases.
3. **Reproducible Dependency Locks**:
   - `package-lock.json` with strict SHA-512 subresource integrity hashes for npm.
   - Maven wrapper (`mvnw`) with SHA-256 binary validation to prevent build-tool tampering.
