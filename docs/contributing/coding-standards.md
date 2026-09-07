# Codexa Coding Standards

- Java 17+ / 21+ idioms with record classes and sealed interfaces.
- React 18 functional components with Tailwind CSS.
- 100% test coverage on all security rules.


### JavaParser Rule Development Conventions

1. Always extend `VoidVisitorAdapter<List<Finding>>` for AST node traversal.
2. Mask sensitive tokens using `CodeMaskingService` before emitting evidence snippets.
3. Ensure all rules have corresponding positive and negative unit test fixtures.
