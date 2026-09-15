# Codexa Release Process & Versioning Checklist

## Pre-Release Checks
- [ ] Ensure `main` branch GitHub Actions `Frontend CI` and `Backend CI` are 100% green.
- [ ] Run local `mvn clean verify -Ddependency-check.skip=true` to confirm all 104+ unit tests pass.
- [ ] Run `npm run lint` and `npm run build` in `frontend/`.
- [ ] Sync compiled frontend dist to backend static resources.

## Tagging & Publishing
1. Bump version in `backend/pom.xml` and `frontend/package.json`.
2. Commit: `chore(release): prepare v1.1.0`.
3. Create annotated Git tag:
   ```bash
   git tag -a v1.1.0 -m "Release v1.1.0 - Autonomous Code Review Platform"
   git push origin v1.1.0
   ```
4. Release workflow triggers container builds and attaches compiled JAR / APK artifacts to GitHub Releases.
