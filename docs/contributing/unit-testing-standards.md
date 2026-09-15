# Codexa Backend Testing Conventions & Quality Gates

## Testing Stack
- **Test Framework**: JUnit 5 (Jupiter)
- **Mocking**: Mockito 5 (`@ExtendWith(MockitoExtension.class)`)
- **Assertions**: AssertJ (`assertThat(...)`)
- **Coverage**: JaCoCo Maven Plugin (Enforced minimum line coverage: >60% across business services)

## Architecture Conventions
1. **Fast Unit Tests**: Unit tests must run purely in-memory without starting the full Spring ApplicationContext whenever possible.
2. **Slice Tests**: Use `@WebMvcTest` or `@DataJpaTest` for isolated component testing.
3. **Deterministic Fixtures**: Avoid relying on current timestamps or random IDs in assertion checks.
