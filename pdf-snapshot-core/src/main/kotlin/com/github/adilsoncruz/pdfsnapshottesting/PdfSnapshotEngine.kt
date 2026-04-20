package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.DiffResult
import com.github.adilsoncruz.pdfsnapshottesting.models.ImageRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotPageResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReportByRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
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
            val baselineResults =
                pages.mapIndexed { index, snapshot ->
                    val pageBase64 = snapshot.toBase64()
                    SnapshotPageResult(
                        "Page ${index + 1}",
                        pageBase64,
                        pageBase64,
                        pageBase64,
                        true,
                        0f,
                        config.tolerancePercent,
                        emptyList(),
                        emptyList(),
                        emptyList(),
                    )
                }
            return SnapshotReport(results = baselineResults, expectedPageSize = pages.size, generatedPageSize = pages.size)
        }

        val expectedSnapshots = renderPages(expectedPdfFile.readBytes(), config.dpi)

        if (pages.size >= expectedSnapshots.size)
            {
                pages.mapIndexed { index, actualSnapshot ->
                    val expectedSnapshot: BufferedImage =
                        if (index < expectedSnapshots.size) {
                            expectedSnapshots[index]
                        } else {
                            BufferedImage(
                                actualSnapshot.width,
                                actualSnapshot.height,
                                BufferedImage.TYPE_INT_ARGB,
                            )
                        }
                    val diff = DiffByBoudingBox.buildDiffImageWithBoundingBox(expectedSnapshot, actualSnapshot, config)

                    pagesResults +=
                        SnapshotPageResult(
                            "Page ${index + 1}",
                            diff.expectedHighlighted.toBase64(),
                            diff.generatedHighlighted.toBase64(),
                            diff.diffImage.toBase64(),
                            diff.matches,
                            diff.differenceRatio,
                            diff.tolerancePercentage,
                            diff.addedBoxes,
                            diff.removedBoxes,
                            diff.changedBoxes,
                        )
                }
                return SnapshotReport(
                    expectedPageSize = expectedSnapshots.size,
                    generatedPageSize = pages.size,
                    results = pagesResults,
                )
            } else {
            expectedSnapshots.mapIndexed { index, expectedSnapshot ->
                val actualSnapshot: BufferedImage =
                    if (index < pages.size) {
                        pages[index]
                    } else {
                        BufferedImage(
                            expectedSnapshot.width,
                            expectedSnapshot.height,
                            BufferedImage.TYPE_INT_ARGB,
                        )
                    }
                val diff = DiffByBoudingBox.buildDiffImageWithBoundingBox(expectedSnapshot, actualSnapshot, config)
                pagesResults +=
                    SnapshotPageResult(
                        "Page ${index + 1}",
                        diff.expectedHighlighted.toBase64(),
                        diff.generatedHighlighted.toBase64(),
                        diff.diffImage.toBase64(),
                        diff.matches,
                        diff.differenceRatio,
                        diff.tolerancePercentage,
                        diff.addedBoxes,
                        diff.removedBoxes,
                        diff.changedBoxes,
                    )
            }
            return SnapshotReport(
                expectedPageSize = expectedSnapshots.size,
                generatedPageSize = pages.size,
                results = pagesResults,
            )
        }
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

    private fun deepCopy(original: BufferedImage): BufferedImage {
        val copy =
            BufferedImage(
                original.width,
                original.height,
                BufferedImage.TYPE_INT_ARGB,
            )
        val g = copy.createGraphics()
        g.drawImage(original, 0, 0, null)
        g.dispose()
        return copy
    }

    private fun buildDiffImage(
        expected: BufferedImage,
        generated: BufferedImage,
        config: SnapshotConfig,
    ): DiffResult {
        val width = expected.width
        val height = expected.height
        val totalPixels = width * height
        var differentPixels = 0

        // Imagens originais (somente leitura)
        val expectedOriginal = expected
        val generatedOriginal = generated

        // Imagens para highlight (mutáveis)
        val expectedHighlighted = deepCopy(expected)
        val generatedHighlighted = deepCopy(generated)

        val diffImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        val minor = config.minorPixelThreshold
        val blur = config.diffBlurRadius

        for (y in 0 until height) {
            for (x in 0 until width) {
                val e = Color(expectedOriginal.getRGB(x, y), true)
                val g = Color(generatedOriginal.getRGB(x, y), true)

                val delta =
                    kotlin.math.abs(e.red - g.red) +
                        kotlin.math.abs(e.green - g.green) +
                        kotlin.math.abs(e.blue - g.blue)

                if (delta <= minor) {
                    diffImage.setRGB(x, y, e.rgb)
                    continue
                }

                differentPixels++

                val expectedHasContent = hasContent(e)
                val generatedHasContent = hasContent(g)

                when {
                    // 🔵 Conteúdo removido (existia no expected)
                    expectedHasContent && !generatedHasContent -> {
                        val blue = Color(0, 120, 255, 200)

                        drawDiffPixel(diffImage, x, y, blue, blur)
                        drawDiffPixel(expectedHighlighted, x, y, blue, blur)
                    }

                    // 🔴 Conteúdo adicionado (existe só no generated)
                    !expectedHasContent && generatedHasContent -> {
                        val red = Color(255, 0, 0, 200)

                        drawDiffPixel(diffImage, x, y, red, blur)
                        drawDiffPixel(generatedHighlighted, x, y, red, blur)
                    }

                    // 🟣 Ambos têm conteúdo mas são diferentes (mudança visual)
                    else -> {
                        val purple = Color(180, 0, 180, 200)
                        drawDiffPixel(diffImage, x, y, purple, blur)
                    }
                }
            }
        }

        val differenceRatio =
            differentPixels.toFloat() / totalPixels.toFloat()

        val matches =
            differenceRatio <= config.tolerancePercent

        return DiffResult(
            diffImage = diffImage,
            expectedHighlighted = expectedHighlighted,
            generatedHighlighted = generatedHighlighted,
            matches = matches,
            differenceRatio = differenceRatio,
            tolerancePercentage = config.tolerancePercent,
        )
    }

    private fun hasContent(c: Color): Boolean {
        // Considera branco como fundo
        return !(c.red > 245 && c.green > 245 && c.blue > 245)
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

    public fun run(
        expectedPdf: String,
        pdfBytes: ByteArray,
        pageIndex: Int,
        requests: List<SnapshotRequest> = emptyList(),
        drawOverlay: Boolean = false,
        overlayName: String? = null
    ): SnapshotReport {

        require(requests.isNotEmpty()) {
            "No snapshot regions defined"
        }

        val expectedPdfFile = expectedFile(expectedPdf)
        val pages = renderPages(pdfBytes, config.dpi)
        if (!expectedPdfFile.exists()) {
            expectedPdfFile.writeBytes(pdfBytes)
            val pageImage = pages[pageIndex]
            val baselineResults =
                requests.map { request ->
                    processBaselineRegion(
                        pageImage = pageImage,
                        request = request,
                    )
                }
            return SnapshotReport(results = baselineResults, expectedPageSize = pages.size, generatedPageSize = pages.size)
        }

        val pageImage = pages[pageIndex]
        val expectedImage =  renderPages(expectedPdfFile.readBytes(), config.dpi)[pageIndex]

        val results = requests.map { request ->
            processRegion(
                expectedImage = expectedImage,
                pageImage = pageImage,
                request = request,
            )
        }

        return SnapshotReport(
            expectedPageSize = 1,
            generatedPageSize = 1,
            results = results
        )
    }

    private fun processRegion(
        expectedImage: BufferedImage,
        pageImage: BufferedImage,
        request: SnapshotRequest,
    ): SnapshotPageResult {

        val imageRegion = convertPdfRegionToImageRegion(
            request.region,
            config.dpi
        )

        val expectedImageRegion = expectedImage.getSubimage(
            imageRegion.x,
            imageRegion.y,
            imageRegion.width,
            imageRegion.height
        )

        val actualSnapshotRegion = pageImage.getSubimage(
            imageRegion.x,
            imageRegion.y,
            imageRegion.width,
            imageRegion.height
        )

        val diff = DiffByBoudingBox.buildDiffImageWithBoundingBox(expectedImageRegion, actualSnapshotRegion, config)

        return SnapshotPageResult(
            request.name,
            diff.expectedHighlighted.toBase64(),
            diff.generatedHighlighted.toBase64(),
            diff.diffImage.toBase64(),
            diff.matches,
            diff.differenceRatio,
            diff.tolerancePercentage,
            diff.addedBoxes,
            diff.removedBoxes,
            diff.changedBoxes,
        )
    }

    private fun processBaselineRegion(
        pageImage: BufferedImage,
        request: SnapshotRequest,
    ): SnapshotPageResult {
        val imageRegion = convertPdfRegionToImageRegion(request.region, config.dpi)
        val snapshotRegion =
            pageImage.getSubimage(
                imageRegion.x,
                imageRegion.y,
                imageRegion.width,
                imageRegion.height,
            )

        val regionBase64 = snapshotRegion.toBase64()
        return SnapshotPageResult(
            request.name,
            regionBase64,
            regionBase64,
            regionBase64,
            true,
            0f,
            config.tolerancePercent,
            emptyList(),
            emptyList(),
            emptyList(),
        )
    }

    private fun convertPdfRegionToImageRegion(
        pdfRegion: PdfRegion,
        dpi: Float
    ): ImageRegion {
        val scale = dpi / 72f

        return ImageRegion(
            x = (pdfRegion.x * scale).toInt(),
            y = (pdfRegion.yFromTop * scale).toInt(),
            width = (pdfRegion.width * scale).toInt(),
            height = (pdfRegion.height * scale).toInt()
        )
    }

}
