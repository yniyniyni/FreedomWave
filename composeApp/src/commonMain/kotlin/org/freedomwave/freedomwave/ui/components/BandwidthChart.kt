package org.freedomwave.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.freedomwave.ui.l10n.localizedBytes

data class ChartSeries(
    val name: String,
    val color: String,
    val data: List<Double>
)

@Composable
fun BandwidthChart(
    categories: List<String>,
    series: List<ChartSeries>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    if (categories.isEmpty() || series.all { it.data.isEmpty() }) return

    val textMeasurer = rememberTextMeasurer()
    val gridColor  = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)

    // Y-axis labels must be localized in composable scope (not inside the draw lambda)
    val maxValue = series.flatMap { it.data }.maxOrNull()?.takeIf { it > 0 }
    val axisLabels = (0..4).map { i -> localizedBytes(((maxValue ?: 0.0) * (i.toFloat() / 4f)).toLong()) }

    Canvas(modifier = modifier) {
        val leftPad   = 52.dp.toPx()
        val bottomPad = 22.dp.toPx()
        val chartW    = size.width - leftPad
        val chartH    = size.height - bottomPad

        val maxVal = maxValue ?: return@Canvas

        val n = categories.size
        val xStep = if (n > 1) chartW / (n - 1).toFloat() else chartW

        // Horizontal grid lines + Y-axis labels
        for (i in 0..4) {
            val fraction = i.toFloat() / 4f
            val y = chartH * (1f - fraction)
            drawLine(gridColor, Offset(leftPad, y), Offset(size.width, y), strokeWidth = 0.5.dp.toPx())

            val measured = textMeasurer.measure(axisLabels[i], labelStyle)
            drawText(
                measured,
                topLeft = Offset(
                    x = leftPad - measured.size.width.toFloat() - 4.dp.toPx(),
                    y = y - measured.size.height.toFloat() / 2f
                )
            )
        }

        // Series lines
        series.forEach { s ->
            if (s.data.isEmpty()) return@forEach
            val lineColor = parseHexColor(s.color)
            val path = Path()
            var firstPoint = true
            s.data.forEachIndexed { idx, v ->
                if (idx >= n) return@forEachIndexed
                val x = leftPad + idx * xStep
                val y = chartH * (1f - (v / maxVal).toFloat())
                if (firstPoint) { path.moveTo(x, y); firstPoint = false }
                else path.lineTo(x, y)
            }
            drawPath(path, lineColor, style = Stroke(
                width     = 2.dp.toPx(),
                cap       = StrokeCap.Round,
                join      = StrokeJoin.Round
            ))
        }

        // X-axis date labels (every N-th to avoid overlap)
        val labelEvery = maxOf(1, n / 5)
        categories.forEachIndexed { idx, cat ->
            if (idx % labelEvery == 0 || idx == n - 1) {
                val x = leftPad + idx * xStep
                val shortDate = if (cat.length >= 10) cat.substring(5, 10) else cat // "MM-DD"
                val measured = textMeasurer.measure(shortDate, labelStyle)
                drawText(
                    measured,
                    topLeft = Offset(
                        x = x - measured.size.width.toFloat() / 2f,
                        y = chartH + 4.dp.toPx()
                    )
                )
            }
        }
    }
}

@Composable
fun SparklineChart(
    data: List<Double>,
    modifier: Modifier = Modifier.fillMaxWidth().height(80.dp)
) {
    if (data.isEmpty()) return
    val lineColor  = MaterialTheme.colorScheme.primary
    val fillColor  = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val gridColor  = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Canvas(modifier = modifier) {
        val maxVal = data.maxOrNull()?.takeIf { it > 0 } ?: return@Canvas
        val n = data.size
        val xStep = if (n > 1) size.width / (n - 1).toFloat() else size.width

        // Light grid
        for (i in 0..2) {
            val y = size.height * (1f - i.toFloat() / 2f)
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5.dp.toPx())
        }

        // Fill area under the line
        val fillPath = Path()
        data.forEachIndexed { idx, v ->
            val x = idx * xStep
            val y = size.height * (1f - (v / maxVal).toFloat())
            if (idx == 0) fillPath.moveTo(x, y) else fillPath.lineTo(x, y)
        }
        fillPath.lineTo((n - 1) * xStep, size.height)
        fillPath.lineTo(0f, size.height)
        fillPath.close()
        drawPath(fillPath, fillColor)

        // Line
        val linePath = Path()
        data.forEachIndexed { idx, v ->
            val x = idx * xStep
            val y = size.height * (1f - (v / maxVal).toFloat())
            if (idx == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(linePath, lineColor, style = Stroke(
            width = 2.dp.toPx(),
            cap   = StrokeCap.Round,
            join  = StrokeJoin.Round
        ))
    }
}

private fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#").trim()
    return if (cleaned.length == 6) {
        val r = cleaned.substring(0, 2).toIntOrNull(16) ?: 128
        val g = cleaned.substring(2, 4).toIntOrNull(16) ?: 128
        val b = cleaned.substring(4, 6).toIntOrNull(16) ?: 128
        Color(r / 255f, g / 255f, b / 255f)
    } else {
        Color(0.5f, 0.5f, 0.5f)
    }
}
