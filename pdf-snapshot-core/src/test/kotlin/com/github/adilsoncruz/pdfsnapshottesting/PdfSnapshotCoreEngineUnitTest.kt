package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.file.Path

class PdfSnapshotCoreEngineUnitTest {
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
