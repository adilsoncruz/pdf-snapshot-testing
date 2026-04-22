package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Path

class PdfSnapshotCoreEngineUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `engine should create baseline when expected file does not exist`() {
        val generated = "generated-baseline".encodeToByteArray()
        val engine = engineWithRenderer(mapOf("generated-baseline" to listOf(solidImage())))

        val report = engine.run("baseline.pdf", generated)

        assertEquals(0, report.results.size)
        assertFalse(report.hasFailures)
        assertTrue(tempDir.resolve("baseline.pdf").toFile().exists())
    }

    @Test
    fun `engine should compare generated pdf against baseline`() {
        val baseline = "same-pdf".encodeToByteArray()
        val images = listOf(solidImage())
        val engine = engineWithRenderer(mapOf("same-pdf" to images))

        engine.run("compare.pdf", baseline)
        val report = engine.run("compare.pdf", baseline)

        assertFalse(report.hasFailures)
        assertEquals(1, report.expectedPageSize)
        assertEquals(1, report.generatedPageSize)
        assertEquals(1, report.results.size)
    }

    @Test
    fun `engine should return mismatch report for changed content`() {
        val baseline = "pdf-A".encodeToByteArray()
        val changed = "pdf-B".encodeToByteArray()
        val engine =
            engineWithRenderer(
                mapOf(
                    "pdf-A" to listOf(solidImage(Color.WHITE)),
                    "pdf-B" to listOf(solidImage(Color.BLACK)),
                ),
            )

        engine.run("changed.pdf", baseline)
        val report = engine.run("changed.pdf", changed)

        assertTrue(report.hasFailures)
        assertEquals(1, report.results.size)
    }

    @Test
    fun `engine should compare files even when page count differs`() {
        val baseline = "one-page".encodeToByteArray()
        val generated = "two-pages".encodeToByteArray()
        val engine =
            engineWithRenderer(
                mapOf(
                    "one-page" to listOf(solidImage(Color.WHITE)),
                    "two-pages" to listOf(solidImage(Color.WHITE), solidImage(Color.BLACK)),
                ),
            )

        engine.run("pages.pdf", baseline)
        val report = engine.run("pages.pdf", generated)

        assertTrue(report.hasFailures)
        assertEquals(1, report.expectedPageSize)
        assertEquals(2, report.generatedPageSize)
        assertEquals(2, report.results.size)
    }

    @Test
    fun `AssertPdfBySnapshot should delegate to engine`() {
        val baseline = "assert-content".encodeToByteArray()
        val engine = engineWithRenderer(mapOf("assert-content" to listOf(solidImage())))

        engine.run("assert.pdf", baseline)
        val report = AssertPdfBySnapshot("assert.pdf", baseline, engine).execute()

        assertFalse(report.hasFailures)
        assertEquals(1, report.results.size)
    }

    private fun engineWithRenderer(renderMap: Map<String, List<BufferedImage>>): PdfSnapshotEngine =
        PdfSnapshotEngine(
            config = SnapshotConfig(snapshotsDir = tempDir.toString(), tolerancePercent = 0f, dpi = 72f),
        ) { pdfBytes, _ ->
            renderMap[pdfBytes.decodeToString()] ?: error("No rendered pages for key: ${pdfBytes.decodeToString()}")
        }

    private fun solidImage(color: Color = Color.WHITE): BufferedImage =
        BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB).also { image ->
            val graphics = image.createGraphics()
            graphics.color = color
            graphics.fillRect(0, 0, image.width, image.height)
            graphics.dispose()
        }
}
