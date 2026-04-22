package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.assertAll

internal var snapshotAssertionExecutor: (expected: String, pdfBytes: ByteArray, config: SnapshotConfig) -> SnapshotReport =
    ::executeSnapshotAssertion

internal fun resetSnapshotAssertionExecutor() {
    snapshotAssertionExecutor = ::executeSnapshotAssertion
}

public fun assertPdfByFile(
    expected: String,
    pdfBytes: ByteArray,
    config: SnapshotConfig = SnapshotConfig(),
) {
    val report = snapshotAssertionExecutor(expected, pdfBytes, config)

    CurrentTest.get().let { uniqueId ->
        if (uniqueId != null) {
            SnapshotResultFileWriter.write(SnapshotContextManager.get(uniqueId)!!, report)
        }
    }

    return report.assertAll()
}

private fun executeSnapshotAssertion(expected: String, pdfBytes: ByteArray, config: SnapshotConfig): SnapshotReport =
    AssertPdfBySnapshot(expectedPdf = expected, pdfBytes = pdfBytes, engine = PdfSnapshotEngine(config)).execute()
