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

class AssertPdfBySnapshotUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `execute should delegate to engine and return comparison report`() {
        val engine = PdfSnapshotEngine(configForTempDir())
        val expectedName = "assert-unit.pdf"
        val baselinePdf = generatePdf("same content")

        engine.run(expectedName, baselinePdf)

        val report = AssertPdfBySnapshot(expectedName, baselinePdf, engine).execute()

        assertFalse(report.hasFailures)
        assertEquals(1, report.results.size)
        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
    }

    @Test
    fun `execute should surface mismatch from engine`() {
        val engine = PdfSnapshotEngine(configForTempDir())
        val expectedName = "assert-mismatch.pdf"

        engine.run(expectedName, generatePdf("A"))

        val report = AssertPdfBySnapshot(expectedName, generatePdf("B"), engine).execute()

        assertTrue(report.hasFailures)
        assertEquals(1, report.results.size)
    }

    private fun configForTempDir(): SnapshotConfig =
        SnapshotConfig(
            snapshotsDir = tempDir.toString(),
            tolerancePercent = 0f,
            dpi = 72f,
        )

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
