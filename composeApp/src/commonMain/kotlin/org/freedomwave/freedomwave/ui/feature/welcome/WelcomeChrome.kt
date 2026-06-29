package org.freedomwave.ui.feature.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp as lerpColor
import org.freedomwave.ui.components.freedomWaveGeometry
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp
import kotlin.math.abs

/**
 * Row of pager dots driven by the live scroll [position] (page index + offset fraction), so the
 * active aqua pill grows and slides continuously as the carousel is dragged rather than snapping.
 * [position] is a provider so only this row recomposes per frame, not the whole page.
 */
@Composable
fun WelcomeDots(
    pageCount: Int,
    position: () -> Float,
    modifier: Modifier = Modifier,
) {
    val inactive = MaterialTheme.colorScheme.surfaceContainerHighest
    val active = MaterialTheme.colorScheme.primary
    val pos = position()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        repeat(pageCount) { i ->
            val t = (1f - abs(i - pos)).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .height(7.dp)
                    .width(lerpDp(7.dp, 22.dp, t))
                    .clip(RoundedCornerShape(4.dp))
                    .background(lerpColor(inactive, active, t)),
            )
        }
    }
}

/** Very low-opacity wave behind a hero. The only permitted graphical flourish on data. */
@Composable
fun WaveBackdrop(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val g = freedomWaveGeometry()
        drawPath(
            path = g.main,
            color = primary.copy(alpha = 0.05f),
            style = Stroke(width = g.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
