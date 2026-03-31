package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

private object SnapshotIntegrationFixtures {
    var snapshotsDir: Path = Files.createTempDirectory("pdf-snapshot-junit-fixture")
}

@EnablePdfSnapshots
class SnapshotExtensionSampleSpec {
    @Test
    fun `should execute snapshot assertion through junit extension lifecycle`() {
        val pdfBytes = generatePdf("sample content")

        assertPdfBySnapshot(
            expected = "sample.pdf",
            pdfBytes = pdfBytes,
            config =
                SnapshotConfig(
                    snapshotsDir = SnapshotIntegrationFixtures.snapshotsDir.toString(),
                    tolerancePercent = 0f,
                    dpi = 72f,
                ),
        )
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

class PdfSnapshotJUnitIntegrationTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should create runtime artifacts when running test class with snapshot extension`() {
        val resultDir = File("build/pdf-snapshots")
        resultDir.deleteRecursively()

        SnapshotIntegrationFixtures.snapshotsDir = tempDir

        val request =
            LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(SnapshotExtensionSampleSpec::class.java))
                .build()

        val listener = SummaryGeneratingListener()
        val launcher = LauncherFactory.create()

        launcher.registerTestExecutionListeners(listener)
        launcher.execute(request)

        val summary = listener.summary
        assertEquals(1, summary.testsSucceededCount)
        assertEquals(0, summary.testsFailedCount)

        val indexFile = File(resultDir, "index.html")
        assertTrue(indexFile.exists())
        assertFalse(File(resultDir, "raw").exists())

        resultDir.deleteRecursively()
    }
}
