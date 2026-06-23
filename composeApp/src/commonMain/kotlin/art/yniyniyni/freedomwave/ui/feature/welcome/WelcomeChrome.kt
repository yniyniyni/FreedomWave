package art.yniyniyni.freedomwave.ui.feature.welcome

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Row of pager dots; the active dot is an elongated aqua pill. */
@Composable
fun WelcomeDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        repeat(pageCount) { i ->
            val active = i == currentPage
            val w by animateDpAsState(if (active) 22.dp else 7.dp, label = "dotW")
            val color by animateColorAsState(
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                label = "dotC",
            )
            Box(
                modifier = Modifier
                    .height(7.dp)
                    .width(w)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
            )
        }
    }
}

/** Very low-opacity wave line behind a hero. The only permitted graphical flourish on data. */
@Composable
fun WaveBackdrop(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val (full, _) = freedomWavePath()
        drawPath(
            path = full,
            color = primary.copy(alpha = 0.05f),
            style = Stroke(width = size.height * 0.06f, cap = StrokeCap.Round),
        )
    }
}
