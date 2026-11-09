package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.DiffResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import java.awt.image.BufferedImage

/**
 * Strategy interface for comparing two images and producing a [DiffResult].
 */
public fun interface DiffStrategy {
    public fun diff(
        expected: BufferedImage,
        generated: BufferedImage,
        config: SnapshotConfig,
    ): DiffResult
}
