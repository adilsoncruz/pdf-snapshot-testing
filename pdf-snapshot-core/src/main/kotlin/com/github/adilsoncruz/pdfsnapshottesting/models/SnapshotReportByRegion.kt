package com.github.adilsoncruz.pdfsnapshottesting.models

public data class SnapshotReportByRegion(
    val results: List<SnapshotResult>,
    val reportLink: String? = null,
){
    val hasFailures: Boolean =
        results.any { it is SnapshotResult.Mismatch }
}
