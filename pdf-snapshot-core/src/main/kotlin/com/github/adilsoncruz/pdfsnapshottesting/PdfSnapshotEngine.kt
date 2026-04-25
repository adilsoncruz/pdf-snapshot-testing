package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.ImageRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
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

        val expectedPdfFile = expectedFile(expectedPdf)

        if (!expectedPdfFile.exists()) {
            expectedPdfFile.writeBytes(pdfBytes)
            return SnapshotReport(results = emptyList(), expectedPageSize = pages.size, generatedPageSize = pages.size)
        }

        val expectedSnapshots = renderPages(expectedPdfFile.readBytes(), config.dpi)
        return buildPageComparisonReport(pages, expectedSnapshots)
    }

    private fun expectedFile(name: String): File = File(config.snapshotsDir, name)

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
    ): SnapshotReport {
        require(requests.isNotEmpty()) {
            "No snapshot regions defined"
        }

        val expectedPdfFile = expectedFile(expectedPdf)
        val pages = renderPages(pdfBytes, config.dpi)
        if (!expectedPdfFile.exists()) {
            expectedPdfFile.writeBytes(pdfBytes)
            return SnapshotReport(results = emptyList(), expectedPageSize = pages.size, generatedPageSize = pages.size)
        }

        val pageImage = pages[pageIndex]
        val expectedImage = renderPages(expectedPdfFile.readBytes(), config.dpi)[pageIndex]

        val results = buildRegionComparisonResults(expectedImage, pageImage, requests)

        return SnapshotReport(
            expectedPageSize = 1,
            generatedPageSize = 1,
            results = results,
        )
    }

    private fun processRegion(
        expectedImage: BufferedImage,
        pageImage: BufferedImage,
        request: SnapshotRequest,
    ): SnapshotResult {
        val imageRegion =
            convertPdfRegionToImageRegion(
                request.region,
                config.dpi,
            )

        val expectedImageRegion =
            expectedImage.getSubimage(
                imageRegion.x,
                imageRegion.y,
                imageRegion.width,
                imageRegion.height,
            )

        val actualSnapshotRegion =
            pageImage.getSubimage(
                imageRegion.x,
                imageRegion.y,
                imageRegion.width,
                imageRegion.height,
            )

        val diff = DiffByBoudingBox.buildDiffImageWithBoundingBox(expectedImageRegion, actualSnapshotRegion, config)

        return SnapshotResult(
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

    private fun convertPdfRegionToImageRegion(
        pdfRegion: PdfRegion,
        dpi: Float,
    ): ImageRegion {
        val scale = dpi / 72f

        return ImageRegion(
            x = (pdfRegion.x * scale).toInt(),
            y = (pdfRegion.yFromTop * scale).toInt(),
            width = (pdfRegion.width * scale).toInt(),
            height = (pdfRegion.height * scale).toInt(),
        )
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

    internal fun buildPageComparisonReport(
        pages: List<BufferedImage>,
        expectedSnapshots: List<BufferedImage>,
    ): SnapshotReport {
        val pagesResults = mutableListOf<SnapshotResult>()

        if (pages.size >= expectedSnapshots.size) {
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
                    SnapshotResult(
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
                SnapshotResult(
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

    internal fun buildRegionComparisonResults(
        expectedImage: BufferedImage,
        pageImage: BufferedImage,
        requests: List<SnapshotRequest>,
    ): List<SnapshotResult> =
        requests.map { request ->
            processRegion(
                expectedImage = expectedImage,
                pageImage = pageImage,
                request = request,
            )
        }
}
