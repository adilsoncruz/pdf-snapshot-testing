package com.github.adilsoncruz.pdfsnapshottesting.kotest

import com.github.adilsoncruz.pdfsnapshottesting.AssertPdfBySnapshot
import com.github.adilsoncruz.pdfsnapshottesting.PdfSnapshotEngine
import com.github.adilsoncruz.pdfsnapshottesting.PdfTextAssertion
import com.github.adilsoncruz.pdfsnapshottesting.SnapshotReportFinalizerLifecycle
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport

public fun assertPdfByFile(
    expected: String,
    pdfBytes: ByteArray,
    config: SnapshotConfig = SnapshotConfig(),
) {
    val report =
        AssertPdfBySnapshot(
            expectedPdf = expected,
            pdfBytes = pdfBytes,
            engine = PdfSnapshotEngine(config),
        ).execute()

    finalizeSnapshotReport(report, config)
}

public fun assertPdf(pdfBytes: ByteArray): PdfTextAssertion = PdfTextAssertion(pdfBytes)

internal fun finalizeSnapshotReport(
    report: SnapshotReport,
    config: SnapshotConfig = SnapshotConfig(),
) {
    SnapshotReportFinalizerLifecycle.finalizeReport(report, config)
}
