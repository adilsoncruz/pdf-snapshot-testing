package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class PdfSnapshotRegionUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `engine should require at least one snapshot request for region mode`() {
        val engine =
            PdfSnapshotEngine(
                SnapshotConfig(
                    snapshotsDir = tempDir.toString(),
                    tolerancePercent = 0f,
                    dpi = 72f,
                ),
            )

        val error =
            assertThrows(IllegalArgumentException::class.java) {
                engine.run("region-validation.pdf", byteArrayOf(1, 2, 3), pageIndex = 0, requests = emptyList())
            }

        assertEquals("No snapshot regions defined", error.message)
    }
}
