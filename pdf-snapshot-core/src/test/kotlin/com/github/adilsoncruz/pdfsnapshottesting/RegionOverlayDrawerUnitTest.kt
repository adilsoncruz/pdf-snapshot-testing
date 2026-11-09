package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Path

class RegionOverlayDrawerUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `drawOverlay should create overlay file with custom and default name`() {
        val config = SnapshotConfig(snapshotsResultDir = tempDir.toString())
        val image =
            BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).apply {
                val g = createGraphics()
                g.color = Color.WHITE
                g.fillRect(0, 0, 100, 100)
                g.dispose()
            }
        val requests =
            listOf(
                SnapshotRequest("header", PdfRegion(0f, 0f, 40f, 20f)),
                SnapshotRequest("footer", PdfRegion(0f, 60f, 40f, 20f)),
            )

        val customFile = RegionOverlayDrawer.drawOverlay(image, requests, config, "custom-overlay.png")
        assertTrue(customFile.exists())

        val defaultFile = RegionOverlayDrawer.drawOverlay(image, requests, config)
        assertTrue(defaultFile.exists())
    }
}
