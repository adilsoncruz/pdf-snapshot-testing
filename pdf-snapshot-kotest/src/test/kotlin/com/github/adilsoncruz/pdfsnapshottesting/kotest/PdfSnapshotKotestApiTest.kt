package com.github.adilsoncruz.pdfsnapshottesting.kotest

import com.github.adilsoncruz.pdfsnapshottesting.CurrentTest
import com.github.adilsoncruz.pdfsnapshottesting.SnapshotContextManager
import com.github.adilsoncruz.pdfsnapshottesting.SnapshotExecutionContext
import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.openpdf.text.Document
import org.openpdf.text.Paragraph
import org.openpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File

class PdfSnapshotKotestApiTest : FunSpec({
    val output = File("build/pdf-snapshots/raw/PdfSnapshotKotestApiTest-writes_report.json")
    val failOutput = File("build/pdf-snapshots/raw/PdfSnapshotKotestApiTest-failing_test.json")

    afterTest {
        CurrentTest.clear()
        SnapshotContextManager.remove("uid-kotest")
        SnapshotContextManager.remove("uid-kotest-fail")
        output.delete()
        failOutput.delete()
    }

    test("PdfSnapshotTest should reject negative page index") {
        val snapshot = PdfSnapshotTest("negative-page.pdf", generatePdf("invoice"), configForBuildDir())

        shouldThrow<IllegalArgumentException> {
            snapshot.page(-1)
        }
    }

    test("PdfSnapshotTest should keep fluent api when adding region") {
        val snapshot = PdfSnapshotTest("fluent-api.pdf", generatePdf("invoice"), configForBuildDir())

        val sameAfterPage = snapshot.page(0)
        val sameAfterRegion = snapshot.region("header", PdfRegion(0f, 0f, 200f, 120f))
        val sameAfterOverlay = snapshot.drawRegionOverlay("overlay.png")

        sameAfterPage shouldBe snapshot
        sameAfterRegion shouldBe snapshot
        sameAfterOverlay shouldBe snapshot
    }

    test("PdfSnapshotTest should draw overlay when requested") {
        val pdf = generatePdf("kotest overlay test")
        val config = configForBuildDir()
        val dir = File(config.snapshotsDir).apply { mkdirs() }
        File(dir, "overlay-kotest.pdf").delete()

        // first create baseline
        val snapshot1 =
            PdfSnapshotTest("overlay-kotest.pdf", pdf, config)
                .page(0)
                .region("box", PdfRegion(0f, 0f, 100f, 100f))
                .drawRegionOverlay("kotest-overlay.png")
        snapshot1.assertPdfBySnapshot()

        // second run produces overlay
        val snapshot2 =
            PdfSnapshotTest("overlay-kotest.pdf", pdf, config)
                .page(0)
                .region("box", PdfRegion(0f, 0f, 100f, 100f))
                .drawRegionOverlay("kotest-overlay.png")
        snapshot2.assertPdfBySnapshot()

        val overlayFile = File(config.snapshotsResultDir, "overlays/kotest-overlay.png")
        overlayFile.exists() shouldBe true
        overlayFile.delete()
    }

    test("assertPdf should validate text inside a region") {
        assertPdf(generatePdf("Invoice"))
            .page(0)
            .region(0, 0, 400, 200)
            .containsText("Invoice")
    }

    test("PdfSnapshotTest should assert runner report") {
        val snapshot =
            PdfSnapshotTest(
                expectedPdf = "stub.pdf",
                pdfBytes = generatePdf("content"),
                config = configForBuildDir(),
                runner =
                    PdfSnapshotTest.SnapshotRunner { _, _, _, _, _, _, _ ->
                        SnapshotReport(
                            results = listOf(SnapshotResult("ok", "e", "g", "d", true, 0f, 0f)),
                            expectedPageSize = 1,
                            generatedPageSize = 1,
                        )
                    },
            )

        val report = snapshot.assertPdfBySnapshot()

        report.results.size shouldBe 1
    }

    test("assertPdfByFile should fail fast for invalid pdf bytes") {
        shouldThrow<Exception> {
            assertPdfByFile(
                expected = "invalid.pdf",
                pdfBytes = byteArrayOf(1, 2, 3, 4),
                config = configForBuildDir(),
            )
        }
    }

    test("finalizeSnapshotReport should write runtime report when CurrentTest is present") {
        output.delete()

        CurrentTest.set("uid-kotest")
        SnapshotContextManager.put(
            "uid-kotest",
            SnapshotExecutionContext("writes report", "PdfSnapshotKotestApiTest", "uid-kotest"),
        )

        val report =
            SnapshotReport(
                results = listOf(SnapshotResult("p1", "e", "g", "d", true, 0f, 0f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        finalizeSnapshotReport(report)

        output.exists() shouldBe true
    }

    test("finalizeSnapshotReport should throw rich AssertionError with links on failure") {
        CurrentTest.set("uid-kotest-fail")
        SnapshotContextManager.put(
            "uid-kotest-fail",
            SnapshotExecutionContext("failing test", "PdfSnapshotKotestApiTest", "uid-kotest-fail"),
        )

        val report =
            SnapshotReport(
                results = listOf(SnapshotResult("Page 1", "e", "g", "d", false, 0.15f, 0.01f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        val error =
            shouldThrow<AssertionError> {
                finalizeSnapshotReport(report, configForBuildDir())
            }

        error.message shouldContain "PdfSnapshotKotestApiTest > failing test"
        error.message shouldContain "Page 1: difference ratio was 15.00% (tolerance: 0.01%)"
        error.message shouldContain "file://"
    }

    test("assertPdfByFile should create baseline and pass on execution") {
        val pdf = generatePdf("baseline test")
        val config = configForBuildDir()
        val dir = File(config.snapshotsDir).apply { mkdirs() }
        File(dir, "created-baseline.pdf").delete()

        assertPdfByFile("created-baseline.pdf", pdf, config)
        File(dir, "created-baseline.pdf").exists() shouldBe true
    }

    test("PdfSnapshotTest should use default runner when runner is omitted") {
        val pdf = generatePdf("default runner test")
        val config = configForBuildDir()
        val dir = File(config.snapshotsDir).apply { mkdirs() }
        File(dir, "default-runner.pdf").delete()

        val snapshot =
            PdfSnapshotTest(
                expectedPdf = "default-runner.pdf",
                pdfBytes = pdf,
                config = config,
            ).page(0).region("all", PdfRegion(0f, 0f, 200f, 200f))

        val report = snapshot.assertPdfBySnapshot()
        report.results.isEmpty() shouldBe true
    }
})

private fun configForBuildDir(): SnapshotConfig =
    SnapshotConfig(
        snapshotsDir = "build/tmp/pdf-snapshots",
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
