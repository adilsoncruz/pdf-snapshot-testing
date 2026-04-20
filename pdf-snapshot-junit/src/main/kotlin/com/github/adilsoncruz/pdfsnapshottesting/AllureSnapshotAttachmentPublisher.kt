package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Base64

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
                attachContentIfPresent(
                    addAttachmentMethod = addAttachmentMethod,
                    attachmentName = "${page.name} - expected",
                    content = page.expected,
                )
            attachedCount +=
                attachContentIfPresent(
                    addAttachmentMethod = addAttachmentMethod,
                    attachmentName = "${page.name} - generated",
                    content = page.generated,
                )
            attachedCount +=
                attachContentIfPresent(
                    addAttachmentMethod = addAttachmentMethod,
                    attachmentName = "${page.name} - diff",
                    content = page.diff,
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

    private fun attachContentIfPresent(
        addAttachmentMethod: java.lang.reflect.Method,
        attachmentName: String,
        content: String,
    ): Int {
        if (content.isBlank()) return 0

        val file = File(content)
        if (file.exists() && file.isFile) {
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

        val bytes = decodeBase64(content) ?: return 0
        return runCatching {
            addAttachmentMethod.invoke(
                null,
                attachmentName,
                "image/png",
                ByteArrayInputStream(bytes),
                "png",
            )
            1
        }.getOrDefault(0)
    }

    private fun decodeBase64(content: String): ByteArray? {
        val payload = content.substringAfter("base64,", content)
        return runCatching { Base64.getDecoder().decode(payload) }.getOrNull()
    }

    private fun loadAllureClass(): Class<*>? =
        runCatching { Class.forName(ALLURE_CLASS) }.getOrNull()
}
