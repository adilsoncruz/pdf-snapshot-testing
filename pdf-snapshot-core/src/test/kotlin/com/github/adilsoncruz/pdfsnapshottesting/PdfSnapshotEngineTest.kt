package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PdfSnapshotEngineTest {
    val config = SnapshotConfig(tolerancePercent = 0.0001f)

    @BeforeAll
    fun clearSnapShots(): Unit {
        val dir = File(config.snapshotsDir)

        if (dir.exists()) {
            dir.deleteRecursively()
        }

        dir.mkdirs()
    }

    @Test
    fun `should generate test result with Failure`() {
        val expectedPdfFile = File(config.snapshotsDir, "expected.pdf")
        val expectedPdf = generateSimplePdf()
        val newPdf = generateSimplePdf("Mock Diff Text")

        val pdfSnapshotEngine = PdfSnapshotEngine(config = config)
        expectedPdfFile.writeBytes(expectedPdf)

        val result =
            pdfSnapshotEngine.run(
                expectedPdfFile.name,
                newPdf,
            )
        assertTrue(result.hasFailures)
        expectedPdfFile.delete()
    }

    @Test
    fun `should generate test result without Failure`() {
        val expectedPdfFile = File(config.snapshotsDir, "expected.pdf")
        val expectedPdf = generateSimplePdf()

        val pdfSnapshotEngine = PdfSnapshotEngine(config = config)
        if (!expectedPdfFile.exists()) {
            expectedPdfFile.writeBytes(expectedPdf)
        }
        val result =
            pdfSnapshotEngine.run(
                expectedPdfFile.name,
                expectedPdf,
            )
        assertFalse(result.hasFailures)
    }

    @Test
    fun `should generate expected file when not exist`() {
        val generatedFile = File(config.snapshotsDir, "expected_not_exist.pdf")
        val generated = generateSimplePdf()
        val pdfSnapshotEngine = PdfSnapshotEngine(config = config)

        generatedFile.writeBytes(generated)
        val result =
            pdfSnapshotEngine.run(
                generatedFile.name,
                generated,
            )
        assertFalse(result.hasFailures)
        generatedFile.delete()
    }

    private fun generateSimplePdf(paragraph: String = "Hello PDF Snapshot"): ByteArray {
        val out = ByteArrayOutputStream()
        val document = Document()
        PdfWriter.getInstance(document, out)

        document.open()
        document.add(Paragraph(paragraph))
        document.close()

        return out.toByteArray()
    }
}
