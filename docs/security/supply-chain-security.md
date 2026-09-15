# Codexa Software Supply Chain Security & SLSA Compliance

## Overview
Codexa adheres to Supply-chain Levels for Software Artifacts (SLSA) Level 3 requirements to guarantee the integrity of compiled backend and frontend release bundles.

## Key Measures
1. **Reproducible Builds**: Maven and Vite build pipelines declare exact lockfile checksums.
2. **SBOM Generation**: CycloneDX and SPDX Software Bill of Materials (SBOM) JSON files are automatically published alongside each GitHub Release.
3. **Container Signing (Cosign)**: Production Docker images are cryptographically signed using Sigstore Cosign in GitHub Actions:
   ```bash
   cosign verify --key cosign.pub ghcr.io/codewithjainam7/codexa:latest
   ```
4. **Automated Dependency Auditing**: RenovateBot and Dependabot continuously monitor transiting libraries for CVE alerts.
