package com.example.kylepl.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kylepl.data.weighin.WeighIn
import com.example.kylepl.ui.screens.formatKg
import com.example.kylepl.ui.screens.roundToOneDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val xAxisDateFormatter = DateTimeFormatter.ofPattern("dd/MM")

/**
 * Line chart of bodyweight over [periodDates] (every calendar day in the selected
 * week or month, so gaps in logging show as gaps in the line), with a dashed
 * line marking [meanWeightKg], a Y axis of bodyweight values, and an X axis of
 * dates in DD/MM format.
 */
@Composable
fun WeighInLineChart(
    periodDates: List<LocalDate>,
    entries: List<WeighIn>,
    meanWeightKg: Double?,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No weigh-ins logged for this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val meanColor = MaterialTheme.colorScheme.secondary
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val sortedEntries = entries.sortedBy { it.date }
    val weights = sortedEntries.map { it.weightKg }
    val dateIndex = periodDates.withIndex().associate { (i, d) -> d to i }
    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = TextStyle(color = labelColor, fontSize = 10.sp)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        ) {
            val minWeight = minOf(weights.min(), meanWeightKg ?: weights.min())
            val maxWeight = maxOf(weights.max(), meanWeightKg ?: weights.max())
            val padding = ((maxWeight - minWeight).takeIf { it > 0.01 } ?: 1.0) * 0.2
            val lowerBound = minWeight - padding
            val upperBound = maxWeight + padding
            val range = (upperBound - lowerBound).takeIf { it > 0.0 } ?: 1.0

            // Reserve space on the left for Y-axis labels and at the bottom for X-axis labels.
            val yAxisLabelWidth = 40.dp.toPx()
            val xAxisLabelHeight = 20.dp.toPx()
            val plotLeft = yAxisLabelWidth
            val plotRight = size.width
            val plotTop = 0f
            val plotBottom = size.height - xAxisLabelHeight
            val plotWidth = plotRight - plotLeft
            val plotHeight = plotBottom - plotTop

            val stepX = if (periodDates.size > 1) plotWidth / (periodDates.size - 1) else plotWidth

            fun yFor(weight: Double): Float =
                plotBottom - ((weight - lowerBound) / range * plotHeight).toFloat()

            fun xFor(index: Int): Float = plotLeft + index * stepX

            // Y axis gridlines + weight value labels.
            val yTickCount = 4
            for (tick in 0..yTickCount) {
                val weight = lowerBound + range * tick / yTickCount
                val y = yFor(weight)
                drawLine(
                    color = axisColor,
                    start = Offset(plotLeft, y),
                    end = Offset(plotRight, y),
                    strokeWidth = 1.dp.toPx(),
                )
                val label = textMeasurer.measure(formatKg(roundToOneDecimal(weight)), style = labelTextStyle)
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(
                        x = yAxisLabelWidth - label.size.width - 6.dp.toPx(),
                        y = (y - label.size.height / 2f).coerceIn(0f, size.height - label.size.height),
                    ),
                )
            }

            // X axis tick dates, evenly spaced, capped so labels don't overlap.
            val maxXLabels = 6
            val tickIndices = if (periodDates.size <= maxXLabels) {
                periodDates.indices.toList()
            } else {
                val step = (periodDates.size - 1).toDouble() / (maxXLabels - 1)
                (0 until maxXLabels).map { i -> (i * step).roundToInt() }.distinct()
            }
            tickIndices.forEach { index ->
                val x = xFor(index)
                val label = textMeasurer.measure(
                    periodDates[index].format(xAxisDateFormatter),
                    style = labelTextStyle,
                )
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(
                        x = (x - label.size.width / 2f).coerceIn(plotLeft, plotRight - label.size.width),
                        y = plotBottom + 4.dp.toPx(),
                    ),
                )
            }

            // Axis lines.
            drawLine(
                color = axisColor,
                start = Offset(plotLeft, plotTop),
                end = Offset(plotLeft, plotBottom),
                strokeWidth = 1.5.dp.toPx(),
            )
            drawLine(
                color = axisColor,
                start = Offset(plotLeft, plotBottom),
                end = Offset(plotRight, plotBottom),
                strokeWidth = 1.5.dp.toPx(),
            )

            if (meanWeightKg != null) {
                val y = yFor(meanWeightKg)
                drawLine(
                    color = meanColor,
                    start = Offset(plotLeft, y),
                    end = Offset(plotRight, y),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                )
            }

            val points = sortedEntries.mapNotNull { entry ->
                dateIndex[entry.date]?.let { index -> Offset(xFor(index), yFor(entry.weightKg)) }
            }

            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }

            points.forEach { point -> drawCircle(color = lineColor, radius = 4.dp.toPx(), center = point) }
        }

        if (meanWeightKg != null) {
            Text(
                "- - - Mean: ${formatKg(meanWeightKg)} kg",
                style = MaterialTheme.typography.labelSmall,
                color = meanColor,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
