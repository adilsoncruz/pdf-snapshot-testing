package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.DiffResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotPageResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import javax.imageio.ImageIO

/**
 * Core engine responsible for executing PDF snapshot tests.
 *
 * This class is framework-agnostic and does NOT throw assertions.
 * It returns a SnapshotReport describing the results.
 */
public class PdfSnapshotEngine(
    private val config: SnapshotConfig = SnapshotConfig(),
) {
    public fun run(
        expectedPdf: String,
        pdfBytes: ByteArray,
    ): SnapshotReport {
        val pages = renderPages(pdfBytes, config.dpi)
        val pagesResults = mutableListOf<SnapshotPageResult>()

        val expectedPdfFile = expectedFile(expectedPdf)

        if (!expectedPdfFile.exists()) {
            expectedPdfFile.writeBytes(pdfBytes)
            return SnapshotReport(results = emptyList())
        }

        val expectedSnapshots = renderPages(expectedPdfFile.readBytes(), config.dpi)

        pages.mapIndexed { index, actualSnapshot ->
            val expectedSnapshot: BufferedImage = expectedSnapshots[index]
            val diff = buildDiffImage(expectedSnapshot, actualSnapshot, config)

            pagesResults +=
                SnapshotPageResult(
                    index,
                    expectedSnapshot.toBase64(),
                    actualSnapshot.toBase64(),
                    diff.image.toBase64(),
                    diff.matches,
                )
        }
        return SnapshotReport(results = pagesResults)
    }

    private fun renderPages(
        pdfBytes: ByteArray,
        dpi: Float = 144f,
    ): List<BufferedImage> {
        PDDocument.load(pdfBytes).use { document ->
            val renderer = PDFRenderer(document)

            return (0 until document.numberOfPages).map { pageIndex ->
                renderer.renderImageWithDPI(
                    pageIndex,
                    dpi,
                    ImageType.RGB,
                )
            }
        }
    }

    private fun expectedFile(name: String): File = File(config.snapshotsDir, name)

    private fun buildDiffImage(
        expected: BufferedImage,
        generated: BufferedImage,
        config: SnapshotConfig,
    ): DiffResult {
        val width = expected.width
        val height = expected.height
        val totalPixels = width * height
        var differentPixels = 0
        val diffImage =
            BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB,
            )
        val minor = config.minorPixelThreshold
        val blur = config.diffBlurRadius

        for (y in 0 until height) {
            for (x in 0 until width) {
                val e = Color(expected.getRGB(x, y), true)
                val a = Color(generated.getRGB(x, y), true)

                val delta =
                    kotlin.math.abs(e.red - a.red) +
                        kotlin.math.abs(e.green - a.green) +
                        kotlin.math.abs(e.blue - a.blue)

                val stable = delta <= minor

                if (stable) {
                    diffImage.setRGB(
                        x,
                        y,
                        Color(e.red, e.green, e.blue).rgb,
                    )
                } else {
                    differentPixels++

                    val eLuma = e.red + e.green + e.blue
                    val aLuma = a.red + a.green + a.blue

                    val color =
                        if (aLuma < eLuma) {
                            Color(255, 0, 0, 180)
                        } else {
                            Color(0, 120, 255, 180)
                        }

                    drawDiffPixel(
                        image = diffImage,
                        x = x,
                        y = y,
                        color = color,
                        radius = blur,
                    )
                }
            }
        }

        val differenceRatio =
            differentPixels.toFloat() / totalPixels.toFloat()

        val matches =
            differenceRatio <= config.tolerancePercent

        return DiffResult(diffImage, matches)
    }

    private fun drawDiffPixel(
        image: BufferedImage,
        x: Int,
        y: Int,
        color: Color,
        radius: Int,
    ) {
        for (dy in -radius..radius) {
            for (dx in -radius..radius) {
                val px = x + dx
                val py = y + dy
                if (px in 0 until image.width && py in 0 until image.height) {
                    image.setRGB(px, py, color.rgb)
                }
            }
        }
    }

    private fun BufferedImage.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(this, "png", outputStream)
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }
}
