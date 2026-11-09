package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import com.github.adilsoncruz.pdfsnapshottesting.models.assertAll
import org.junit.jupiter.api.DynamicTest

public class PdfSnapshotTest(
    private val expectedPdf: String,
    private val pdfBytes: ByteArray,
    private val config: SnapshotConfig = SnapshotConfig(),
    private val runner: SnapshotRunner = defaultSnapshotRunner(),
) {
    private var pageIndex: Int = 0
    private val regions = mutableListOf<SnapshotRequest>()

    // Overlay config (optional)
    private var drawOverlay: Boolean = false
    private var overlayName: String? = null

    /**
     * Selects the page index to snapshot.
     */
    public fun page(index: Int): PdfSnapshotTest {
        require(index >= 0) { "Page index must be >= 0" }
        this.pageIndex = index
        return this
    }

    /**
     * Adds a snapshot region.
     */
    public fun region(
        name: String,
        region: PdfRegion,
    ): PdfSnapshotTest {
        regions += SnapshotRequest(name, region)
        return this
    }

    /**
     * Enables debug overlay rendering.
     *
     * @param name Optional output file. If not provided,
     *             the engine default location is used.
     */
    public fun drawRegionOverlay(name: String? = null): PdfSnapshotTest {
        this.drawOverlay = true
        this.overlayName = name
        return this
    }

    private fun execute(): SnapshotReport {
        return runner.run(
            expectedPdf = expectedPdf,
            pdfBytes = pdfBytes,
            pageIndex = pageIndex,
            requests = regions,
            config = config,
            drawOverlay = drawOverlay,
            overlayName = overlayName,
        )
    }

    public fun assertPdfBySnapshot(): Collection<DynamicTest> {
        val report = this.execute()
        var enrichedReport = report

        CurrentTest.get()?.let { uniqueId ->
            SnapshotContextManager.get(uniqueId)?.let { context ->
                SnapshotResultFileWriter.write(context, report, config)
                enrichedReport =
                    report.copy(
                        testName = context.testName,
                        className = context.className,
                    )
            }
        }

        val dynamicTest =
            enrichedReport.results.map { result: SnapshotResult ->
                DynamicTest.dynamicTest(result.name) {
                    if (result.matches) {
                        Unit
                    } else {
                        enrichedReport.copy(results = listOf(result)).assertAll(config)
                    }
                }
            }

        return dynamicTest
    }

    public fun interface SnapshotRunner {
        public fun run(
            expectedPdf: String,
            pdfBytes: ByteArray,
            pageIndex: Int,
            requests: List<SnapshotRequest>,
            config: SnapshotConfig,
            drawOverlay: Boolean,
            overlayName: String?,
        ): SnapshotReport
    }

    private companion object {
        fun defaultSnapshotRunner(): SnapshotRunner =
            SnapshotRunner { expectedPdf, pdfBytes, pageIndex, requests, config, drawOverlay, overlayName ->
                PdfSnapshotEngine(config).run(
                    expectedPdf = expectedPdf,
                    pdfBytes = pdfBytes,
                    pageIndex = pageIndex,
                    requests = requests,
                    drawOverlay = drawOverlay,
                    overlayName = overlayName,
                )
            }
    }
}
