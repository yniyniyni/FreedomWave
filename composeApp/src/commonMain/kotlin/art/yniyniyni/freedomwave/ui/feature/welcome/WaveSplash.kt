package art.yniyniyni.freedomwave.ui.feature.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_brand_freedom
import freedomwave.composeapp.generated.resources.common_brand_wave
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

// M3 emphasized easing — same curve the design system specifies for enter motion.
private val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)

// Brand wave colors, taken verbatim from the logomark (wm_logo.svg).
private val WaveDeep = Color(0xFF0FB8A4)
private val WaveMid = Color(0xFF2FE0C9)
private val WaveLight = Color(0xFF5CF0DC)
private val WaveNode = Color(0xFF1EFFDD)

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
 * One-time splash: the wave draws in left-to-right, the echo + node dot fade in, the wordmark
 * fades, then [onFinished] fires. A tap completes it early. Caller shows it once per appearance.
 */
@Composable
fun WaveSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draw = remember { Animatable(0f) }
    val nodeAlpha = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val pathMeasure = remember { PathMeasure() }

    // Single-fire guard so tap + animation-end can't both dismiss.
    val latestOnFinished by rememberUpdatedState(onFinished)
    var fired by remember { mutableStateOf(false) }
    val finishOnce = {
        if (!fired) {
            fired = true
            latestOnFinished()
        }
    }

    // Wave geometry, rebuilt only when the canvas size changes (not every frame).
    var geoSize by remember { mutableStateOf(Size.Unspecified) }
    var geometry by remember { mutableStateOf<WaveGeometry?>(null) }
    val segment = remember { Path() }

    LaunchedEffect(Unit) {
        draw.animateTo(1f, tween(1150, easing = Emphasized))
        nodeAlpha.animateTo(1f, tween(250))
        wordmarkAlpha.animateTo(1f, tween(400))
        delay(450) // brief hold so the wordmark is visible before dismiss
        finishOnce()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { finishOnce() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Canvas(modifier = Modifier.size(176.dp, 120.dp)) {
                if (size != geoSize) {
                    val g = freedomWaveGeometry()
                    geometry = g
                    pathMeasure.setPath(g.main, false)
                    geoSize = size
                }
                val g = geometry ?: return@Canvas
                val stroke = Stroke(width = g.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

                // Faint echo wave behind, fading in with the node once the main wave is drawn.
                if (nodeAlpha.value > 0f) {
                    drawPath(g.echo, color = WaveMid.copy(alpha = 0.18f * nodeAlpha.value), style = stroke)
                }
                // Main wave, revealed left-to-right.
                segment.reset()
                pathMeasure.getSegment(0f, draw.value * pathMeasure.length, segment, true)
                drawPath(
                    path = segment,
                    brush = Brush.linearGradient(
                        colors = listOf(WaveDeep, WaveMid, WaveLight),
                        start = g.gradientStart,
                        end = g.gradientEnd,
                    ),
                    style = stroke,
                )
                // Bright node dot on the leading crest.
                drawCircle(
                    color = WaveNode.copy(alpha = nodeAlpha.value),
                    radius = g.nodeRadius,
                    center = g.node,
                )
            }
            Wordmark(alphaValue = wordmarkAlpha.value)
        }
    }
}

@Composable
private fun Wordmark(alphaValue: Float) {
    Row(modifier = Modifier.alpha(alphaValue)) {
        Text(
            text = stringResource(Res.string.common_brand_freedom),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(Res.string.common_brand_wave),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
