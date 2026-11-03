# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-11-03

### Added
- Core rendering and visual diff engine (`pdf-snapshot-core`).
- Configurable pixel threshold and region-based snapshot assertions.
- Modernized HTML report generation and runtime diagnostics (`pdf-snapshot-runtime`).
- JUnit 5 extension and assertion API (`pdf-snapshot-junit`).
- Kotest extension and test matchers (`pdf-snapshot-kotest`).
- Clickable `file://` hyperlinks in IDE console failure output.
- Full POM metadata, license, SCM, and bundled sources JARs.
- Contributor and AI agent guide (`AGENTS.md`).
- Automated CI pipeline with commitlint, ktlint, detekt, and JaCoCo >= 95% coverage verification.

### Changed
- Upgraded Apache PDFBox to `3.0.8` using `Loader.loadPDF`.
- Upgraded Jackson to `2.22.4` resolving all known CVEs.
- Upgraded OpenPDF to `3.0.5`.
- Upgraded Mockito-Kotlin to `6.4.0` and Detekt to `1.23.8`.
