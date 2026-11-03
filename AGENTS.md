# AGENTS.md — AI & Contributor Guide for pdf-snapshot-testing

This document establishes the architecture, design principles, conventions, and verification workflows for AI coding assistants and human contributors working on `pdf-snapshot-testing`.

---

## 1. Project Overview & Philosophy

`pdf-snapshot-testing` is an open-source, framework-agnostic visual snapshot testing library for PDFs on the JVM.

### Core Goals:
- **Zero Boilerplate:** One-liner assertions (`assertPdfByFile(...)`) for full documents or designated regions.
- **Visual Clarity:** Deterministic visual diffing using bounding box algorithms that highlight additions, removals, and changes with readable color overlays.
- **Developer Experience (DX):** Actionable console feedback with clickable `file://` links in IDE terminals (IntelliJ, VS Code) to inspect diff images and consolidated HTML reports instantly.

---

## 2. Multi-Module Architecture

The project is structured into four independent, focused modules:

```
pdf-snapshot-testing/
├── pdf-snapshot-core/       # Core visual engine & diffing algorithms (framework-agnostic)
├── pdf-snapshot-runtime/    # Context management & HTML report rendering
├── pdf-snapshot-junit/      # JUnit 5 Jupiter adapter & extension (@EnablePdfSnapshots)
└── pdf-snapshot-kotest/     # Kotest integration extension & assertions
```

### Module Responsibilities:

| Module | Purpose | Key Classes & Responsibilities |
| :--- | :--- | :--- |
| `pdf-snapshot-core` | Pure PDF rendering & pixel/region diffing. Never throws assertions directly. | `PdfSnapshotEngine`, `DiffByBoudingBox`, `DiffByPixel`, `SnapshotReport`, `SnapshotConfig`, `PdfRegion`. |
| `pdf-snapshot-runtime` | Manages test session state, serialization, and report rendering. | `SnapshotContextManager`, `CurrentTest`, `SnapshotReportRenderer`, `SnapshotResultFileWriter`. |
| `pdf-snapshot-junit` | JUnit 5 extension and assertion API. Enriches reports with test metadata and produces `DynamicTest`. | `SnapshotExtension`, `PdfSnapshotTest`, `PdfSnapshotJUnitAdapter`, `@EnablePdfSnapshots`. |
| `pdf-snapshot-kotest` | Kotest extension and test matchers. Hooks into Kotest lifecycle hooks. | `SnapshotKotestExtension`, `PdfSnapshotKotestAdapter`, `PdfSnapshotTest`. |

---

## 3. Core Engine Mechanics

1. **Baseline Creation vs Comparison:**
   - On the first test run, if no baseline PDF exists in `SnapshotConfig.snapshotsDir`, the engine stores the generated PDF as the baseline and passes.
   - Subsequent runs compare the generated PDF against this baseline.
2. **DPI & Coordinate Scaling:**
   - PDFs use typographic points (72 DPI). The engine renders pages to images at configured DPI (default 300 DPI, test default 144/150 DPI).
   - Coordinates in `PdfRegion(x, y, width, height)` are automatically scaled to raster pixels based on the active DPI.
3. **Bounding Box Visual Diff:**
   - `DiffByBoudingBox` computes delta masks for added (red), removed (blue), and visual changes (purple).
   - Translucent box highlights and scaled stroke borders ensure regressions stand out cleanly on high-res displays.
4. **Rich Failure Reporting:**
   - When snapshots differ beyond `tolerancePercent`, `SnapshotReport.assertAll()` writes individual failure artifacts (`page-X-expected.png`, `page-X-generated.png`, `page-X-diff.png`).
   - The assertion error prints clickable `file://` URIs pointing directly to the generated files and the consolidated `index.html` report.

---

## 4. Strict Quality Gates & Invariants

All changes must satisfy the following non-negotiable rules:

1. **Java 21 Toolchain:** The project targets JVM 21 using Temurin JDK.
2. **Kotlin Explicit API Mode:** Public APIs must explicitly declare visibility (`public`, `internal`, `private`) and return types.
3. **95% Test Coverage Enforcement:**
   - JaCoCo is configured with a hard threshold of **minimum 0.95 (95%) line coverage** on every submodule.
   - Pull requests with < 95% line coverage in any module will fail CI (`jacocoTestCoverageVerification`).
4. **Conventional Commits:**
   - Commit messages must follow Conventional Commits (`feat:`, `fix:`, `chore:`, `test:`, `docs:`, `refactor:`).
   - Header length must not exceed **100 characters** (enforced by `commitlint`).
5. **Linting & Static Analysis:**
   - Code must pass `ktlint` and `detekt` rules without suppression unless explicitly authorized.

---

## 5. Essential Commands

```bash
# Run full verification (ktlint, detekt, compile, tests, coverage verification)
./gradlew check

# Run tests and validate JaCoCo coverage specifically
./gradlew coverage

# Automatically format code according to project style
./gradlew ktlintFormat

# Publish artifacts to local Maven repository (~/.m2/repository)
./gradlew publishToMavenLocal
```

---

## 6. Guidance for AI Agents

- **Preserve Coverage:** Whenever adding a feature or fixing a bug, write corresponding unit tests covering all branch conditions to keep line coverage >= 95%.
- **Respect Modular Boundaries:** `pdf-snapshot-core` must **never** depend on JUnit, Kotest, or test frameworks. It must remain pure and embeddable anywhere on the JVM.
- **Commit Messages:** Ensure commit headers are concise (under 100 characters) and strictly conform to Conventional Commits.
