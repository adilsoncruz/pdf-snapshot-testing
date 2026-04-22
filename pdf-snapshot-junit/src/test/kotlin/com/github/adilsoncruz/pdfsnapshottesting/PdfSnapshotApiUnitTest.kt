package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.file.Path

class PdfSnapshotApiUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `PdfSnapshotTest should reject negative page index`() {
        val config = configForTempDir()
        val baselinePdf = generatePdf("invoice")
        val snapshot = PdfSnapshotTest("negative-page.pdf", baselinePdf, config)

        assertThrows(IllegalArgumentException::class.java) {
            snapshot.page(-1)
        }
    }

    @Test
    fun `PdfSnapshotTest should keep fluent api when adding region and overlay`() {
        val config = configForTempDir()
        val baselinePdf = generatePdf("invoice")
        val snapshot = PdfSnapshotTest("fluent-api.pdf", baselinePdf, config)

        val sameAfterPage = snapshot.page(0)
        val sameAfterRegion = snapshot.region("header", PdfRegion(0f, 0f, 200f, 120f))
        val sameAfterOverlay = snapshot.drawRegionOverlay("overlay.png")

        assertSame(snapshot, sameAfterPage)
        assertSame(snapshot, sameAfterRegion)
        assertSame(snapshot, sameAfterOverlay)
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
