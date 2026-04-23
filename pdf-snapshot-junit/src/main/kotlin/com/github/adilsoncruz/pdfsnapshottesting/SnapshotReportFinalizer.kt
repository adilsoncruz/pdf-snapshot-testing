package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import java.io.File

internal fun generateSnapshotHtmlReport(
    config: SnapshotConfig = SnapshotConfig(),
    cleanRawFiles: Boolean = false,
): File? {
    val rawDir = File(config.snapshotsResultDir, "raw")

    val reports =
        rawDir.listFiles()
            ?.map {
                SnapshotJsonSerializer.deserialize(it.readText())
            }
            ?: emptyList()

    if (reports.isEmpty()) {
        return null
    }

    val indexFile = File(config.snapshotsResultDir, "index.html")
    val grouped = reports.groupBy { it.className }

    val html = SnapshotReportRenderer().generateReport(grouped)
    indexFile.writeText(html)

    if (cleanRawFiles) {
        rawDir.deleteRecursively()
    }

    return indexFile
}
