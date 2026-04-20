package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotPageResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import io.qameta.allure.Allure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Base64

class AllureSnapshotAttachmentPublisherUnitTest {
    @BeforeEach
    fun setup() {
        Allure.clearAttachments()
    }

    @Test
    fun `should publish base64 expected generated diff and summary for each snapshot page`() {
        val pngBase64 = Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3, 4))

        val report =
            SnapshotReport(
                testName = "test",
                className = "AnyTest",
                results =
                    listOf(
                        SnapshotPageResult(
                            name = "invoice-page-1",
                            expected = pngBase64,
                            generated = pngBase64,
                            diff = pngBase64,
                            matches = false,
                            differenceRatio = 0.12f,
                            tolerancePercentage = 0.01f,
                        ),
                    ),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        val attachedCount = AllureSnapshotAttachmentPublisher.publish(report, enabled = true)

        assertEquals(4, attachedCount)
        assertEquals(4, Allure.attachments().size)
        assertEquals("png", Allure.attachments()[0].extension)
    }

    @Test
    fun `should support file paths as attachment sources`() {
        val png = tempPng("expected")

        val report =
            SnapshotReport(
                testName = "test",
                className = "AnyTest",
                results =
                    listOf(
                        SnapshotPageResult(
                            name = "invoice-page-1",
                            expected = png.absolutePath,
                            generated = png.absolutePath,
                            diff = png.absolutePath,
                            matches = true,
                            differenceRatio = 0f,
                            tolerancePercentage = 0.01f,
                        ),
                    ),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        val attachedCount = AllureSnapshotAttachmentPublisher.publish(report, enabled = true)

        assertEquals(4, attachedCount)
        assertEquals(4, Allure.attachments().size)
    }

    @Test
    fun `should do nothing when disabled`() {
        val report =
            SnapshotReport(
                testName = "test",
                className = "AnyTest",
                results = emptyList(),
                expectedPageSize = 0,
                generatedPageSize = 0,
            )

        val attachedCount = AllureSnapshotAttachmentPublisher.publish(report, enabled = false)

        assertEquals(0, attachedCount)
        assertEquals(0, Allure.attachments().size)
    }

    @Test
    fun `should add summary attachment when report has no pages`() {
        val report =
            SnapshotReport(
                testName = "test",
                className = "AnyTest",
                results = emptyList(),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        val attachedCount = AllureSnapshotAttachmentPublisher.publish(report, enabled = true)

        assertEquals(1, attachedCount)
        assertEquals(1, Allure.attachments().size)
        assertEquals("snapshot-summary", Allure.attachments().first().name)
    }

    private fun tempPng(prefix: String): File {
        val file = File.createTempFile(prefix, ".png")
        file.writeBytes(byteArrayOf(1, 2, 3, 4))
        file.deleteOnExit()
        return file
    }
}
