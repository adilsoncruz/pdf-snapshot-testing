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
    finalizeSnapshotReport(report, config)
}

internal fun finalizeSnapshotReport(
    report: SnapshotReport,
    config: SnapshotConfig = SnapshotConfig(),
) {
    val runtimeContext = CurrentTest.get()?.let { uniqueId -> SnapshotContextManager.get(uniqueId) }

    val context =
        runtimeContext
            ?: SnapshotExecutionContext(
                testName = report.testName ?: "snapshot assertion",
                className = report.className.ifBlank { "StandaloneSnapshotReport" },
            )

    SnapshotResultFileWriter.write(context, report, config)

    if (runtimeContext == null) {
        generateSnapshotHtmlReport(config)
            ?: writeSingleSnapshotHtmlReport(context, report, config)
    }

    report.assertAll()
}
