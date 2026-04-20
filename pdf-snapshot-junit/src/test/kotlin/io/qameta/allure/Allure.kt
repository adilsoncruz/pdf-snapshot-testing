package io.qameta.allure

import java.io.InputStream

public object Allure {
    public data class Attachment(
        val name: String,
        val type: String,
        val extension: String,
        val content: ByteArray,
    )

    private val records: MutableList<Attachment> = mutableListOf()

    @JvmStatic
    public fun addAttachment(
        name: String,
        type: String,
        content: InputStream,
        extension: String,
    ) {
        records += Attachment(name, type, extension, content.readBytes())
    }

    @JvmStatic
    public fun clearAttachments() {
        records.clear()
    }

    @JvmStatic
    public fun attachments(): List<Attachment> = records.toList()
}
