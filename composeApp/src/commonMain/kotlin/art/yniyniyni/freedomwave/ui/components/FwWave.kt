package art.yniyniyni.freedomwave.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * The brand wave mapped from the logomark's 500x500 viewBox into a draw [size] (fit, centered):
 * the main aqua wave, a faint echo wave behind it, the bright node dot on the leading crest,
 * the matching stroke width, and the gradient endpoints — all in canvas pixels.
 */
class WaveGeometry(
    val main: Path,
    val echo: Path,
    val node: Offset,
    val strokeWidth: Float,
    val nodeRadius: Float,
    val gradientStart: Offset,
    val gradientEnd: Offset,
)

fun DrawScope.freedomWaveGeometry(): WaveGeometry {
    // Source box around the wave + node + stroke half-width, from the 500x500 logomark.
    val srcX = 78f
    val srcY = 145f
    val srcW = 344f
    val srcH = 236f
    val scale = minOf(size.width / srcW, size.height / srcH)
    val offX = (size.width - srcW * scale) / 2f
    val offY = (size.height - srcH * scale) / 2f
    fun p(x: Float, y: Float) = Offset(offX + (x - srcX) * scale, offY + (y - srcY) * scale)

    val main = Path().apply {
        val s = p(102.459f, 281.967f)
        moveTo(s.x, s.y)
        cubicTo(p(151.639f, 175.41f).x, p(151.639f, 175.41f).y, p(200.82f, 175.41f).x, p(200.82f, 175.41f).y, p(250f, 249.18f).x, p(250f, 249.18f).y)
        cubicTo(p(299.18f, 322.951f).x, p(299.18f, 322.951f).y, p(348.361f, 322.951f).x, p(348.361f, 322.951f).y, p(397.541f, 216.393f).x, p(397.541f, 216.393f).y)
    }
    val echo = Path().apply {
        val s = p(102.459f, 314.754f)
        moveTo(s.x, s.y)
        cubicTo(p(151.639f, 208.197f).x, p(151.639f, 208.197f).y, p(200.82f, 208.197f).x, p(200.82f, 208.197f).y, p(250f, 281.967f).x, p(250f, 281.967f).y)
        cubicTo(p(299.18f, 355.738f).x, p(299.18f, 355.738f).y, p(348.361f, 355.738f).x, p(348.361f, 355.738f).y, p(397.541f, 249.18f).x, p(397.541f, 249.18f).y)
    }
    return WaveGeometry(
        main = main,
        echo = echo,
        node = p(168.033f, 180.328f),
        strokeWidth = 45.082f * scale,
        nodeRadius = 28.688f * scale,
        gradientStart = p(102.459f, 281.967f),
        gradientEnd = p(397.541f, 216.393f),
    )
}

/**
 * Indeterminate loading indicator shaped like the brand wave (no node dot): a faint full wave with
 * a bright segment running along it on a loop. Drop-in for `CircularProgressIndicator`; size it via
 * [modifier] (defaults to a small wave) and tint via [color] (e.g. `onPrimary` inside a button).
 */
@Composable
fun WaveLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val transition = rememberInfiniteTransition(label = "waveLoader")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )

    val measure = remember { PathMeasure() }
    val head = remember { Path() }
    val wrap = remember { Path() }
    var geoSize by remember { mutableStateOf(Size.Unspecified) }
    var geometry by remember { mutableStateOf<WaveGeometry?>(null) }

    Canvas(modifier = Modifier.size(width = 56.dp, height = 38.dp).then(modifier)) {
        if (size != geoSize) {
            val g = freedomWaveGeometry()
            geometry = g
            measure.setPath(g.main, false)
            geoSize = size
        }
        val g = geometry ?: return@Canvas
        val len = measure.length
        val stroke = Stroke(width = g.strokeWidth * 0.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Faint full wave as the track.
        drawPath(g.main, color = color.copy(alpha = 0.18f), style = stroke)

        // Bright segment (~35% of the path) travelling along it, wrapping at the end.
        val segLen = len * 0.35f
        val start = phase * len
        val end = start + segLen
        head.reset()
        measure.getSegment(start, minOf(end, len), head, true)
        drawPath(head, color = color, style = stroke)
        if (end > len) {
            wrap.reset()
            measure.getSegment(0f, end - len, wrap, true)
            drawPath(wrap, color = color, style = stroke)
        }
    }
}
