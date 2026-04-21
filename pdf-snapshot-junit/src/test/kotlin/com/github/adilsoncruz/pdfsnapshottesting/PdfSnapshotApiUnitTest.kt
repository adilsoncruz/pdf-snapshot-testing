package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.file.Path

class PdfSnapshotApiUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `PdfSnapshotTest should create baseline on first region run`() {
        val config = configForTempDir()
        val baselinePdf = generatePdf("invoice")
        val snapshot =
            PdfSnapshotTest("region-check.pdf", baselinePdf, config)
                .page(0)
                .region("header", PdfRegion(0f, 0f, 200f, 120f))

        val report = snapshot.assertPdfBySnapshot()
        assertEquals(0, report.size)
    }

    @Test
    fun `assertPdfByFile should pass for equal pdf and fail for mismatch`() {
        val config = configForTempDir()
        val baselinePdf = generatePdf("contract A")

        assertDoesNotThrow {
            assertPdfByFile("adapter-check.pdf", baselinePdf, config)
            assertPdfByFile("adapter-check.pdf", baselinePdf, config)
        }

        val mismatchPdf = generatePdf("contract B")
        assertThrows(AssertionError::class.java) {
            assertPdfByFile("adapter-check.pdf", mismatchPdf, config)
        }
    }

    private fun configForTempDir(): SnapshotConfig =
        SnapshotConfig(
            snapshotsDir = tempDir.toString(),
            tolerancePercent = 0f,
            dpi = 72f,
        )

    private fun generatePdf(text: String): ByteArray {
        val out = ByteArrayOutputStream()
        val document = Document()
        PdfWriter.getInstance(document, out)

        document.open()
        document.add(Paragraph(text))
        document.close()

        return out.toByteArray()
    }
}
