package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.assertAll

public fun assertPdfByFile(
    expected: String,
    pdfBytes: ByteArray,
    config: SnapshotConfig = SnapshotConfig(),
) {
    val report = AssertPdfBySnapshot(expectedPdf = expected, pdfBytes = pdfBytes, engine = PdfSnapshotEngine(config)).execute()
    finalizeSnapshotReport(report)
}

internal fun finalizeSnapshotReport(report: SnapshotReport) {
    val context =
        CurrentTest.get()?.let { uniqueId -> SnapshotContextManager.get(uniqueId) }
            ?: SnapshotExecutionContext(
                testName = report.testName ?: "snapshot assertion",
                className = report.className.ifBlank { "StandaloneSnapshotReport" },
            )

    SnapshotResultFileWriter.write(context, report)
    generateSnapshotHtmlReport()
    report.assertAll()
}
