package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.BoundingBox
import com.github.adilsoncruz.pdfsnapshottesting.models.ImageRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import com.github.adilsoncruz.pdfsnapshottesting.models.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ModelAndAssertionUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should expose default snapshot config values`() {
        val config = SnapshotConfig()

        assertEquals(300f, config.dpi)
        assertEquals(10, config.pixelThreshold)
        assertEquals("src/test/resources/pdf-snapshots", config.snapshotsDir)
        assertFalse(config.writeActualOnFailure)
    }

    @Test
    fun `should create full page pdf region`() {
        val full = PdfRegion.fullPage(pageWidth = 595f, pageHeight = 842f)

        assertEquals(PdfRegion(0f, 0f, 595f, 842f), full)
    }

    @Test
    fun `should create immutable model classes`() {
        val bbox = BoundingBox(1, 2, 3, 4)
        val imageRegion = ImageRegion(10, 20, 30, 40)
        val request = SnapshotRequest("invoice", PdfRegion(0f, 0f, 50f, 50f))

        assertEquals(1, bbox.x)
        assertEquals(20, imageRegion.y)
        assertEquals("invoice", request.name)
    }

    @Test
    fun `should identify failures in snapshot report`() {
        val passing = SnapshotResult("page-1", "e", "g", "d", true, 0f, 0.01f)
        val failing = SnapshotResult("page-2", "e", "g", "d", false, 0.2f, 0.01f)

        val report = SnapshotReport(results = listOf(passing, failing), expectedPageSize = 2, generatedPageSize = 2)

        assertTrue(report.hasFailures)
        val error = assertThrows(AssertionError::class.java) { report.assertAll() }
        assertTrue(error.message!!.contains("PDF snapshot mismatch in PDF snapshot:"))
        assertTrue(error.message!!.contains("page-2: difference ratio was 20.00% (tolerance: 0.01%)"))
        assertTrue(error.message!!.contains("Consolidated HTML report:"))
    }

    @Test
    fun `should persist failure images and format file links when base64 is provided`() {
        val tinyPngBase64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        val failing =
            SnapshotResult(
                name = "Page 1",
                expected = tinyPngBase64,
                generated = tinyPngBase64,
                diff = tinyPngBase64,
                matches = false,
                differenceRatio = 0.045f,
                tolerancePercentage = 0.01f,
            )

        val tempResultsDir = File("build/test-failures-check")
        tempResultsDir.deleteRecursively()
        val config = SnapshotConfig(snapshotsResultDir = tempResultsDir.path)

        val report =
            SnapshotReport(
                results = listOf(failing),
                className = "InvoiceSpec",
                testName = "renders invoice pdf",
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        val error = assertThrows(AssertionError::class.java) { report.assertAll(config) }

        assertTrue(error.message!!.contains("InvoiceSpec > renders invoice pdf"))
        assertTrue(error.message!!.contains("Page 1: difference ratio was 4.50% (tolerance: 0.01%)"))
        assertTrue(error.message!!.contains("file://"))
        assertTrue(error.message!!.contains("page-1-diff.png"))

        val expectedDiffFile = File(tempResultsDir, "failures/InvoiceSpec/renders_invoice_pdf/page-1-diff.png")
        assertTrue(expectedDiffFile.exists())
        tempResultsDir.deleteRecursively()
    }

    @Test
    fun `should pass assertAll when all pages match`() {
        val passing = SnapshotResult("page-1", "e", "g", "d", true, 0f, 0.01f)
        val report = SnapshotReport(results = listOf(passing), expectedPageSize = 1, generatedPageSize = 1)

        report.assertAll()
        assertFalse(report.hasFailures)
    }

    @Test
    fun `should expose all snapshot result fields`() {
        val boxes = listOf(BoundingBox(1, 2, 3, 4))
        val result =
            SnapshotResult(
                name = "page-1",
                expected = "e",
                generated = "g",
                diff = "d",
                matches = false,
                differenceRatio = 0.2f,
                tolerancePercentage = 0.01f,
                addedBoxes = boxes,
                removedBoxes = boxes,
                changedBoxes = boxes,
            )

        assertEquals("page-1", result.name)
        assertEquals("e", result.expected)
        assertEquals("g", result.generated)
        assertEquals("d", result.diff)
        assertFalse(result.matches)
        assertEquals(0.2f, result.differenceRatio)
        assertEquals(0.01f, result.tolerancePercentage)
        assertEquals(boxes, result.addedBoxes)
        assertEquals(boxes, result.removedBoxes)
        assertEquals(boxes, result.changedBoxes)
    }

    @Test
    fun `should cover AssertPdfBySnapshot execute call`() {
        val engine =
            PdfSnapshotEngine(
                SnapshotConfig(
                    snapshotsDir = tempDir.toString(),
                    tolerancePercent = 0f,
                    dpi = 72f,
                ),
            )
        val assertion = AssertPdfBySnapshot("invalid.pdf", byteArrayOf(1, 2, 3), engine)

        assertThrows(Exception::class.java) { assertion.execute() }
    }
}
