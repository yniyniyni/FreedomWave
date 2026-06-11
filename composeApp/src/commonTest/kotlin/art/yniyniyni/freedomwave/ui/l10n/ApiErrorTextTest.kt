package art.yniyniyni.freedomwave.ui.l10n

import art.yniyniyni.freedomwave.data.api.ApiError
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.error_generic
import freedomwave.composeapp.generated.resources.error_network
import freedomwave.composeapp.generated.resources.error_not_found
import freedomwave.composeapp.generated.resources.error_request_failed_code
import freedomwave.composeapp.generated.resources.error_unauthorized
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiErrorTextTest {
    @Test fun unauthorized_maps_to_resource_ignoring_baked_message() =
        assertEquals(UiText.Res(Res.string.error_unauthorized), ApiError.Unauthorized().toUiText())

    @Test fun not_found_maps_to_resource() =
        assertEquals(UiText.Res(Res.string.error_not_found), ApiError.NotFound().toUiText())

    @Test fun network_error_maps_to_resource() =
        assertEquals(UiText.Res(Res.string.error_network), ApiError.NetworkError("timeout").toUiText())

    @Test fun server_error_keeps_backend_message() =
        assertEquals(UiText.Raw("quota exceeded"), ApiError.ServerError("quota exceeded").toUiText())

    @Test fun server_error_blank_message_falls_back_to_generic() =
        assertEquals(UiText.Res(Res.string.error_generic), ApiError.ServerError(" ").toUiText())

    @Test fun unknown_throwable_with_message_is_raw() =
        assertEquals(UiText.Raw("boom"), IllegalStateException("boom").toUiText())

    @Test fun unknown_throwable_without_message_is_generic() =
        assertEquals(UiText.Res(Res.string.error_generic), IllegalStateException().toUiText())

    @Test fun unauthorized_with_custom_message_still_maps_to_resource() =
        assertEquals(UiText.Res(Res.string.error_unauthorized), ApiError.Unauthorized("server says X").toUiText())

    @Test fun unknown_api_error_keeps_message_as_raw() =
        assertEquals(UiText.Raw("oops"), ApiError.Unknown("oops").toUiText())

    @Test fun res_vararg_constructor_equals_list_constructor() =
        assertEquals(UiText.Res(Res.string.error_generic, "a"), UiText.Res(Res.string.error_generic, args = listOf("a")))

    @Test fun server_error_blank_with_status_maps_to_request_failed_code() =
        assertEquals(UiText.Res(Res.string.error_request_failed_code, 503), ApiError.ServerError("", 503).toUiText())

    @Test fun server_error_with_message_and_status_keeps_message() =
        assertEquals(UiText.Raw("quota exceeded"), ApiError.ServerError("quota exceeded", 429).toUiText())
}
