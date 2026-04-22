package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Path

class PdfSnapshotCoreEngineUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `engine should require at least one snapshot request for region mode`() {
        val engine = engine()

        val error =
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
                engine.run("region-validation.pdf", byteArrayOf(1, 2, 3), pageIndex = 0, requests = emptyList())
            }

        assertEquals("No snapshot regions defined", error.message)
    }

    @Test
    fun `engine should convert pdf region to image coordinates using dpi scale`() {
        val engine = engine()
        val method = PdfSnapshotEngine::class.java.getDeclaredMethod("convertPdfRegionToImageRegion", PdfRegion::class.java, Float::class.javaPrimitiveType)
        method.isAccessible = true

        val region = method.invoke(engine, PdfRegion(10f, 20f, 30f, 40f), 144f)

        val clazz = region!!::class.java
        assertEquals(20, clazz.getDeclaredField("x").apply { isAccessible = true }.get(region))
        assertEquals(40, clazz.getDeclaredField("y").apply { isAccessible = true }.get(region))
        assertEquals(60, clazz.getDeclaredField("width").apply { isAccessible = true }.get(region))
        assertEquals(80, clazz.getDeclaredField("height").apply { isAccessible = true }.get(region))
    }

    @Test
    fun `engine should process region diff for identical snapshots`() {
        val engine = engine()
        val processRegion =
            PdfSnapshotEngine::class.java.getDeclaredMethod(
                "processRegion",
                BufferedImage::class.java,
                BufferedImage::class.java,
                SnapshotRequest::class.java,
            )
        processRegion.isAccessible = true

        val expected = solidImage()
        val generated = solidImage()
        val request = SnapshotRequest("header", PdfRegion(0f, 0f, 16f, 16f))

        val result = processRegion.invoke(engine, expected, generated, request)
        val resultClass = result!!::class.java

        assertEquals("header", resultClass.getDeclaredField("name").apply { isAccessible = true }.get(result))
        assertTrue(resultClass.getDeclaredField("matches").apply { isAccessible = true }.get(result) as Boolean)
    }

    @Test
    fun `engine should create baseline on first full run`() {
        val engine = engine()
        val pdfBytes = onePagePdf()

        val report = engine.run("baseline-full.pdf", pdfBytes)

        assertTrue(tempDir.resolve("baseline-full.pdf").toFile().exists())
        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
        assertTrue(report.results.isEmpty())
    }

    @Test
    fun `engine should compare full run against existing baseline`() {
        val engine = engine()
        val pdfBytes = onePagePdf()
        engine.run("compare-full.pdf", pdfBytes)

        val report = engine.run("compare-full.pdf", pdfBytes)

        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
        assertEquals(1, report.results.size)
        assertEquals("Page 1", report.results.first().name)
        assertTrue(report.results.first().matches)
        assertFalse(report.hasFailures)
    }

    @Test
    fun `engine should create baseline on first region run`() {
        val engine = engine()
        val pdfBytes = onePagePdf()

        val report = engine.run(
            expectedPdf = "baseline-region.pdf",
            pdfBytes = pdfBytes,
            pageIndex = 0,
            requests = listOf(SnapshotRequest("header", PdfRegion(0f, 0f, 50f, 50f))),
        )

        assertTrue(tempDir.resolve("baseline-region.pdf").toFile().exists())
        assertTrue(report.results.isEmpty())
        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
    }

    @Test
    fun `engine should compare region run against existing baseline`() {
        val engine = engine()
        val pdfBytes = onePagePdf()
        val requests = listOf(SnapshotRequest("header", PdfRegion(0f, 0f, 50f, 50f)))
        engine.run("compare-region.pdf", pdfBytes, pageIndex = 0, requests = requests)

        val report = engine.run("compare-region.pdf", pdfBytes, pageIndex = 0, requests = requests)

        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
        assertEquals(1, report.results.size)
        assertEquals("header", report.results.first().name)
        assertTrue(report.results.first().matches)
    }

    @Test
    fun `engine should build page comparison report when generated has more pages`() {
        val engine = engine()
        val report = engine.buildPageComparisonReport(
            pages = listOf(solidImage(Color.WHITE), solidImage(Color.BLACK)),
            expectedSnapshots = listOf(solidImage(Color.WHITE)),
        )

        assertEquals(1, report.expectedPageSize)
        assertEquals(2, report.generatedPageSize)
        assertEquals(2, report.results.size)
        assertEquals("Page 2", report.results.last().name)
    }

    @Test
    fun `engine should build page comparison report when expected has more pages`() {
        val engine = engine()
        val report = engine.buildPageComparisonReport(
            pages = listOf(solidImage(Color.WHITE)),
            expectedSnapshots = listOf(solidImage(Color.WHITE), solidImage(Color.BLACK)),
        )

        assertEquals(2, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
        assertEquals(2, report.results.size)
        assertEquals("Page 2", report.results.last().name)
    }

    @Test
    fun `engine should build region comparison results for requests`() {
        val engine = engine()
        val results =
            engine.buildRegionComparisonResults(
                expectedImage = solidImage(Color.WHITE),
                pageImage = solidImage(Color.WHITE),
                requests = listOf(SnapshotRequest("header", PdfRegion(0f, 0f, 16f, 16f))),
            )

        assertEquals(1, results.size)
        assertEquals("header", results.first().name)
        assertTrue(results.first().matches)
    }

    private fun engine(): PdfSnapshotEngine =
        PdfSnapshotEngine(
            SnapshotConfig(
                snapshotsDir = tempDir.toString(),
                tolerancePercent = 0f,
                dpi = 72f,
            ),
        )

    private fun solidImage(color: Color = Color.WHITE): BufferedImage =
        BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB).also { image ->
            val graphics = image.createGraphics()
            graphics.color = color
            graphics.fillRect(0, 0, image.width, image.height)
            graphics.dispose()
        }

    private fun onePagePdf(): ByteArray {
        val document = PDDocument()
        document.use {
            it.addPage(PDPage(PDRectangle(200f, 200f)))
            val output = ByteArrayOutputStream()
            it.save(output)
            return output.toByteArray()
        }
    }
}
