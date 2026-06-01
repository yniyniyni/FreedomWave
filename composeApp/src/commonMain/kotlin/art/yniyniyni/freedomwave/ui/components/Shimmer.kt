package art.yniyniyni.freedomwave.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBlock(modifier: Modifier) {
    val transition = rememberInfiniteTransition("shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.10f,
        targetValue  = 0.30f,
        animationSpec = infiniteRepeatable(
            animation   = tween(900, easing = FastOutSlowInEasing),
            repeatMode  = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    Box(modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha), RoundedCornerShape(6.dp)))
}

@Composable
fun ShimmerListItem() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBlock(Modifier.size(12.dp).clip(CircleShape))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerBlock(Modifier.width(160.dp).height(14.dp))
                ShimmerBlock(Modifier.width(100.dp).height(10.dp))
            }
        }
    }
}

@Composable
fun ShimmerList(count: Int = 8, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count) { ShimmerListItem() }
    }
}
