# PDF Snapshot Testing (JVM)

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![JVM](https://img.shields.io/badge/JVM-21-ED8B00?logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.10-02303A?logo=gradle&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-5.10-25A162?logo=junit5&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

Visual snapshot testing for PDFs on the JVM.
Test **what users see** instead of comparing unstable PDF internals.

---

## What this library is for

*pdf-snapshot-testing* helps you validate rendered PDF output in automated tests.

Traditional byte-by-byte PDF comparison is brittle because PDFs can change internally even when the visual output is identical (metadata, object ordering, compression, font embedding, etc.).

This library renders PDFs to images and compares the visual result, which makes tests significantly more stable for real-world document generation scenarios.

---

## Main benefits

- **Visual-first validation**: verifies rendered content, not binary structure.
- **More stable tests**: avoids false negatives caused by non-deterministic PDF internals.
- **Region-based assertions**: focus checks on specific areas of a page.
- **Multiple snapshots per document**: assert several regions in one test.
- **JUnit 5 integration**: easy usage in existing JVM test suites.
- **Debug/report artifacts**: generates runtime artifacts for diagnosis.

---

## Modules

- `pdf-snapshot-core`: rendering and diff engine.
- `pdf-snapshot-runtime`: runtime report serialization and HTML report generation.
- `pdf-snapshot-junit`: JUnit 5 API and extension helpers.
- `pdf-snapshot-kotest`: Kotest API and lifecycle extension helpers.

---

## Installation

Use the module that matches your test framework:

### JUnit

```kotlin
dependencies {
    testImplementation("io.github.adilsoncruz:pdf-snapshot-junit:<version>")
}
```

`pdf-snapshot-junit` packages core/runtime APIs for JUnit consumers.

### Kotest

```kotlin
dependencies {
    testImplementation("io.github.adilsoncruz:pdf-snapshot-kotest:<version>")
}
```

`pdf-snapshot-kotest` packages core/runtime APIs for Kotest consumers under `com.github.adilsoncruz.pdfsnapshottesting.kotest` to avoid class conflicts with the JUnit module.

> For local development in this repository, the current project version is `0.1.0`.

If you publish/consume through GitHub Packages, declare the repository in your build:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/adilsoncruz/pdf-snapshot-testing")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}
```

---

## Quick start with `assertPdfByFile`

`assertPdfByFile` is the simplest API. It compares a generated PDF (`ByteArray`) against a baseline snapshot file name.

Use the import that matches the test framework module you installed:

- JUnit: `com.github.adilsoncruz.pdfsnapshottesting.assertPdfByFile`
- Kotest: `com.github.adilsoncruz.pdfsnapshottesting.kotest.assertPdfByFile`

```kotlin
import com.github.adilsoncruz.pdfsnapshottesting.assertPdfByFile
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import org.junit.jupiter.api.Test

class InvoicePdfTest {

    @Test
    fun `should match invoice pdf`() {
        val generatedPdf: ByteArray = generateInvoicePdf()

        assertPdfByFile(
            expected = "invoice.pdf",
            pdfBytes = generatedPdf,
            config = SnapshotConfig(
                snapshotsDir = "src/test/resources/pdf-snapshots",
                snapshotsResultDir = "build/pdf-snapshots"
            )
        )
    }
}
```

### When to use

Use `assertPdfByFile` when you want a **single full-page comparison** with minimal ceremony.

---

## Using `PdfSnapshotTest` for fluent/advanced scenarios

`PdfSnapshotTest` provides a fluent API for page selection and region-based snapshots.

```kotlin
import com.github.adilsoncruz.pdfsnapshottesting.PdfSnapshotTest
import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class InvoiceRegionsPdfTest {

    @TestFactory
    fun `should match key regions`: Collection<DynamicTest> {
        val generatedPdf: ByteArray = generateInvoicePdf()

        return PdfSnapshotTest(
            expectedPdf = "invoice.pdf",
            pdfBytes = generatedPdf,
            config = SnapshotConfig()
        )
            .page(0)
            .region("header", PdfRegion(0f, 0f, 550f, 120f))
            .region("totals", PdfRegion(350f, 650f, 220f, 140f))
            .assertPdfBySnapshot()
    }
}
```

### Optional debug overlay

You can call `drawRegionOverlay()` while building the fluent chain to generate visual aid artifacts for coordinate debugging.

---

## Text assertions with `assertPdf`

You can also assert text content directly within a PDF page or isolated region without full visual rendering:

```kotlin
import com.github.adilsoncruz.pdfsnapshottesting.assertPdf
// or import com.github.adilsoncruz.pdfsnapshottesting.kotest.assertPdf for Kotest

// Assert text exists anywhere on page 0
assertPdf(pdfBytes)
    .page(0)
    .containsText("Invoice #1234")

// Assert text exists inside a specific bounded region
assertPdf(pdfBytes)
    .page(0)
    .region(x = 0, y = 0, width = 400, height = 150)
    .containsText("Total: $1,250.00")
```

---

## Rich IDE failure feedback

When a snapshot fails, the test framework throws a descriptive `AssertionError` with clickable `file://` hyperlinks formatted for IDEs (IntelliJ IDEA / Android Studio):

```text
PDF snapshot mismatch in InvoiceSpec > renders invoice pdf:
  • Page 1: difference ratio was 4.50% (tolerance: 0.01%)
    Diff:      file:///workspace/build/pdf-snapshots/failures/InvoiceSpec/renders_invoice_pdf/page-1-diff.png
    Generated: file:///workspace/build/pdf-snapshots/failures/InvoiceSpec/renders_invoice_pdf/page-1-generated.png
    Expected:  file:///workspace/build/pdf-snapshots/failures/InvoiceSpec/renders_invoice_pdf/page-1-expected.png

Consolidated HTML report:
  file:///workspace/build/pdf-snapshots/index.html
```

- Clicking any image URL opens the PNG image directly inside the IDE editor.
- The failure images (`expected`, `generated`, and `diff`) are persisted under `<snapshotsResultDir>/failures/`.

---

## Enabling lifecycle integration

### JUnit (`@EnablePdfSnapshots`)

`@EnablePdfSnapshots` registers the snapshot extension for a test class.
When enabled, snapshot runtime artifacts are finalized into a consolidated HTML report at the end of the test session.

```kotlin
import com.github.adilsoncruz.pdfsnapshottesting.EnablePdfSnapshots
import org.junit.jupiter.api.Test

@EnablePdfSnapshots
class InvoiceSnapshotSuite {

    @Test
    fun `some snapshot-producing test`() {
        // call assertPdfByFile(...) or PdfSnapshotTest(...)
    }
}
```

### Kotest (`SnapshotKotestExtension`)

Register `SnapshotKotestExtension` in your Kotest project configuration so snapshot runtime artifacts are finalized into the consolidated HTML report after the Kotest project finishes.

```kotlin
import com.github.adilsoncruz.pdfsnapshottesting.kotest.SnapshotKotestExtension
import io.kotest.core.config.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SnapshotKotestExtension)
}
```

## Snapshot report output

When JUnit `@EnablePdfSnapshots` or Kotest `SnapshotKotestExtension` is active, snapshot assertions generate runtime artifacts and the HTML report is consolidated at the end of the test session.
This avoids generating HTML for every assertion and keeps test execution faster.

- Default report location: `build/pdf-snapshots/index.html` (or `<snapshotsResultDir>/index.html` when customized in `SnapshotConfig`)
- Raw intermediate files are processed and cleaned after report generation

Use this report to inspect:

- per-test snapshot results
- expected vs generated images
- diff output for failures

You can open it locally after running tests:

```bash
open build/pdf-snapshots/index.html
# or on Linux
xdg-open build/pdf-snapshots/index.html
```

---

## Default configuration (`SnapshotConfig`)

Important defaults:

- `snapshotsDir = "src/test/resources/pdf-snapshots"`
- `snapshotsResultDir = "build/pdf-snapshots"`
- `dpi = 300f`
- `tolerancePercent = 0.001f`

Tune these values to match your quality/performance needs.

---

## What this library does **not** do

- It does **not** generate PDFs.
- It does **not** compare PDFs byte-by-byte.
- It does **not** validate semantic/document structure.
- It does **not** replace business assertions.

It is focused on validating final rendered output.

---

## License

MIT
