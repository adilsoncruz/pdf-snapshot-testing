package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotPageResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import io.qameta.allure.Allure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class AllureSnapshotAttachmentPublisherUnitTest {
    @BeforeEach
    fun setup() {
        Allure.clearAttachments()
    }

    @Test
    fun `should publish expected generated diff and summary for each snapshot page`() {
        val expected = tempPng("expected")
        val generated = tempPng("generated")
        val diff = tempPng("diff")

        val report =
            SnapshotReport(
                testName = "test",
                className = "AnyTest",
                results =
                    listOf(
                        SnapshotPageResult(
                            name = "invoice-page-1",
                            expected = expected.absolutePath,
                            generated = generated.absolutePath,
                            diff = diff.absolutePath,
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

    private fun tempPng(prefix: String): File {
        val file = File.createTempFile(prefix, ".png")
        file.writeBytes(byteArrayOf(1, 2, 3, 4))
        file.deleteOnExit()
        return file
    }
}
