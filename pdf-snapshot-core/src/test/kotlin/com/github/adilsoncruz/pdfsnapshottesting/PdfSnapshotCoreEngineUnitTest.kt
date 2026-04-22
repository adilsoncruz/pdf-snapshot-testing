package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import org.junit.jupiter.api.Assertions.assertEquals
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
}
