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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
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

// Stroke width / node radius as a fraction of canvas height (dot matches the line weight).
private const val STROKE_FRACTION = 0.085f

/**
 * Builds the brand wave path scaled into [size]. Coordinates come from the 160x90 logomark
 * viewBox so the curve matches the brand mark. The leading crest (node point) is the last point.
 */
fun DrawScope.freedomWavePath(): Pair<Path, Offset> {
    val w = size.width
    val h = size.height
    fun px(x: Float, y: Float) = Offset(x / 160f * w, y / 90f * h)
    val start = px(8f, 58f)
    val path = Path().apply {
        moveTo(start.x, start.y)
        cubicTo(px(30f, 58f).x, px(30f, 58f).y, px(34f, 30f).x, px(34f, 30f).y, px(56f, 30f).x, px(56f, 30f).y)
        cubicTo(px(78f, 58f).x, px(78f, 58f).y, px(82f, 58f).x, px(82f, 58f).y, px(104f, 58f).x, px(104f, 58f).y)
        cubicTo(px(126f, 58f).x, px(126f, 58f).y, px(130f, 34f).x, px(130f, 34f).y, px(152f, 34f).x, px(152f, 34f).y)
    }
    return path to px(152f, 34f)
}

/**
 * One-time splash: wave draws in, node dot pops, wordmark fades, then [onFinished] fires.
 * A tap completes it early. Caller is responsible for only showing it once per appearance.
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
    var pathSize by remember { mutableStateOf(Size.Unspecified) }
    var wavePath by remember { mutableStateOf(Path()) }
    var nodePoint by remember { mutableStateOf(Offset.Zero) }
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
            val primary = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.size(150.dp, 84.dp)) {
                if (size != pathSize) {
                    val (p, n) = freedomWavePath()
                    wavePath = p
                    nodePoint = n
                    pathMeasure.setPath(p, false)
                    pathSize = size
                }
                val strokeWidth = size.height * STROKE_FRACTION
                segment.reset()
                pathMeasure.getSegment(0f, draw.value * pathMeasure.length, segment, true)
                drawPath(
                    path = segment,
                    color = primary,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                drawCircle(
                    color = primary.copy(alpha = nodeAlpha.value),
                    radius = strokeWidth,
                    center = nodePoint,
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
