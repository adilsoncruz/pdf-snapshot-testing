package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotPageResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport

public class SnapshotReportRenderer {
    private val FAILED = "#E53935"
    private val PASSED = "#4CAF50"

    private fun loadTemplate(): String {
        val stream = this::class.java
            .getResourceAsStream("/template/report.html")
            ?: error("template.html not found in resources")

        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    public fun generateConsolidatedHtml(groupedReports: Map<String, List<SnapshotReport>>): String {
        val testSections =
            groupedReports.entries.joinToString("\n") { ( _, reports) ->
                val hasFailure = reports.any { report -> report.hasFailures }
                val color = if (hasFailure) FAILED else PASSED
                val testResultSections =
                    reports.map { report ->
                        "<h3>${report.testName}</h3>" + buildTestPageSection(report)
                    }.joinToString("")
                """
                <section>
                    <h2 style="margin-top:0;color:$color">
                        ${reports.first().className}
                    </h2>
                    $testResultSections
                </section>
                """.trimIndent()
            }

        return """
              <html>
              <head>
                  <title>PDF Snapshot Report</title>
                  <meta charset="UTF-8">
                  <style>
              body {
                font-family: system-ui;
                background: #FFF;
                color: #303030;
                padding: 16px;
              }
              section {
                border:1px solid #ddd;
                border-radius:10px;
                padding:20px;
                margin-bottom:30px;
              }
              .grid {
                display: grid;
                grid-template-columns: repeat(3, 1fr);
                gap: 12px;
              }
              .cell {
                background: #f5f5f5;
                padding: 8px;
                border-radius: 8px;
                margin-bottom: 5px;
              }
              img {
                width: 100%;
              }
              h2 {
                text-align: left;
                font-size: 14px;
              }
              h4 {
                text-align: right;
              }
            </style>
              </head>
              <body>
                  <h1>PDF Snapshot Test Report</h1>
                  <p>
                  </p>
                  $testSections
              </body>
              </html>
            """.trimIndent()
    }

    private fun buildTestPageSection(report: SnapshotReport): String =
        report.results.mapIndexed { index, result: SnapshotPageResult ->
        """
          <h3>Page ${index+1}</h3>
          <h4>difference ratio: ${report.results.first().differenceRatio }\n tolerance percentage: ${report.results.first().tolerancePercentage.toString()}%</h4>
          <div class="grid">
            <div class="cell">
                <h2>Expected</h2>
                <img src="data:image/png;base64,${result.expected}"/>
            </div>
            <div class="cell">
                <h2>Diff</h2>
                <img src="data:image/png;base64,${result.diff}"/>
            </div>
            <div class="cell">
                <h2>Generated</h2>
                <img src="data:image/png;base64,${result.generated}"/>
            </div>
          </div>
        """
        }.joinToString(separator = "")
}
