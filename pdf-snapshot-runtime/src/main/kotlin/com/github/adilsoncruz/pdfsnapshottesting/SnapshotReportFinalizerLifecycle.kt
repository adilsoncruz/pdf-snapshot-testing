package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.assertAll

/**
 * Handles test execution enrichment, writing raw report results for HTML consolidation,
 * and asserting snapshot report results.
 */
public object SnapshotReportFinalizerLifecycle {
    public fun finalizeReport(
        report: SnapshotReport,
        config: SnapshotConfig = SnapshotConfig(),
    ) {
        var enrichedReport = report
        CurrentTest.get()?.let { uniqueId ->
            SnapshotContextManager.get(uniqueId)?.let { context ->
                SnapshotResultFileWriter.write(context, report, config)
                enrichedReport =
                    report.copy(
                        testName = context.testName,
                        className = context.className,
                    )
            }
        }

        enrichedReport.assertAll(config)
    }
}
