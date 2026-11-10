package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import java.io.File

public object SnapshotResultFileWriter {
    private val invalidFileChars = Regex("[^a-zA-Z0-9._-]")

    public fun write(
        context: SnapshotExecutionContext,
        report: SnapshotReport,
        config: SnapshotConfig = SnapshotConfig(),
    ): File {
        val dir = File(config.snapshotsResultDir, "raw")
        dir.mkdirs()

        val baseName = "${sanitize(context.className)}-${sanitize(context.testName)}"
        val file = resolveNextFile(dir, baseName)
        file.writeText(
            SnapshotJsonSerializer.serialize(
                SnapshotReport(
                    testName = context.testName,
                    className = context.className,
                    results = report.results,
                    expectedPageSize = report.expectedPageSize,
                    generatedPageSize = report.generatedPageSize,
                ),
            ),
        )
        return file
    }

    private fun sanitize(value: String): String = value.trim().replace(invalidFileChars, "_")

    private fun resolveNextFile(
        dir: File,
        baseName: String,
    ): File {
        val primary = File(dir, "$baseName.json")
        if (!primary.exists()) {
            return primary
        }
        var sequence = 2
        while (true) {
            val candidate = File(dir, "$baseName-$sequence.json")
            if (!candidate.exists()) {
                return candidate
            }
            sequence++
        }
    }
}
