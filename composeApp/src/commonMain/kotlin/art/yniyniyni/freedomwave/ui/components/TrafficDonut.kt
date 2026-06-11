package art.yniyniyni.freedomwave.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.component_donut_lifetime
import art.yniyniyni.freedomwave.resources.component_donut_limit
import art.yniyniyni.freedomwave.resources.component_donut_used
import art.yniyniyni.freedomwave.resources.symbol_infinity
import art.yniyniyni.freedomwave.ui.theme.LocalFwMonoFont
import art.yniyniyni.freedomwave.ui.theme.LocalFwStatus
import art.yniyniyni.freedomwave.ui.l10n.localizedBytes
import org.jetbrains.compose.resources.stringResource

/**
 * Circular donut chart for traffic usage.
 *
 * - Fraction = usedBytes / limitBytes (clamped 0..1).
 * - When [limitBytes] is 0 (unlimited), the ring shows at 100% fill with the "online" color
 *   but the center label reads "∞".
 */
@Composable
fun TrafficDonut(
    usedBytes: Long,
    limitBytes: Long,
    lifetimeBytes: Long,
    modifier: Modifier = Modifier,
    donutSize: Dp = 140.dp,
    strokeWidth: Dp = 14.dp,
) {
    val monoFont  = LocalFwMonoFont.current
    val fwStatus  = LocalFwStatus.current
    val primary   = MaterialTheme.colorScheme.primary
    val track     = MaterialTheme.colorScheme.surfaceContainerHigh
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val isUnlimited = limitBytes == 0L
    val fraction = if (isUnlimited) 1f else
        (usedBytes.toFloat() / limitBytes.toFloat()).coerceIn(0f, 1f)

    val fillColor = when {
        isUnlimited       -> fwStatus.online
        fraction >= 0.9f  -> fwStatus.offline
        fraction >= 0.7f  -> fwStatus.warning
        else              -> primary
    }

    // Resolve all localized labels in composable scope — not inside the Canvas draw lambda.
    val pctLabel = if (isUnlimited) stringResource(Res.string.symbol_infinity)
        else "${(fraction * 100).toInt()}%"
    val usedLabel = stringResource(Res.string.component_donut_used)

    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Donut
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(donutSize)) {
            Canvas(modifier = Modifier.size(donutSize)) {
                val sw = strokeWidth.toPx()
                val inset = sw / 2f
                val arcSize = Size(size.width - sw, size.height - sw)
                val topLeft = Offset(inset, inset)
                val stroke = Stroke(width = sw, cap = StrokeCap.Round)

                // Track (background ring)
                drawArc(
                    color      = track,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = stroke,
                )

                // Filled arc
                val sweep = 360f * fraction
                if (sweep > 0f) {
                    drawArc(
                        color      = fillColor,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = stroke,
                    )
                }

                // Center label — draw via drawText (same pattern as BandwidthChart)
                val pctStyle = TextStyle(
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = onSurface,
                    fontFamily = monoFont,
                )
                val measuredPct = textMeasurer.measure(pctLabel, pctStyle)
                drawText(
                    measuredPct,
                    topLeft = Offset(
                        x = center.x - measuredPct.size.width / 2f,
                        y = center.y - measuredPct.size.height / 2f,
                    )
                )

                // "Used" sub-label
                val subStyle = TextStyle(fontSize = 10.sp, color = onVariant)
                val measuredSub = textMeasurer.measure(usedLabel, subStyle)
                drawText(
                    measuredSub,
                    topLeft = Offset(
                        x = center.x - measuredSub.size.width / 2f,
                        y = center.y + measuredPct.size.height / 2f + 2,
                    )
                )
            }
        }

        Spacer(Modifier.height(0.dp))

        // Three-column stats below the donut
        val limitStr = if (isUnlimited) stringResource(Res.string.symbol_infinity) else localizedBytes(limitBytes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            DonutStatColumn(usedLabel, localizedBytes(usedBytes), monoFont)
            DonutStatColumn(stringResource(Res.string.component_donut_limit), limitStr, monoFont)
            DonutStatColumn(stringResource(Res.string.component_donut_lifetime), localizedBytes(lifetimeBytes), monoFont)
        }
    }
}

@Composable
private fun DonutStatColumn(
    label: String,
    value: String,
    monoFont: androidx.compose.ui.text.font.FontFamily,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoFont),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
