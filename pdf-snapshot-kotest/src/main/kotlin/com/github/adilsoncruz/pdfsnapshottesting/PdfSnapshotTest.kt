package com.github.adilsoncruz.pdfsnapshottesting

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

    private fun execute(): SnapshotReport {
        return runner.run(
            expectedPdf = expectedPdf,
            pdfBytes = pdfBytes,
            pageIndex = pageIndex,
            requests = regions,
            config = config,
        )
    }

    public fun assertPdfBySnapshot() {
        val report = this.execute()
        finalizeSnapshotReport(report, config)
    }

    public fun interface SnapshotRunner {
        public fun run(
            expectedPdf: String,
            pdfBytes: ByteArray,
            pageIndex: Int,
            requests: List<SnapshotRequest>,
            config: SnapshotConfig,
        ): SnapshotReport
    }

    private companion object {
        fun defaultSnapshotRunner(): SnapshotRunner =
            SnapshotRunner { expectedPdf, pdfBytes, pageIndex, requests, config ->
                PdfSnapshotEngine(config).run(expectedPdf, pdfBytes, pageIndex, requests)
            }
    }
}
