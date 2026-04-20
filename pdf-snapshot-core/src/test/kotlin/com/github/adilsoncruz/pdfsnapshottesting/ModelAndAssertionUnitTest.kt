package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.BoundingBox
import com.github.adilsoncruz.pdfsnapshottesting.models.ImageRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import com.github.adilsoncruz.pdfsnapshottesting.models.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class ModelAndAssertionUnitTest {
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
        val passing = SnapshotResult(0, "e", "g", "d", true, 0f, 0.01f)
        val failing = SnapshotResult(1, "e", "g", "d", false, 0.2f, 0.01f)

        val report = SnapshotReport(results = listOf(passing, failing), expectedPageSize = 2, generatedPageSize = 2)

        assertTrue(report.hasFailures)
        assertThrows(AssertionError::class.java) { report.assertAll() }
    }

    @Test
    fun `should pass assertAll when all pages match`() {
        val passing = SnapshotResult(0, "e", "g", "d", true, 0f, 0.01f)
        val report = SnapshotReport(results = listOf(passing), expectedPageSize = 1, generatedPageSize = 1)

        report.assertAll()
        assertFalse(report.hasFailures)
    }

    @Test
    fun `should keep snapshot result subtype contracts`() {
        val region = PdfRegion(1f, 2f, 3f, 4f)
        val success = SnapshotResult.Success("name", region, matches = true)
        val created = SnapshotResult.Created("name", File("snapshot.pdf"), region)
        val firstCreate = SnapshotResult.FirstCreate("name", region)
        val mismatch = SnapshotResult.Mismatch("name", File("expected.pdf"), region, matches = false)

        assertTrue(success.matches)
        assertNull(created.matches)
        assertNull(firstCreate.matches)
        assertFalse(mismatch.matches)
    }
}
