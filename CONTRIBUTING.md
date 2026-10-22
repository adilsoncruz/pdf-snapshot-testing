# Contributing to PDF Snapshot Testing

Thank you for your interest in contributing! Here are guidelines to help you get started.

## Development Workflow

1. Fork and clone the repository.
2. Ensure you have Java 21 installed (managed automatically via `mise` or your preferred JDK manager).
3. Create a feature branch from `develop`:
   ```bash
   git checkout -b feat/my-new-feature
   ```

## Commit Message Convention

This project enforces [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` new feature or capability
- `fix:` bug fix
- `test:` adding or refactoring tests
- `docs:` documentation changes
- `chore:` maintenance tasks, dependency updates, tooling
- `refactor:` code change that neither fixes a bug nor adds a feature

Example:
```text
feat(core): support custom pixel threshold comparison
```

## Quality and Verification

Before submitting a Pull Request, ensure all checks pass:

```bash
# Run linters, static analysis, and full test suite
./gradlew check

# Verify JaCoCo coverage (minimum 95% line coverage per module required)
./gradlew jacocoTestCoverageVerification
```

## Submitting a Pull Request

1. Push your branch to GitHub.
2. Open a Pull Request targeting the `develop` branch.
3. Fill out the Pull Request template completely.
4. Ensure all CI checks pass.
