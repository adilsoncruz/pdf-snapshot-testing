package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
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

    CurrentTest.get().let { uniqueId ->
        if (uniqueId != null) {
            SnapshotResultFileWriter.write(SnapshotContextManager.get(uniqueId)!!, report)
        }
    }

    AllureSnapshotAttachmentPublisher.publish(report, config.enableAllureAttachments)

    return report.assertAll()
}

//public fun assertPdfBySnapshot(
//    expected: String,
//    pdfBytes: ByteArray,
//    config: SnapshotConfig = SnapshotConfig(),
//) {
//    val report =
//        PdfSnapshotTest(
//            expectedPdf = expected,
//            pdfBytes = pdfBytes,
//            engine = PdfSnapshotEngine(config),
//        ).execute()
//
//    CurrentTest.get().let { uniqueId ->
//        if (uniqueId != null) {
//            SnapshotResultFileWriter.write(SnapshotContextManager.get(uniqueId)!!, report)
//        }
//    }
//
//    return report.assertAll()
//}
