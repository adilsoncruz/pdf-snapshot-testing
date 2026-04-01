package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.image.BufferedImage

class DiffStrategiesUnitTest {
    @Test
    fun `DiffByPixel should match identical images`() {
        val expected = image(20, 20, Color.WHITE)
        val generated = image(20, 20, Color.WHITE)

        val result = DiffByPixel.buildDiffImage(expected, generated, SnapshotConfig())

        assertTrue(result.matches)
        assertEquals(0f, result.differenceRatio)
    }

    @Test
    fun `DiffByPixel should detect local differences`() {
        val expected = image(20, 20, Color.WHITE)
        val generated = image(20, 20, Color.WHITE).also { it.setRGB(10, 10, Color.BLACK.rgb) }

        val result = DiffByPixel.buildDiffImage(expected, generated, SnapshotConfig(tolerancePercent = 0f))

        assertFalse(result.matches)
        assertTrue(result.differenceRatio > 0f)
    }

    @Test
    fun `DiffByPixel should detect global background change`() {
        val expected = image(40, 40, Color(240, 240, 240))
        val generated = image(40, 40, Color(120, 120, 120))

        val result = DiffByPixel.buildDiffImage(expected, generated, SnapshotConfig(tolerancePercent = 0f))

        assertFalse(result.matches)
        assertTrue(result.differenceRatio > 0.6f)
    }

    @Test
    fun `DiffByBoudingBox should detect added area`() {
        val expected = image(30, 30, Color.WHITE)
        val generated = image(30, 30, Color.WHITE)
        fillRect(generated, 5, 5, 8, 8, Color.BLACK)

        val result =
            DiffByBoudingBox.buildDiffImageWithBoundingBox(
                expected,
                generated,
                SnapshotConfig(tolerancePercent = 0f),
            )

        assertFalse(result.matches)
        assertTrue(result.addedBoxes.orEmpty().isNotEmpty())
    }

    @Test
    fun `DiffByBoudingBox should detect changed area`() {
        val expected = image(30, 30, Color.WHITE)
        val generated = image(30, 30, Color.WHITE)
        fillRect(expected, 10, 10, 8, 8, Color(40, 120, 80))
        fillRect(generated, 10, 10, 8, 8, Color(105, 80, 90))

        val result =
            DiffByBoudingBox.buildDiffImageWithBoundingBox(
                expected,
                generated,
                SnapshotConfig(tolerancePercent = 0f),
            )

        assertFalse(result.matches)
        assertTrue(result.changedBoxes.orEmpty().isNotEmpty())
    }

    private fun image(
        width: Int,
        height: Int,
        color: Color,
    ): BufferedImage {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        fillRect(img, 0, 0, width, height, color)
        return img
    }

    private fun fillRect(
        image: BufferedImage,
        startX: Int,
        startY: Int,
        w: Int,
        h: Int,
        color: Color,
    ) {
        for (y in startY until (startY + h)) {
            for (x in startX until (startX + w)) {
                image.setRGB(x, y, color.rgb)
            }
        }
    }
}
