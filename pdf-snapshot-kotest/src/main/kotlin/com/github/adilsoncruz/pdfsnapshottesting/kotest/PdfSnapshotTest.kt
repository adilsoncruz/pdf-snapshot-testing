package com.github.adilsoncruz.pdfsnapshottesting.kotest

import com.github.adilsoncruz.pdfsnapshottesting.PdfSnapshotEngine
import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotRequest

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

    public fun page(index: Int): PdfSnapshotTest {
        require(index >= 0) { "Page index must be >= 0" }
        this.pageIndex = index
        return this
    }

    public fun region(
        name: String,
        region: PdfRegion,
    ): PdfSnapshotTest {
        regions += SnapshotRequest(name, region)
        return this
    }

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

    public fun assertPdfBySnapshot(): SnapshotReport {
        val report = this.execute()
        finalizeSnapshotReport(report, config)
        return report
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
