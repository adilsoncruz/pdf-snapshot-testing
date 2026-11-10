package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.DiffResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage

public object DiffByPixel : DiffStrategy {
    override fun diff(
        expected: BufferedImage,
        generated: BufferedImage,
        config: SnapshotConfig,
    ): DiffResult = buildDiffImage(expected, generated, config)

    public fun buildDiffImage(
        expected: BufferedImage,
        generated: BufferedImage,
        config: SnapshotConfig,
    ): DiffResult {
        val width = expected.width
        val height = expected.height
        val totalPixels = width * height

        val expectedOriginal = expected
        val generatedOriginal = generated

        val expectedHighlighted = deepCopy(expected)
        val generatedHighlighted = deepCopy(generated)

        val diffImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        val minor = config.minorPixelThreshold
        val blur = config.diffBlurRadius

        var differentPixels = 0
        var totalDelta = 0.0
        val deltas = mutableListOf<Double>()

        // -------- PASSO 1: calcular diferenças --------

        for (y in 0 until height) {
            for (x in 0 until width) {
                val e = Color(expectedOriginal.getRGB(x, y), true)
                val g = Color(generatedOriginal.getRGB(x, y), true)

                val delta =
                    kotlin.math.abs(e.red - g.red) +
                        kotlin.math.abs(e.green - g.green) +
                        kotlin.math.abs(e.blue - g.blue)

                if (delta > minor) {
                    differentPixels++
                    totalDelta += delta
                    deltas.add(delta.toDouble())
                }
            }
        }

        val differenceRatio = differentPixels.toFloat() / totalPixels.toFloat()

        if (differentPixels == 0) {
            return DiffResult(
                diffImage = expected,
                expectedHighlighted = expectedHighlighted,
                generatedHighlighted = generatedHighlighted,
                matches = true,
                differenceRatio = 0f,
                tolerancePercentage = config.tolerancePercent,
            )
        }

        // -------- PASSO 2: detectar mudança global --------

        val meanDelta = totalDelta / differentPixels

        val variance = deltas.sumOf { (it - meanDelta) * (it - meanDelta) } / differentPixels

        val isGlobalBackgroundChange =
            differenceRatio > 0.6 && variance < 50

        // -------- PASSO 3: renderização --------

        val g2 = diffImage.createGraphics()

        if (isGlobalBackgroundChange) {
            // destaca página inteira
            g2.drawImage(expectedOriginal, 0, 0, null)

            g2.color = Color(255, 0, 0, 80)
            g2.fillRect(0, 0, width, height)

            g2.color = Color.RED
            g2.stroke = BasicStroke(4f)
            g2.drawRect(0, 0, width - 1, height - 1)

            g2.dispose()

            return DiffResult(
                diffImage = diffImage,
                expectedHighlighted = expectedHighlighted,
                generatedHighlighted = generatedHighlighted,
                matches = false,
                differenceRatio = differenceRatio,
                tolerancePercentage = config.tolerancePercent,
            )
        }

        // -------- PASSO 4: mudança local (modo normal) --------

        for (y in 0 until height) {
            for (x in 0 until width) {
                val e = Color(expectedOriginal.getRGB(x, y), true)
                val g = Color(generatedOriginal.getRGB(x, y), true)

                val delta =
                    kotlin.math.abs(e.red - g.red) +
                        kotlin.math.abs(e.green - g.green) +
                        kotlin.math.abs(e.blue - g.blue)

                if (delta <= minor) {
                    diffImage.setRGB(x, y, e.rgb)
                    continue
                }

                val eGray = 0.299 * e.red + 0.587 * e.green + 0.114 * e.blue
                val gGray = 0.299 * g.red + 0.587 * g.green + 0.114 * g.blue

                val threshold = 15.0

                when {
                    gGray + threshold < eGray -> {
                        val red = Color(255, 0, 0, 200)
                        drawDiffPixel(diffImage, x, y, red, blur)
                        drawDiffPixel(generatedHighlighted, x, y, red, blur)
                    }

                    eGray + threshold < gGray -> {
                        val blue = Color(0, 120, 255, 200)
                        drawDiffPixel(diffImage, x, y, blue, blur)
                        drawDiffPixel(expectedHighlighted, x, y, blue, blur)
                    }

                    else -> {
                        val purple = Color(180, 0, 180, 200)
                        drawDiffPixel(diffImage, x, y, purple, blur)
                    }
                }
            }
        }

        g2.dispose()

        return DiffResult(
            diffImage = diffImage,
            expectedHighlighted = expectedHighlighted,
            generatedHighlighted = generatedHighlighted,
            matches = differenceRatio <= config.tolerancePercent,
            differenceRatio = differenceRatio,
            tolerancePercentage = config.tolerancePercent,
        )
    }

    private fun deepCopy(original: BufferedImage): BufferedImage {
        val copy =
            BufferedImage(
                original.width,
                original.height,
                BufferedImage.TYPE_INT_ARGB,
            )
        val g = copy.createGraphics()
        g.drawImage(original, 0, 0, null)
        g.dispose()
        return copy
    }

    private fun drawDiffPixel(
        image: BufferedImage,
        x: Int,
        y: Int,
        color: Color,
        radius: Int,
    ) {
        for (dy in -radius..radius) {
            for (dx in -radius..radius) {
                val px = x + dx
                val py = y + dy
                if (px in 0 until image.width && py in 0 until image.height) {
                    image.setRGB(px, py, color.rgb)
                }
            }
        }
    }
}
