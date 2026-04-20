package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotPageResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport

public class SnapshotReportRenderer {
    private val FAILED = "#E53935"
    private val PASSED = "#4CAF50"

    private fun loadTemplate(template: String): String {
        val stream =
            this::class.java
                .getResourceAsStream("/$template")
                ?: error("template.html not found in resources")

        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    public fun generateReport(groupedReports: Map<String, List<SnapshotReport>>): String {
        val reportTemplate = loadTemplate("report-template.html")
        val testSections =
            groupedReports.entries.joinToString("\n") { (_, reports) ->
                val hasFailure = reports.any { report -> report.hasFailures }
                val color = if (hasFailure) FAILED else PASSED
                val template = loadTemplate("test-session.html")

                val testResultSections =
                    reports.map { report ->
                        val metrics = report.results.firstOrNull()
                        val metricsHtml =
                            if (metrics != null) {
                                "<h4>difference ratio: ${metrics.differenceRatio}</br> tolerance percentage: ${metrics.tolerancePercentage}%</h4>"
                            } else {
                                "<h4>No diff data available (snapshot baseline generated).</h4>"
                            }

                        "<h3>${report.testName}</h3>" +
                            metricsHtml +
                            buildTestPageSection(report)
                    }.joinToString("")
                template
                    .replace("{{className}}", reports.first().className)
                    .replace("{{result}}", testResultSections)
                    .replace("{{color}}", color)
            }

        return reportTemplate.replace("{{testSections}}", testSections, true)
    }

    private fun buildTestPageSection(report: SnapshotReport): String {
        if (report.results.isEmpty()) {
            return "<p>No snapshot pages were compared in this run.</p>"
        }

        val template = loadTemplate("snapshot-session.html")
        val pages = mutableListOf<String>()
        report.results.map { result: SnapshotPageResult ->
            pages.add(
                template
                    .replace("{{color}}", PASSED.takeIf { result.matches } ?: FAILED)
                    .replace("{{page}}", result.name)
                    .replace("{{expected}}", result.expected)
                    .replace("{{diff}}", result.diff)
                    .replace("{{generated}}", result.generated),
            )
        }

        return pages.joinToString("")
    }
}
