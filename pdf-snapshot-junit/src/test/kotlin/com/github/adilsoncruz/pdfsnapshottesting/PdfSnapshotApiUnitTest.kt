package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path

class PdfSnapshotApiUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @AfterEach
    fun resetAdapterExecutor() {
        snapshotAssertionExecutor = ::executeSnapshotAssertion
    }

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
        assertThrows(IllegalStateException::class.java) { tests[1].executable.execute() }
    }

    @Test
    fun `assertPdfByFile should delegate to executor and pass on matching report`() {
        val report =
            SnapshotReport(
                results = listOf(SnapshotResult("p1", "e", "g", "d", true, 0f, 0f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        assertDoesNotThrow {
            assertPdfByFile(
                expected = "expected.pdf",
                pdfBytes = generatePdf("content"),
                config = configForTempDir(),
            ) { expected, _, _ ->
                assertEquals("expected.pdf", expected)
                report
            }
        }
    }

    @Test
    fun `assertPdfByFile should throw on mismatching report`() {
        val report =
            SnapshotReport(
                results = listOf(SnapshotResult("p1", "e", "g", "d", false, 0.3f, 0f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        assertThrows(AssertionError::class.java) {
            assertPdfByFile(
                expected = "expected.pdf",
                pdfBytes = generatePdf("content"),
                config = configForTempDir(),
            ) { _, _, _ -> report }
        }
    }

    @Test
    fun `PdfSnapshotTest should write runtime report when CurrentTest is present`() {
        val output = File("build/pdf-snapshots/raw/PdfSnapshotApiUnitTest.json")
        output.delete()

        val uniqueId = "uid-pdf-snapshot-test"
        CurrentTest.set(uniqueId)
        SnapshotContextManager.put(uniqueId, SnapshotExecutionContext("writes report", "PdfSnapshotApiUnitTest", uniqueId))

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
    fun `assertPdfByFile should execute public adapter path with configured executor`() {
        var called = false
        val report =
            SnapshotReport(
                results = listOf(SnapshotResult("p1", "e", "g", "d", true, 0f, 0f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )
        snapshotAssertionExecutor = { expected, _, _ ->
            called = true
            assertEquals("public-adapter-baseline.pdf", expected)
            report
        }

        assertDoesNotThrow {
            assertPdfByFile(
                expected = "public-adapter-baseline.pdf",
                pdfBytes = generatePdf("public adapter"),
                config = configForTempDir(),
            )
        }

        assertTrue(called)
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
