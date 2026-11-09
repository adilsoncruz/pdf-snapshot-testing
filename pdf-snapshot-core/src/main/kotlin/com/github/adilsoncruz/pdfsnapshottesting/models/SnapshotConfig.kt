package com.github.adilsoncruz.pdfsnapshottesting.models

public data class SnapshotConfig(
    val dpi: Float = 300f,
    val minorPixelThreshold: Int = 10,
    val tolerancePercent: Float = 0.001f,
    val diffBlurRadius: Int = 1,
    val snapshotsDir: String = "src/test/resources/pdf-snapshots",
    val snapshotsResultDir: String = "build/pdf-snapshots",
)
