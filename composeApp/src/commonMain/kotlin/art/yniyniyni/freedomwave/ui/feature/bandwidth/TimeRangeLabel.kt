package art.yniyniyni.freedomwave.ui.feature.bandwidth

import androidx.compose.runtime.Composable
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.bandwidth_range_30d
import art.yniyniyni.freedomwave.resources.bandwidth_range_7d
import art.yniyniyni.freedomwave.resources.bandwidth_range_90d
import org.jetbrains.compose.resources.stringResource

@Composable
fun TimeRange.label(): String = stringResource(
    when (this) {
        TimeRange.DAYS_7  -> Res.string.bandwidth_range_7d
        TimeRange.DAYS_30 -> Res.string.bandwidth_range_30d
        TimeRange.DAYS_90 -> Res.string.bandwidth_range_90d
    }
)
