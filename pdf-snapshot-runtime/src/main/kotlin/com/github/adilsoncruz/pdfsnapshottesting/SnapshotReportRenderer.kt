package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport

public class SnapshotReportRenderer {
    public fun generateConsolidatedHtml(groupedReports: Map<String, List<SnapshotReport>>): String {
        val testSections =
            groupedReports.entries.joinToString("\n") { (testId, reports) ->

                val hasFailure =
                    reports.any { report ->
                        report.results.any { !it.matches }
                    }

                val color = if (hasFailure) "#E53935" else "#4CAF50"

                val data = (
                    reports.map { report ->
                        val title = """
         <h3>${report.testName}</h3>
        """
                        title +
                            report.results.map {
                                """
            <div class="grid">
          <div class="cell">
            <h2>Expected</h2>
            <img src="data:image/png;base64,${it.expected}"/>
          </div>
          <div class="cell">
            <h2>Diff</h2>
            <img src="data:image/png;base64,${it.diff}"/>
          </div>
          <div class="cell">
            <h2>Generated</h2>
            <img src="data:image/png;base64,${it.generated}"/>
          </div>
        </div>
          """
                            }.joinToString("")
                    }
                )

                println("DIFF REPORT: $data")

                """
                <section style="
                    border:1px solid #ddd;
                    border-radius:10px;
                    padding:20px;
                    margin-bottom:30px;
                ">
                    <h2 style="margin-top:0;color:$color">
                        ${reports.first().className}
                    </h2>
                    ${data.joinToString("")}
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
                background: #111;
                color: #eee;
                padding: 16px;
              }
              .grid {
                display: grid;
                grid-template-columns: repeat(3, 1fr);
                gap: 12px;
              }
              .cell {
                background: #1e1e1e;
                padding: 8px;
                border-radius: 8px;
              }
              img {
                width: 100%;
              }
              h2 {
                text-align: center;
                font-size: 14px;
              }
            </style>
              </head>
              <body>
                  <h1>PDF Snapshot Consolidated</h1>
                  $testSections
              </body>
              </html>
            """.trimIndent()
    }
}
