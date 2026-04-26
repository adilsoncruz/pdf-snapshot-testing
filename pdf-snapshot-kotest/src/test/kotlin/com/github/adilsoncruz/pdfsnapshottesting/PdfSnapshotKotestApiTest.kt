package com.github.adilsoncruz.pdfsnapshottesting.kotest

import com.github.adilsoncruz.pdfsnapshottesting.SnapshotExecutionContext
import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.File

class PdfSnapshotKotestApiTest : FunSpec({
    val output = File("build/pdf-snapshots/raw/PdfSnapshotKotestApiTest.json")

    afterTest {
        CurrentKotestTest.clear()
        KotestSnapshotContextManager.remove("uid-kotest")
        output.delete()
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

        sameAfterPage shouldBe snapshot
        sameAfterRegion shouldBe snapshot
    }

    test("PdfSnapshotTest should assert runner report") {
        val snapshot =
            PdfSnapshotTest(
                expectedPdf = "stub.pdf",
                pdfBytes = generatePdf("content"),
                config = configForBuildDir(),
                runner =
                    PdfSnapshotTest.SnapshotRunner { _, _, _, _, _ ->
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

        CurrentKotestTest.set("uid-kotest")
        KotestSnapshotContextManager.put(
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
