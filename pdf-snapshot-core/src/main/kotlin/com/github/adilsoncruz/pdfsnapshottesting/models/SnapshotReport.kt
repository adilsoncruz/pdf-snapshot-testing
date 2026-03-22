package com.github.adilsoncruz.pdfsnapshottesting.models

import java.io.File

public data class SnapshotReport(
    val results: List<SnapshotPageResult>,
    val testName: String? = null,
    val className: String = "",
    val expectedPageSize: Int,
    val generatedPageSize: Int,
    val reportLink: File? = null,
) {
    val hasFailures: Boolean =
        results.any { !it.matches }
}

public fun SnapshotReport.assertAll() {
    if (results.any { !it.matches }) {
        throw AssertionError(
            """
            PDF snapshot mismatch.
            """.trimIndent(),
        )
    }
}
