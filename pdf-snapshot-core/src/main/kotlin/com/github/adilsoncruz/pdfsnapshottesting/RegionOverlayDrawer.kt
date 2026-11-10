package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Utility for drawing debug overlays on page snapshots to visually verify region coordinates.
 */
public object RegionOverlayDrawer {
    public fun drawOverlay(
        pageImage: BufferedImage,
        requests: List<SnapshotRequest>,
        config: SnapshotConfig,
        outputFileName: String? = null,
    ): File {
        val overlayImage =
            BufferedImage(
                pageImage.width,
                pageImage.height,
                BufferedImage.TYPE_INT_ARGB,
            )

        val g2 = overlayImage.createGraphics()
        g2.drawImage(pageImage, 0, 0, null)

        val scale = config.dpi / 72f
        val strokeWidth = maxOf(2f, config.dpi / 100f)
        g2.stroke = BasicStroke(strokeWidth)

        requests.forEachIndexed { index, req ->
            val rx = (req.region.x * scale).toInt()
            val ry = (req.region.yFromTop * scale).toInt()
            val rw = (req.region.width * scale).toInt()
            val rh = (req.region.height * scale).toInt()

            // Translucent fill
            g2.color = Color(0, 120, 255, 40)
            g2.fillRect(rx, ry, rw, rh)

            // Border
            g2.color = Color(0, 120, 255, 220)
            g2.drawRect(rx, ry, rw, rh)

            // Label
            g2.color = Color(0, 70, 180)
            g2.drawString("${req.name} (#${index + 1})", rx + 4, maxOf(14, ry - 4))
        }

        g2.dispose()

        val overlaysDir = File(config.snapshotsResultDir, "overlays").apply { mkdirs() }
        val targetName = outputFileName?.takeIf { it.isNotBlank() } ?: "region-overlay.png"
        val outputFile = File(overlaysDir, targetName)
        ImageIO.write(overlayImage, "png", outputFile)
        return outputFile
    }
}
