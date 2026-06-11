package art.yniyniyni.freedomwave.ui.l10n

import androidx.compose.runtime.Composable
import art.yniyniyni.freedomwave.domain.model.UserStatus
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.users_status_active
import art.yniyniyni.freedomwave.resources.users_status_disabled
import art.yniyniyni.freedomwave.resources.users_status_expired
import art.yniyniyni.freedomwave.resources.users_status_limited
import org.jetbrains.compose.resources.stringResource

/**
 * Singular user-status label for per-user StatusBadge (user list rows and user detail header).
 * Grammatically correct in Russian: short-form masculine singular agreeing with «пользователь».
 * Do NOT use for dashboard chart or category filter chips — those use dashboard_status_* (plural).
 */
@Composable
fun UserStatus.localized(): String = stringResource(
    when (this) {
        UserStatus.ACTIVE   -> Res.string.users_status_active
        UserStatus.LIMITED  -> Res.string.users_status_limited
        UserStatus.EXPIRED  -> Res.string.users_status_expired
        UserStatus.DISABLED -> Res.string.users_status_disabled
    }
)
