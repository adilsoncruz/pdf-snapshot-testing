package com.github.adilsoncruz.pdfsnapshottesting.models

public data class SnapshotReport(
    val results: List<SnapshotPageResult>,
) {
    val hasFailures: Boolean =
        results.any { !it.matches }
}
