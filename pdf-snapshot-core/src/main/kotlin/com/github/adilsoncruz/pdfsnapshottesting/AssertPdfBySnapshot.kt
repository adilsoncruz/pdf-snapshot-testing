package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport

public class AssertPdfBySnapshot(
    private val expectedPdf: String,
    private val pdfBytes: ByteArray,
    private val engine: PdfSnapshotEngine,
) {
    public fun execute(): SnapshotReport {
        return engine.run(
            expectedPdf = expectedPdf,
            pdfBytes = pdfBytes,
        )
    }
}
