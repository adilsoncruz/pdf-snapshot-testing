# PDF Snapshot Testing (JVM)

Visual snapshot testing for PDFs on the JVM.
Test **what users see** instead of comparing unstable PDF internals.

---

## What this library is for

`pdf-snapshot-testing` helps you validate rendered PDF output in automated tests.

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

---

## Installation (`pdf-snapshot-junit`)

Use the JUnit module when writing tests:

```kotlin
dependencies {
    testImplementation("com.github.adilsoncruz:pdf-snapshot-junit:<version>")
}
```

> For local development in this repository, the current project version is `0.1.0-SNAPSHOT`.

---

## Quick start with `assertPdfByFile`

`assertPdfByFile` is the simplest API. It compares a generated PDF (`ByteArray`) against a baseline snapshot file name.

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

## Enabling JUnit lifecycle integration (`@EnablePdfSnapshots`)

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
