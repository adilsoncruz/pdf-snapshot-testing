package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
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

class PdfSnapshotRegionUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `engine should compare a requested region`() {
        val engine = PdfSnapshotEngine(configForTempDir())
        val baseline = generatePdf("region baseline")
        val request = SnapshotRequest(name = "page-1", region = PdfRegion(0f, 0f, 200f, 200f))

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
        val engine = PdfSnapshotEngine(configForTempDir())
        val pdf = generatePdf("region validation")

        val error =
            assertThrows(IllegalArgumentException::class.java) {
                engine.run("region-validation.pdf", pdf, pageIndex = 0, requests = emptyList())
            }

        assertEquals("No snapshot regions defined", error.message)
    }

    @Test
    fun `engine should create baseline in region mode when expected file is missing`() {
        val engine = PdfSnapshotEngine(configForTempDir())
        val pdf = generatePdf("new baseline")
        val request = SnapshotRequest(name = "page-1", region = PdfRegion(0f, 0f, 200f, 200f))

        val report = engine.run("region-baseline.pdf", pdf, pageIndex = 0, requests = listOf(request))

        assertEquals(0, report.results.size)
        assertFalse(report.hasFailures)
        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
        assertTrue(tempDir.resolve("region-baseline.pdf").toFile().exists())
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
