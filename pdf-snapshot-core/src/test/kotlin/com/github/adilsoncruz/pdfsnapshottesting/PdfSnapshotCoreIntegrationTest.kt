package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.file.Path

class PdfSnapshotCoreIntegrationTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `engine should create baseline when expected file does not exist`() {
        val config = configForTempDir()
        val engine = PdfSnapshotEngine(config)
        val generated = generatePdf("first page")

        val report = engine.run("baseline.pdf", generated)

        assertEquals(0, report.results.size)
        assertFalse(report.hasFailures)
        assertTrue(tempDir.resolve("baseline.pdf").toFile().exists())
    }

    @Test
    fun `engine should compare generated pdf against baseline`() {
        val config = configForTempDir()
        val engine = PdfSnapshotEngine(config)
        val baseline = generatePdf("same text")

        engine.run("compare.pdf", baseline)
        val report = engine.run("compare.pdf", baseline)

        assertFalse(report.hasFailures)
        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
        assertEquals(1, report.results.size)
    }

    @Test
    fun `engine should return mismatch report for changed content`() {
        val config = configForTempDir()
        val engine = PdfSnapshotEngine(config)

        engine.run("changed.pdf", generatePdf("A"))
        val report = engine.run("changed.pdf", generatePdf("B"))

        assertTrue(report.hasFailures)
        assertEquals(1, report.results.size)
    }

    @Test
    fun `engine should compare files even when page count differs`() {
        val config = configForTempDir()
        val engine = PdfSnapshotEngine(config)

        engine.run("pages.pdf", generatePdf("only one page"))
        val report = engine.run("pages.pdf", generatePdf("first", "second"))

        assertTrue(report.hasFailures)
        assertEquals(1, report.expectedPageSize)
        assertEquals(2, report.generatedPageSize)
        assertEquals(2, report.results.size)
    }

    @Test
    fun `AssertPdfBySnapshot should delegate to engine`() {
        val config = configForTempDir()
        val engine = PdfSnapshotEngine(config)
        val pdfBytes = generatePdf("assert content")

        engine.run("assert.pdf", pdfBytes)

        val report = AssertPdfBySnapshot("assert.pdf", pdfBytes, engine).execute()

        assertFalse(report.hasFailures)
        assertEquals(1, report.results.size)
    }

    @Test
    fun `engine should compare a requested region`() {
        val config = configForTempDir()
        val engine = PdfSnapshotEngine(config)
        val baseline = generatePdf("full page region")
        val request =
            SnapshotRequest(
                name = "page-1",
                region = PdfRegion(
                    x = 0f,
                    yFromTop = 0f,
                    width = 200f,
                    height = 200f,
                ),
            )

        engine.run("region.pdf", baseline)
        val report = engine.run("region.pdf", baseline, pageIndex = 0, requests = listOf(request))

        assertFalse(report.hasFailures)
        assertEquals(1, report.results.size)
        assertEquals("page-1", report.results.first().name)
        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
    }

    @Test
    fun `engine should require at least one snapshot request for region mode`() {
        val config = configForTempDir()
        val engine = PdfSnapshotEngine(config)
        val pdf = generatePdf("region validation")

        val error =
            assertThrows(IllegalArgumentException::class.java) {
                engine.run("region-validation.pdf", pdf, pageIndex = 0, requests = emptyList())
            }

        assertEquals("No snapshot regions defined", error.message)
    }

    @Test
    fun `engine should create baseline in region mode when expected file is missing`() {
        val config = configForTempDir()
        val engine = PdfSnapshotEngine(config)
        val pdf = generatePdf("region baseline")
        val request =
            SnapshotRequest(
                name = "page-1",
                region = PdfRegion(
                    x = 0f,
                    yFromTop = 0f,
                    width = 200f,
                    height = 200f,
                ),
            )

        val report = engine.run("region-baseline.pdf", pdf, pageIndex = 0, requests = listOf(request))

        assertEquals(0, report.results.size)
        assertFalse(report.hasFailures)
        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
        assertTrue(tempDir.resolve("region-baseline.pdf").toFile().exists())
    }

    private fun configForTempDir(): SnapshotConfig {
        return SnapshotConfig(
            snapshotsDir = tempDir.toString(),
            tolerancePercent = 0f,
            dpi = 72f,
        )
    }

    private fun generatePdf(vararg lines: String): ByteArray {
        val out = ByteArrayOutputStream()
        val document = Document()
        PdfWriter.getInstance(document, out)

        document.open()
        lines.forEachIndexed { index, line ->
            if (index > 0) {
                document.newPage()
            }
            document.add(Paragraph(line))
        }
        document.close()

        return out.toByteArray()
    }
}
