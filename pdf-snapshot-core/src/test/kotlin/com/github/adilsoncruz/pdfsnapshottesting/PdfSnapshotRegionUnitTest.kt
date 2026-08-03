package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.Collections.emptyList

class PdfSnapshotRegionUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `engine should require at least one snapshot request for region mode`() {
        val engine =
            PdfSnapshotEngine(
                SnapshotConfig(
                    snapshotsDir = tempDir.toString(),
                    tolerancePercent = 0f,
                    dpi = 72f,
                ),
            )

        val error =
            assertThrows(IllegalArgumentException::class.java) {
                engine.run("region-validation.pdf", byteArrayOf(1, 2, 3), pageIndex = 0, requests = emptyList())
            }

        assertEquals("No snapshot regions defined", error.message)
    }

    @Test
    fun `engine should extract text from pdf region`() {
        val engine = PdfSnapshotEngine()
        val pdfBytes = generatePdf("Invoice")

        val text =
            engine.extractText(
                pdfBytes = pdfBytes,
                pageIndex = 0,
                region = PdfRegion(0f, 0f, 400f, 200f),
            )

        assertTrue(text.contains("Invoice"))
    }

    @Test
    fun `text assertion should fail when region does not contain expected text`() {
        val assertion =
            PdfTextAssertion(
                pdfBytes = generatePdf("Receipt"),
            )

        val error =
            assertThrows(AssertionError::class.java) {
                assertion
                    .page(0)
                    .region(0, 0, 400, 200)
                    .containsText("Invoice")
            }

        assertEquals(
            "Expected PDF region to contain text 'Invoice', but extracted 'Receipt'.",
            error.message,
        )
    }

    @Test
    fun `text assertion should reject negative page index`() {
        val assertion = PdfTextAssertion(generatePdf("Invoice"))

        val error =
            assertThrows(IllegalArgumentException::class.java) {
                assertion.page(-1)
            }

        assertEquals("Page index must be >= 0", error.message)
    }

    @Test
    fun `engine should reject text extraction when page index is out of bounds`() {
        val engine = PdfSnapshotEngine()

        val error =
            assertThrows(IllegalArgumentException::class.java) {
                engine.extractText(
                    pdfBytes = generatePdf("Invoice"),
                    pageIndex = 1,
                    region = PdfRegion(0f, 0f, 400f, 200f),
                )
            }

        assertEquals("Page index 1 is out of bounds for PDF with 1 pages", error.message)
    }

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
