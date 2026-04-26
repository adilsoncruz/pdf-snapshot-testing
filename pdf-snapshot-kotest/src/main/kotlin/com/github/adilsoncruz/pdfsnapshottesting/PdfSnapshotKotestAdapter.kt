package com.github.adilsoncruz.pdfsnapshottesting.kotest

import com.github.adilsoncruz.pdfsnapshottesting.CurrentTest
import com.github.adilsoncruz.pdfsnapshottesting.SnapshotContextManager
import com.github.adilsoncruz.pdfsnapshottesting.AssertPdfBySnapshot
import com.github.adilsoncruz.pdfsnapshottesting.PdfSnapshotEngine
import com.github.adilsoncruz.pdfsnapshottesting.SnapshotResultFileWriter
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.assertAll

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

internal fun finalizeSnapshotReport(
    report: SnapshotReport,
    config: SnapshotConfig = SnapshotConfig(),
) {
    CurrentTest.get()?.let { uniqueId ->
        SnapshotContextManager.get(uniqueId)?.let { context ->
            SnapshotResultFileWriter.write(context, report, config)
        }
    }

    report.assertAll()
}
