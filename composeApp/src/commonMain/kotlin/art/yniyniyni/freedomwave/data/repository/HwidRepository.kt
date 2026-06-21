package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.service.HwidService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.HwidDevice

class HwidRepository(
    private val service: HwidService,
    private val prefs: AppPreferences
) {
    suspend fun getDevices(userUuid: String): Result<List<HwidDevice>> = api {
        service.getDevices(userUuid)
            .response.devices.map { HwidDevice.from(it) }
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.clearOnUnauthorized(prefs)
}
