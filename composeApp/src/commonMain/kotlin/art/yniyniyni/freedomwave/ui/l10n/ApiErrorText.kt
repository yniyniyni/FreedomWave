package art.yniyniyni.freedomwave.ui.l10n

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.resources.Res
import art.yniyniyni.freedomwave.resources.error_generic
import art.yniyniyni.freedomwave.resources.error_network
import art.yniyniyni.freedomwave.resources.error_not_found
import art.yniyniyni.freedomwave.resources.error_unauthorized

/** Maps any failure to a localizable UiText; backend-supplied texts pass through as raw. */
fun Throwable.toUiText(): UiText = when (this) {
    is ApiError.Unauthorized -> UiText.Res(Res.string.error_unauthorized)
    is ApiError.NotFound     -> UiText.Res(Res.string.error_not_found)
    is ApiError.NetworkError -> UiText.Res(Res.string.error_network)
    else -> message?.takeIf { it.isNotBlank() }?.let { UiText.Raw(it) }
        ?: UiText.Res(Res.string.error_generic)
}
