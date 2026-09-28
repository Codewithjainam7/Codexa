# Codexa Release Engineering & Versioning Checklist

This operational runbook establishes the step-by-step quality gates, versioning protocols, artifact verification steps, and emergency hotfix procedures for publishing releases of the **Codexa Platform**.

---

## 1. Versioning Standards (SemVer 2.0.0)

Codexa adheres strictly to **Semantic Versioning 2.0.0** (`MAJOR.MINOR.PATCH`):

- **MAJOR (`X.0.0`)**: Incompatible API schema changes, removal of deprecated endpoints, major architectural rewrites (e.g. migration from v1 to v2).
- **MINOR (`1.X.0`)**: Backward-compatible new static analysis rules, new CI integrations (Azure DevOps, GitLab), new UI visualizations, or performance enhancements.
- **PATCH (`1.3.X`)**: Backward-compatible bug fixes, false-positive rule adjustments, security vulnerability patches, or documentation corrections.

---

## 2. Phase 1: Pre-Release Quality Gates (Zero-Defect Verification)

Execute these checks on the release branch before tagging:

### Quality Gate Checklist:
- [ ] **Backend Test Suite**: Confirm all 135 unit & integration tests pass with 0 failures:
  ```bash
  cd backend && mvn test -Djacoco.skip=true -Ddependency-check.skip=true
  ```
- [ ] **Code Formatting & Linting**:
  ```bash
  cd backend && mvn spotless:check
  cd ../frontend && npm run lint
  ```
- [ ] **Dependency Security Audit**: Confirm zero CRITICAL or HIGH CVEs in dependencies:
  ```bash
  cd backend && mvn org.owasp:dependency-check-maven:check
  cd ../frontend && npm audit --audit-level=high
  ```
- [ ] **Production Frontend Build**:
  ```bash
  cd frontend && npm run build
  # Verify dist/ files are clean, minified, and < 1.5MB total bundle size
  ```
- [ ] **Static Assets Synchronization**: Verify `frontend/dist` is synchronized into `backend/src/main/resources/static/`.

---

## 3. Phase 2: Version Synchronization & Changelog Updates

Synchronize version identifiers across all package manifests:

1. **Backend Maven POM (`backend/pom.xml`)**:
   ```xml
   <version>1.3.0</version>
   ```
2. **Frontend Manifest (`frontend/package.json`)**:
   ```json
   "version": "1.3.0"
   ```
3. **OpenAPI Specification (`backend/src/main/resources/openapi.yaml`)**:
   ```yaml
   info:
     version: "1.3.0"
   ```
4. **Changelog Update (`docs/CHANGELOG.md`)**:
   - Document all `Added`, `Changed`, `Fixed`, `Security`, and `Performance` improvements under `## [1.3.0] - YYYY-MM-DD`.
   - Provide migration guidance for any deprecated configuration properties.

Commit these synchronized changes:
```bash
git add backend/pom.xml frontend/package.json docs/CHANGELOG.md
git commit -m "chore(release): prepare v1.3.0 release artifacts"
```

---

## 4. Phase 3: Cryptographic Tagging & GitHub Release

1. **Create Annotated GPG-Signed Git Tag**:
   ```bash
   git tag -s -a v1.3.0 -m "Release v1.3.0: Enterprise static analysis, 135 passing tests, and zero false-positive precision"
   git push origin v1.3.0
   ```
2. **Automated CI/CD Workflow Execution**:
   Pushing tag `v1.3.0` automatically triggers `.github/workflows/release.yml`:
   - Builds multi-architecture Docker container images (`linux/amd64`, `linux/arm64`).
   - Pushes signed container images to `ghcr.io/codewithjainam7/codexa:1.3.0` via Sigstore Cosign.
   - Generates and publishes CycloneDX and SPDX Software Bill of Materials (SBOM).
   - Generates executable standalone JAR file (`codexa-backend-1.3.0.jar`).
3. **Publish GitHub Release**:
   - Paste corresponding changelog notes into GitHub Release UI.
   - Attach compiled standalone JAR and SBOM JSON files.

---

## 5. Phase 4: Post-Release Canary Verification

Perform a post-release smoke test against the newly published Docker image:

```bash
# 1. Pull published release image from GitHub Container Registry
docker pull ghcr.io/codewithjainam7/codexa:1.3.0

# 2. Run container locally
docker run -d --name codexa-canary -p 8080:8080 ghcr.io/codewithjainam7/codexa:1.3.0

# 3. Verify health probe
sleep 10
curl -f http://localhost:8080/actuator/health

# 4. Execute test scan
curl -F "file=@sample-repo.zip" http://localhost:8080/api/v1/analyses/zip

# 5. Clean up canary
docker stop codexa-canary && docker rm codexa-canary
```

---

## 6. Emergency Hotfix Procedure (P1 Vulnerability or Build Failure)

If a critical flaw (P1) is discovered in a production release:

1. **Branch off Release Tag**:
   ```bash
   git checkout -b hotfix/v1.3.1 v1.3.0
   ```
2. **Apply Minimal Targeted Fix**: Fix only the vulnerability or regression; never bundle unrelated features.
3. **Add Failing Regression Test**: Add a reproducing unit test in `com.codexa.rules`.
4. **Expedited Review & Merge**: Require 2 senior reviewer approvals.
5. **Publish Patch Release**: Tag `v1.3.1` following Phases 2–4.
6. **Backport to Main**: Merge hotfix branch back into `main` to prevent regression in future releases.
