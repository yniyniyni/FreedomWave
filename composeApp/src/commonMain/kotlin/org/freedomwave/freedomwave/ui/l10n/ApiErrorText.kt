package org.freedomwave.ui.l10n

import org.freedomwave.data.api.ApiError
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.error_generic
import freedomwave.composeapp.generated.resources.error_network
import freedomwave.composeapp.generated.resources.error_not_found
import freedomwave.composeapp.generated.resources.error_request_failed_code
import freedomwave.composeapp.generated.resources.error_unauthorized
import freedomwave.composeapp.generated.resources.error_url_must_be_https

/** Maps any failure to a localizable UiText; backend-supplied texts pass through as raw. */
fun Throwable.toUiText(): UiText = when (this) {
    is ApiError.Unauthorized    -> UiText.Res(Res.string.error_unauthorized)
    is ApiError.NotFound        -> UiText.Res(Res.string.error_not_found)
    is ApiError.NetworkError    -> UiText.Res(Res.string.error_network)
    is ApiError.InvalidServerUrl -> UiText.Res(Res.string.error_url_must_be_https)
    is ApiError.ServerError  ->
        message?.takeIf { it.isNotBlank() }?.let { UiText.Raw(it) }
            ?: statusCode?.let { UiText.Res(Res.string.error_request_failed_code, it) }
            ?: UiText.Res(Res.string.error_generic)
    else -> message?.takeIf { it.isNotBlank() }?.let { UiText.Raw(it) }
        ?: UiText.Res(Res.string.error_generic)
}
