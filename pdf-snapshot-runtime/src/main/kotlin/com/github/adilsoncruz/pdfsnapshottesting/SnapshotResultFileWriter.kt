package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import java.io.File

public object SnapshotResultFileWriter {
    public fun write(
        context: SnapshotExecutionContext,
        report: SnapshotReport,
    ) {
        val dir = File("build/pdf-snapshots/raw")
        dir.mkdirs()

        val file = File(dir, "${context.className}.json")
        file.writeText(
            SnapshotJsonSerializer.serialize(
                SnapshotReport(
                    testName = context.testName,
                    className = context.className,
                    results = report.results,
                ),
            ),
        )
    }
}
