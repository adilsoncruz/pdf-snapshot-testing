package com.github.adilsoncruz.pdfsnapshottesting.models

import java.io.File

public sealed class SnapshotResult(
    public open val name: String,
    public open val region: PdfRegion,
    public open val matches: Boolean? = null,
) {
    public data class Success(
        override val name: String,
        override val region: PdfRegion,
        override val matches: Boolean,
    ) : SnapshotResult(name, region, matches)

    public data class Created(
        override val name: String,
        val snapshot: File,
        override val region: PdfRegion,
    ) : SnapshotResult(name, region, null)

    public data class FirstCreate(
        override val name: String,
        override val region: PdfRegion,
    ) : SnapshotResult(name, region, null)

    public data class Mismatch(
        override val name: String,
        val expected: File,
        override val region: PdfRegion,
        override val matches: Boolean,
    ) : SnapshotResult(name, region, matches)
}
