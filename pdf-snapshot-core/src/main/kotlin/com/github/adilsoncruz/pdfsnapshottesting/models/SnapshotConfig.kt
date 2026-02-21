package com.github.adilsoncruz.pdfsnapshottesting.models

public data class SnapshotConfig(
    val dpi: Float = 300f,
    val pixelThreshold: Int = 10,
    val minorPixelThreshold: Int = 10,
    val majorPixelThreshold: Int = 60,
    val tolerancePercent: Float = 0.001f,
    val diffBlurRadius: Int = 1,
    val snapshotsDir: String = "src/test/resources/pdf-snapshots",
    val snapshotsResultDir: String = "build/pdf-snapshots",
    val writeActualOnFailure: Boolean = false,
    val structureThreshold: Float = 0.02f,
)
