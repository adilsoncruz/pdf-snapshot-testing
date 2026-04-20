package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import java.io.ByteArrayInputStream
import java.io.File

internal object AllureSnapshotAttachmentPublisher {
    private const val ALLURE_CLASS = "io.qameta.allure.Allure"

    fun publish(
        report: SnapshotReport,
        enabled: Boolean,
    ): Int {
        if (!enabled) return 0

        val allure = loadAllureClass() ?: return 0
        val addAttachmentMethod =
            runCatching {
                allure.getMethod(
                    "addAttachment",
                    String::class.java,
                    String::class.java,
                    java.io.InputStream::class.java,
                    String::class.java,
                )
            }.getOrNull() ?: return 0

        var attachedCount = 0

        report.results.forEach { page ->
            attachedCount +=
                attachFileIfExists(
                    addAttachmentMethod = addAttachmentMethod,
                    attachmentName = "${page.name} - expected",
                    filePath = page.expected,
                )
            attachedCount +=
                attachFileIfExists(
                    addAttachmentMethod = addAttachmentMethod,
                    attachmentName = "${page.name} - generated",
                    filePath = page.generated,
                )
            attachedCount +=
                attachFileIfExists(
                    addAttachmentMethod = addAttachmentMethod,
                    attachmentName = "${page.name} - diff",
                    filePath = page.diff,
                )

            val summary =
                """
                page=${page.name}
                matches=${page.matches}
                differenceRatio=${page.differenceRatio}
                tolerancePercentage=${page.tolerancePercentage}
                """.trimIndent()

            attachedCount +=
                runCatching {
                    addAttachmentMethod.invoke(
                        null,
                        "${page.name} - summary",
                        "text/plain",
                        ByteArrayInputStream(summary.toByteArray(Charsets.UTF_8)),
                        "txt",
                    )
                    1
                }.getOrDefault(0)
        }

        return attachedCount
    }

    private fun attachFileIfExists(
        addAttachmentMethod: java.lang.reflect.Method,
        attachmentName: String,
        filePath: String,
    ): Int {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) return 0

        val extension = file.extension.ifBlank { "bin" }
        val mimeType = if (extension.equals("png", ignoreCase = true)) "image/png" else "application/octet-stream"

        return runCatching {
            addAttachmentMethod.invoke(
                null,
                attachmentName,
                mimeType,
                ByteArrayInputStream(file.readBytes()),
                extension,
            )
            1
        }.getOrDefault(0)
    }

    private fun loadAllureClass(): Class<*>? =
        runCatching { Class.forName(ALLURE_CLASS) }.getOrNull()
}
