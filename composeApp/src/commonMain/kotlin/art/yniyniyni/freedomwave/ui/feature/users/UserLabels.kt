package art.yniyniyni.freedomwave.ui.feature.users

import androidx.compose.runtime.Composable
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.dashboard_status_active
import art.yniyniyni.freedomwave.resources.dashboard_status_disabled
import art.yniyniyni.freedomwave.resources.dashboard_status_expired
import art.yniyniyni.freedomwave.resources.users_category_all
import art.yniyniyni.freedomwave.resources.users_category_never_online
import art.yniyniyni.freedomwave.resources.users_category_online
import art.yniyniyni.freedomwave.resources.users_preset_3m
import art.yniyniyni.freedomwave.resources.users_preset_6m
import art.yniyniyni.freedomwave.resources.users_preset_day
import art.yniyniyni.freedomwave.resources.users_preset_forever
import art.yniyniyni.freedomwave.resources.users_preset_month
import art.yniyniyni.freedomwave.resources.users_preset_week
import art.yniyniyni.freedomwave.resources.users_preset_year
import art.yniyniyni.freedomwave.resources.users_sort_id
import art.yniyniyni.freedomwave.resources.users_sort_online
import art.yniyniyni.freedomwave.resources.users_sort_status
import art.yniyniyni.freedomwave.resources.users_sort_username
import art.yniyniyni.freedomwave.resources.users_strategy_day
import art.yniyniyni.freedomwave.resources.users_strategy_month
import art.yniyniyni.freedomwave.resources.users_strategy_month_rolling
import art.yniyniyni.freedomwave.resources.users_strategy_no_reset
import art.yniyniyni.freedomwave.resources.users_strategy_week
import art.yniyniyni.freedomwave.util.ExpiryPreset
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserSortField.label(): String = stringResource(
    when (this) {
        UserSortField.USERNAME -> Res.string.users_sort_username
        UserSortField.STATUS   -> Res.string.users_sort_status
        UserSortField.ONLINE   -> Res.string.users_sort_online
        UserSortField.ID       -> Res.string.users_sort_id
    }
)

@Composable
fun UserCategory.label(): String = stringResource(
    when (this) {
        UserCategory.ALL          -> Res.string.users_category_all
        UserCategory.ACTIVE       -> Res.string.dashboard_status_active
        UserCategory.DISABLED     -> Res.string.dashboard_status_disabled
        UserCategory.EXPIRED      -> Res.string.dashboard_status_expired
        UserCategory.ONLINE       -> Res.string.users_category_online
        UserCategory.NEVER_ONLINE -> Res.string.users_category_never_online
    }
)

@Composable
fun ExpiryPreset.label(): String = stringResource(
    when (this) {
        ExpiryPreset.DAY      -> Res.string.users_preset_day
        ExpiryPreset.WEEK     -> Res.string.users_preset_week
        ExpiryPreset.MONTH    -> Res.string.users_preset_month
        ExpiryPreset.MONTHS_3 -> Res.string.users_preset_3m
        ExpiryPreset.MONTHS_6 -> Res.string.users_preset_6m
        ExpiryPreset.YEAR     -> Res.string.users_preset_year
        ExpiryPreset.FOREVER  -> Res.string.users_preset_forever
    }
)

/** Localized label for a Remnawave traffic-limit reset strategy; unknown values pass through raw. */
@Composable
fun trafficStrategyLabel(strategy: String): String = when (strategy) {
    "NO_RESET"      -> stringResource(Res.string.users_strategy_no_reset)
    "DAY"           -> stringResource(Res.string.users_strategy_day)
    "WEEK"          -> stringResource(Res.string.users_strategy_week)
    "MONTH"         -> stringResource(Res.string.users_strategy_month)
    "MONTH_ROLLING" -> stringResource(Res.string.users_strategy_month_rolling)
    else            -> strategy
}
