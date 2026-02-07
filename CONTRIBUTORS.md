# Contributing to LineFlow

Thanks for your interest in contributing! This guide will help you get set up.

## Prerequisites

- **JDK 17** or higher — ensure `JAVA_HOME` is set in your shell profile (e.g. `~/.zshrc`, `~/.bashrc`)
- **[pre-commit](https://pre-commit.com/)** — install via `pip install pre-commit` (preferably in a virtual environment)

## Getting Started

1. **Fork and clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/line-flow.git
   cd line-flow
   ```

2. **Install pre-commit hooks**
   ```bash
   pre-commit install
   ```

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on a connected device or emulator**
   ```bash
   ./gradlew installDebug
   ```

## Running Tests

```bash
# Run all unit tests
./gradlew test

# Run a specific test class
./gradlew testDebugUnitTest --tests "com.example.lineflow.LevelValidationTest"
```

## Code Style

- Follow the existing patterns in the codebase
- Keep changes focused and minimal

The project uses three linting tools that run automatically via pre-commit hooks:

| Tool | Command | What it checks |
|------|---------|---------------|
| **Android Lint** | `./gradlew lint` | Android-specific issues, performance, security |
| **ktlint** | `./gradlew ktlintCheck` | Kotlin code style and formatting |
| **detekt** | `./gradlew detekt` | Static analysis, code smells, complexity |

To auto-format code with ktlint:
```bash
./gradlew ktlintFormat
```

To run all checks manually:
```bash
./gradlew lint ktlintCheck detekt
```

## Submitting Changes

1. Create a new branch for your feature or fix
2. Make your changes
3. Ensure tests pass
4. Open a pull request with a clear description of what you changed and why

We appreciate all contributions, big or small!
