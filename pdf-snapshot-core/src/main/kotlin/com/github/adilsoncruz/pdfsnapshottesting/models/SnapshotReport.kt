package com.github.adilsoncruz.pdfsnapshottesting.models

import java.io.File
import java.util.Base64
import java.util.Locale

public data class SnapshotReport(
    val results: List<SnapshotResult>,
    val testName: String? = null,
    val className: String = "",
    val expectedPageSize: Int,
    val generatedPageSize: Int,
    val reportLink: File? = null,
) {
    val hasFailures: Boolean =
        results.any { !it.matches }
}

public fun SnapshotReport.assertAll(config: SnapshotConfig = SnapshotConfig()) {
    val failures = results.filter { !it.matches }
    if (failures.isNotEmpty()) {
        val message = buildSnapshotFailureMessage(failures, config, className, testName)
        throw AssertionError(message)
    }
}

internal fun buildSnapshotFailureMessage(
    failures: List<SnapshotResult>,
    config: SnapshotConfig,
    className: String,
    testName: String?,
): String {
    val failuresDir = File(config.snapshotsResultDir, "failures")
    val testDir = resolveTestFailureDir(failuresDir, className, testName)

    return buildString {
        val targetName =
            when {
                className.isNotBlank() && !testName.isNullOrBlank() -> "$className > $testName"
                className.isNotBlank() -> className
                !testName.isNullOrBlank() -> testName
                else -> "PDF snapshot"
            }
        appendLine("PDF snapshot mismatch in $targetName:")

        failures.forEach { failed ->
            val pageSlug = failed.name.lowercase().replace(Regex("[^a-zA-Z0-9._-]"), "-")
            val expectedFile = File(testDir, "$pageSlug-expected.png")
            val generatedFile = File(testDir, "$pageSlug-generated.png")
            val diffFile = File(testDir, "$pageSlug-diff.png")

            writeImageIfPresent(failed.expected, expectedFile)
            writeImageIfPresent(failed.generated, generatedFile)
            writeImageIfPresent(failed.diff, diffFile)

            val diffPct = String.format(Locale.US, "%.2f%%", failed.differenceRatio * 100)
            val tolPct = String.format(Locale.US, "%.2f%%", failed.tolerancePercentage)

            appendLine("  • ${failed.name}: difference ratio was $diffPct (tolerance: $tolPct)")
            appendLine("    Diff:      ${toFileLink(diffFile, failed.diff)}")
            appendLine("    Generated: ${toFileLink(generatedFile, failed.generated)}")
            appendLine("    Expected:  ${toFileLink(expectedFile, failed.expected)}")
        }

        val reportIndex = File(config.snapshotsResultDir, "index.html")
        appendLine()
        appendLine("Consolidated HTML report:")
        append("  ${toFileUri(reportIndex)}")
    }
}

private fun resolveTestFailureDir(
    baseDir: File,
    className: String,
    testName: String?,
): File {
    return if (className.isNotBlank()) {
        val sanitizedClass = className.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val sanitizedTest = (testName ?: "").replace(Regex("[^a-zA-Z0-9._-]"), "_").takeIf { it.isNotBlank() }
        if (sanitizedTest != null) File(baseDir, "$sanitizedClass/$sanitizedTest") else File(baseDir, sanitizedClass)
    } else {
        File(baseDir, "diff")
    }.apply { mkdirs() }
}

private fun writeImageIfPresent(
    content: String,
    destination: File,
) {
    if (content.isBlank()) return
    try {
        val bytes = Base64.getDecoder().decode(content)
        destination.parentFile?.mkdirs()
        destination.writeBytes(bytes)
    } catch (_: IllegalArgumentException) {
        val source = File(content)
        if (source.exists() && source.isFile) {
            destination.parentFile?.mkdirs()
            source.copyTo(destination, overwrite = true)
        }
    }
}

private fun toFileLink(
    file: File,
    fallback: String,
): String = if (file.exists()) toFileUri(file) else fallback

private fun toFileUri(file: File): String {
    val path = file.absolutePath
    return if (path.startsWith("/")) "file://$path" else "file:///$path"
}
