package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.nio.file.Path

class PdfSnapshotRegionUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `engine should compare a requested region`() {
        val baseline = "region-baseline".encodeToByteArray()
        val engine = engineWithRenderer(mapOf("region-baseline" to listOf(BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB))))
        val request = SnapshotRequest(name = "page-1", region = PdfRegion(0f, 0f, 20f, 20f))

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
        val engine = engineWithRenderer(mapOf("region-validation" to listOf(BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB))))
        val pdf = "region-validation".encodeToByteArray()

        val error =
            assertThrows(IllegalArgumentException::class.java) {
                engine.run("region-validation.pdf", pdf, pageIndex = 0, requests = emptyList())
            }

        assertEquals("No snapshot regions defined", error.message)
    }

    @Test
    fun `engine should create baseline in region mode when expected file is missing`() {
        val pdf = "new-baseline".encodeToByteArray()
        val engine = engineWithRenderer(mapOf("new-baseline" to listOf(BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB))))
        val request = SnapshotRequest(name = "page-1", region = PdfRegion(0f, 0f, 20f, 20f))

        val report = engine.run("region-baseline.pdf", pdf, pageIndex = 0, requests = listOf(request))

        assertEquals(0, report.results.size)
        assertFalse(report.hasFailures)
        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
        assertTrue(tempDir.resolve("region-baseline.pdf").toFile().exists())
    }

    private fun engineWithRenderer(renderMap: Map<String, List<BufferedImage>>): PdfSnapshotEngine =
        PdfSnapshotEngine(
            config = SnapshotConfig(snapshotsDir = tempDir.toString(), tolerancePercent = 0f, dpi = 72f),
        ) { pdfBytes, _ ->
            renderMap[pdfBytes.decodeToString()] ?: error("No rendered pages for key: ${pdfBytes.decodeToString()}")
        }
}
