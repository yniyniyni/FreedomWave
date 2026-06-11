package art.yniyniyni.freedomwave.ui.l10n

import androidx.compose.runtime.Composable
import art.yniyniyni.freedomwave.domain.model.UserStatus
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.dashboard_status_active
import art.yniyniyni.freedomwave.resources.dashboard_status_disabled
import art.yniyniyni.freedomwave.resources.dashboard_status_expired
import art.yniyniyni.freedomwave.resources.dashboard_status_limited
import org.jetbrains.compose.resources.stringResource

/**
 * Shared user-status label, reusing the dashboard keys so the status terminology
 * is identical between the Dashboard chart and the Users screen.
 */
@Composable
fun UserStatus.localized(): String = stringResource(
    when (this) {
        UserStatus.ACTIVE   -> Res.string.dashboard_status_active
        UserStatus.LIMITED  -> Res.string.dashboard_status_limited
        UserStatus.EXPIRED  -> Res.string.dashboard_status_expired
        UserStatus.DISABLED -> Res.string.dashboard_status_disabled
    }
)
