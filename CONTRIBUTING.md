# Contributing to Codexa

Thank you for contributing to Codexa! As a platform dedicated to static code analysis, security auditing, and production readiness, we maintain rigorous engineering standards across our backend and frontend codebases.

---

## 1. Prerequisites & Development Setup

- **Java Development Kit (JDK)**: Java 17 or Java 21 (Eclipse Temurin recommended)
- **Build Tool**: Apache Maven 3.9+
- **Node.js**: Node 20+ (LTS) & npm 10+
- **Git**: 2.34+

### Clone & Initial Build

```bash
git clone https://github.com/Codewithjainam7/Codexa.git
cd Codexa

# Build and run backend unit tests (135+ tests)
cd backend
mvn test "-Djacoco.skip=true" "-Ddependency-check.skip=true"

# Install frontend dependencies and build assets
cd ../frontend
npm ci
npm run build
```

---

## 2. Running Codexa Locally

You can launch both backend and frontend concurrently for interactive development:

```bash
# Terminal 1: Launch Spring Boot API (Port 8080)
cd backend
mvn spring-boot:run

# Terminal 2: Launch Vite Dev Server with HMR (Port 5173)
cd frontend
npm run dev
```

The Vite dev server proxies API calls (`/api/v1/*`) directly to `http://localhost:8080`.

---

## 3. Git Branching & Conventional Commits

We adhere strictly to [Conventional Commits](https://www.conventionalcommits.org/):

- `feat(...)`: New feature or scanner capability (e.g. `feat(scanner): add GraphQL injection detection`)
- `fix(...)`: Bug fix or false-positive reduction (e.g. `fix(rules): exclude try-with-resources from nesting depth`)
- `docs(...)`: Documentation additions or refinements
- `test(...)`: Adding or updating test suites
- `refactor(...)`: Code refactoring without behavioral modification
- `perf(...)`: Performance and memory optimizations

### Branch Naming
- `feature/description`
- `fix/issue-description`
- `docs/doc-topic`

---

## 4. Authoring New Analysis Rules

All deterministic static analysis rules implement `AnalysisRule.java` and are automatically discovered via Spring component scanning:

1. **Implement `AnalysisRule`**:
   Annotate with `@Component`, declare a unique rule ID (e.g. `CR-SEC-010`), and implement `evaluate(RuleContext context)`.
2. **Defensive Filtering**:
   Use `context.getParsedJavaFile()` for AST analysis or `readFileLines` for polyglot regex inspection.
3. **Mandatory Unit Tests**:
   Every new rule must have a corresponding JUnit 5 test class under `backend/src/test/java/com/codexa/rules/` containing:
   - Positive test cases (vulnerable code flagged with expected severity and start line).
   - Negative test cases (remediated code or defensive patterns not flagged).

---

## 5. Pull Request Submission Checklist

Before opening a PR:
- [ ] All 135+ tests pass cleanly: `mvn test "-Djacoco.skip=true" "-Ddependency-check.skip=true"`.
- [ ] Frontend builds without TypeScript or JSX errors: `npm run build`.
- [ ] No secrets, keys, or internal tokens committed.
- [ ] Documentation updated in `docs/` or `README.md`.
- [ ] Commit history is clean and follows conventional commit format.
