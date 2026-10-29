package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.openpdf.text.Document
import org.openpdf.text.Paragraph
import org.openpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
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

    @Test
    fun `PdfSnapshotTest should keep fluent api when drawing overlay without name`() {
        val snapshot = PdfSnapshotTest("fluent-overlay-default.pdf", generatePdf("invoice"), configForTempDir())

        val sameAfterOverlay = snapshot.drawRegionOverlay()

        assertSame(snapshot, sameAfterOverlay)
    }

    @Test
    fun `assertPdf should validate text inside a region`() {
        assertDoesNotThrow {
            assertPdf(generatePdf("Invoice"))
                .page(0)
                .region(0, 0, 400, 200)
                .containsText("Invoice")
        }
    }

    @Test
    fun `PdfSnapshotTest should create dynamic tests from runner results`() {
        val report =
            SnapshotReport(
                results =
                    listOf(
                        SnapshotResult("ok", "e", "g", "d", true, 0f, 0f),
                        SnapshotResult("fail", "e", "g", "d", false, 0.2f, 0f),
                    ),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )
        val snapshot =
            PdfSnapshotTest(
                expectedPdf = "stub.pdf",
                pdfBytes = generatePdf("content"),
                config = configForTempDir(),
                runner = PdfSnapshotTest.SnapshotRunner { _, _, _, _, _ -> report },
            )

        val tests = snapshot.assertPdfBySnapshot().toList()

        assertEquals(2, tests.size)
        assertEquals("ok", tests[0].displayName)
        assertEquals("fail", tests[1].displayName)
        assertDoesNotThrow { tests[0].executable.execute() }
        val error = assertThrows(AssertionError::class.java) { tests[1].executable.execute() }
        assertTrue(error.message!!.contains("fail: difference ratio was 20.00%"))
    }

    @Test
    fun `assertPdfByFile should fail fast for invalid pdf bytes`() {
        assertThrows(Exception::class.java) {
            assertPdfByFile(
                expected = "invalid.pdf",
                pdfBytes = byteArrayOf(1, 2, 3, 4),
                config = configForTempDir(),
            )
        }
    }

    @Test
    fun `finalizeSnapshotReport should assert report without runtime context`() {
        val report =
            SnapshotReport(
                results = listOf(SnapshotResult("p1", "e", "g", "d", true, 0f, 0f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        assertDoesNotThrow { finalizeSnapshotReport(report) }
    }

    @Test
    fun `finalizeSnapshotReport should throw for mismatching report`() {
        val report =
            SnapshotReport(
                results = listOf(SnapshotResult("p1", "e", "g", "d", false, 0.3f, 0f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        assertThrows(AssertionError::class.java) { finalizeSnapshotReport(report) }
    }

    @Test
    fun `finalizeSnapshotReport should write runtime report when CurrentTest is present`() {
        val output = File("build/pdf-snapshots/raw/PdfSnapshotApiUnitTest.json")
        output.delete()

        val uniqueId = "uid-finalize-report"
        CurrentTest.set(uniqueId)
        SnapshotContextManager.put(
            uniqueId,
            SnapshotExecutionContext("writes report", "PdfSnapshotApiUnitTest", uniqueId),
        )

        try {
            val report =
                SnapshotReport(
                    results = listOf(SnapshotResult("p1", "e", "g", "d", true, 0f, 0f)),
                    expectedPageSize = 1,
                    generatedPageSize = 1,
                )
            finalizeSnapshotReport(report)
            assertTrue(output.exists())
        } finally {
            CurrentTest.clear()
            SnapshotContextManager.remove(uniqueId)
            output.delete()
        }
    }

    @Test
    fun `PdfSnapshotTest should write runtime report when CurrentTest is present`() {
        val output = File("build/pdf-snapshots/raw/PdfSnapshotApiUnitTest.json")
        output.delete()

        val uniqueId = "uid-pdf-snapshot-test"
        CurrentTest.set(uniqueId)
        SnapshotContextManager.put(
            uniqueId,
            SnapshotExecutionContext("writes report", "PdfSnapshotApiUnitTest", uniqueId),
        )

        try {
            val report =
                SnapshotReport(
                    results = listOf(SnapshotResult("p1", "e", "g", "d", true, 0f, 0f)),
                    expectedPageSize = 1,
                    generatedPageSize = 1,
                )
            val snapshot =
                PdfSnapshotTest(
                    expectedPdf = "stub.pdf",
                    pdfBytes = generatePdf("content"),
                    config = configForTempDir(),
                    runner = PdfSnapshotTest.SnapshotRunner { _, _, _, _, _ -> report },
                )

            snapshot.assertPdfBySnapshot()

            assertTrue(output.exists())
        } finally {
            CurrentTest.clear()
            SnapshotContextManager.remove(uniqueId)
            output.delete()
        }
    }

    @Test
    fun `assertPdfByFile should create baseline and execute successfully`() {
        val pdf = generatePdf("junit assertPdfByFile")
        val config = configForTempDir()

        assertDoesNotThrow {
            assertPdfByFile("baseline-junit.pdf", pdf, config)
        }

        assertTrue(File(tempDir.toFile(), "baseline-junit.pdf").exists())
    }

    @Test
    fun `PdfSnapshotTest should execute with default runner when runner is not specified`() {
        val pdf = generatePdf("junit default runner")
        val config = configForTempDir()

        val snapshot =
            PdfSnapshotTest(
                expectedPdf = "default-runner-junit.pdf",
                pdfBytes = pdf,
                config = config,
            ).page(0).region("all", PdfRegion(0f, 0f, 100f, 100f))

        val dynamicTests = snapshot.assertPdfBySnapshot()
        assertEquals(0, dynamicTests.size)
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
