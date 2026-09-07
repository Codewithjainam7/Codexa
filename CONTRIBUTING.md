# Contributing to Codexa

Thank you for contributing to Codexa! Please follow these guidelines to keep our codebase secure, robust, and well-tested.

## Development Setup

### Backend (Spring Boot & Java 17)
```bash
cd backend
mvn clean test
```

### Frontend (React 18 & Vite)
```bash
cd frontend
npm install
npm run lint
npm run build
```

## Pull Request Checklist

Before submitting a pull request, ensure the following requirements are met:
- [ ] **Tests Pass**: Run `mvn test` in `backend/` and `npm run test` / `npm run build` in `frontend/`.
- [ ] **No Hardcoded Secrets**: Ensure no API keys, database passwords, or JWT secrets are committed.
- [ ] **No Wildcard CORS**: Keep CORS allow-lists strictly configured without wildcard origins combined with credentials.
- [ ] **AST Rules Quality**: New AST rules must include positive and negative test fixtures.
- [ ] **Conventional Commits**: Use semantic commit messages (e.g. `feat:`, `fix:`, `docs:`, `refactor:`, `test:`).
- [ ] **Documentation**: Update relevant guides in `docs/` for API or architectural changes.

## Code Style & Standards
- Backend follows standard Java conventions with checkstyle and JaCoCo coverage rules.
- Frontend uses ESLint and Prettier (`npm run format` / `npm run lint`).
