package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.BoundingBox
import com.github.adilsoncruz.pdfsnapshottesting.models.DiffResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage

public object DiffByBoundingBox : DiffStrategy {
    override fun diff(
        expected: BufferedImage,
        generated: BufferedImage,
        config: SnapshotConfig,
    ): DiffResult = buildDiffImageWithBoundingBox(expected, generated, config)

    public fun buildDiffImageWithBoundingBox(
        expected: BufferedImage,
        generated: BufferedImage,
        config: SnapshotConfig,
    ): DiffResult {
        val width = expected.width
        val height = expected.height
        val totalPixels = width * height

        val expectedHighlighted = deepCopy(expected)
        val generatedHighlighted = deepCopy(generated)
        val diffImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        val minor = config.minorPixelThreshold

        val addedMask = Array(height) { BooleanArray(width) }
        val removedMask = Array(height) { BooleanArray(width) }
        val changedMask = Array(height) { BooleanArray(width) }

        var differentPixels = 0

        // -------- PASSO 1: gerar máscaras --------

        for (y in 0 until height) {
            for (x in 0 until width) {
                val e = Color(expected.getRGB(x, y), true)
                val g = Color(generated.getRGB(x, y), true)

                val delta =
                    kotlin.math.abs(e.red - g.red) +
                        kotlin.math.abs(e.green - g.green) +
                        kotlin.math.abs(e.blue - g.blue)

                if (delta <= minor) continue

                differentPixels++

                val eGray = 0.299 * e.red + 0.587 * e.green + 0.114 * e.blue
                val gGray = 0.299 * g.red + 0.587 * g.green + 0.114 * g.blue

                val threshold = 15.0

                when {
                    // 🔴 Conteúdo adicionado
                    gGray + threshold < eGray -> {
                        addedMask[y][x] = true
                    }

                    // 🔵 Conteúdo removido
                    eGray + threshold < gGray -> {
                        removedMask[y][x] = true
                    }

                    // 🟣 Mudança visual
                    else -> {
                        changedMask[y][x] = true
                    }
                }
            }
        }

        val differenceRatio =
            differentPixels.toFloat() / totalPixels.toFloat()

        // -------- PASSO 2: encontrar bounding boxes --------

        val addedBoxes = findBoundingBoxes(addedMask, width, height)
        val removedBoxes = findBoundingBoxes(removedMask, width, height)
        val changedBoxes = findBoundingBoxes(changedMask, width, height)

        // -------- PASSO 3: desenhar --------

        val gDiff = diffImage.createGraphics()
        gDiff.drawImage(expected, 0, 0, null)
        val strokeWidth = maxOf(3f, (width / 300).toFloat())
        gDiff.stroke = BasicStroke(strokeWidth)

        // 🔴 Added
        for (box in addedBoxes) {
            highlightBox(diffImage, box, Color(255, 0, 0, 50))
            gDiff.color = Color(255, 0, 0, 220)
            gDiff.drawRect(box.x, box.y, box.width, box.height)
            highlightBox(generatedHighlighted, box, Color(255, 0, 0, 80))
        }

        // 🔵 Removed
        for (box in removedBoxes) {
            highlightBox(diffImage, box, Color(0, 120, 255, 50))
            gDiff.color = Color(0, 120, 255, 220)
            gDiff.drawRect(box.x, box.y, box.width, box.height)
            highlightBox(expectedHighlighted, box, Color(0, 120, 255, 80))
        }

        // 🟣 Changed
        for (box in changedBoxes) {
            highlightBox(diffImage, box, Color(180, 0, 180, 50))
            gDiff.color = Color(180, 0, 180, 220)
            gDiff.drawRect(box.x, box.y, box.width, box.height)
        }

        gDiff.dispose()

        return DiffResult(
            diffImage = diffImage,
            expectedHighlighted = expectedHighlighted,
            generatedHighlighted = generatedHighlighted,
            matches = differenceRatio <= config.tolerancePercent,
            differenceRatio = differenceRatio,
            tolerancePercentage = config.tolerancePercent,
            addedBoxes = addedBoxes,
            removedBoxes = removedBoxes,
            changedBoxes = changedBoxes,
        )
    }

    private fun findBoundingBoxes(
        mask: Array<BooleanArray>,
        width: Int,
        height: Int,
    ): List<BoundingBox> {
        val visited = Array(height) { BooleanArray(width) }
        val boxes = mutableListOf<BoundingBox>()

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (!mask[y][x] || visited[y][x]) continue

                var minX = x
                var maxX = x
                var minY = y
                var maxY = y

                val stack = ArrayDeque<Pair<Int, Int>>()
                stack.add(x to y)
                visited[y][x] = true

                while (stack.isNotEmpty()) {
                    val (cx, cy) = stack.removeLast()

                    minX = minOf(minX, cx)
                    maxX = maxOf(maxX, cx)
                    minY = minOf(minY, cy)
                    maxY = maxOf(maxY, cy)

                    for (ny in cy - 1..cy + 1) {
                        for (nx in cx - 1..cx + 1) {
                            if (nx in 0 until width &&
                                ny in 0 until height &&
                                mask[ny][nx] &&
                                !visited[ny][nx]
                            ) {
                                visited[ny][nx] = true
                                stack.add(nx to ny)
                            }
                        }
                    }
                }

                // ignora regiões muito pequenas (ruído)
                val area = (maxX - minX) * (maxY - minY)
                if (area > 20) {
                    boxes.add(
                        BoundingBox(
                            minX,
                            minY,
                            maxX - minX + 1,
                            maxY - minY + 1,
                        ),
                    )
                }
            }
        }

        return boxes
    }

    private fun highlightBox(
        image: BufferedImage,
        box: BoundingBox,
        color: Color,
    ) {
        val g = image.createGraphics()
        g.color = color
        g.fillRect(box.x, box.y, box.width, box.height)
        g.dispose()
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
}

@Deprecated(
    message = "Use DiffByBoundingBox instead",
    replaceWith = ReplaceWith("DiffByBoundingBox"),
)
public typealias DiffByBoudingBox = DiffByBoundingBox
