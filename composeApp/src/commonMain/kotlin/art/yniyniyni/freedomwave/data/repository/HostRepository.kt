package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.api.service.HostService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.Host

class HostRepository(
    private val service: HostService,
    private val prefs: AppPreferences
) {
    suspend fun getHosts(): Result<List<Host>> = runCatching {
        service.getHosts(prefs.getServerUrl()).response.map { Host.from(it) }
    }.also { clearOnUnauthorized(it) }

    suspend fun enableHost(uuid: String): Result<List<Host>> = runCatching {
        service.enableHosts(prefs.getServerUrl(), listOf(uuid)).response.map { Host.from(it) }
    }.also { clearOnUnauthorized(it) }

    suspend fun disableHost(uuid: String): Result<List<Host>> = runCatching {
        service.disableHosts(prefs.getServerUrl(), listOf(uuid)).response.map { Host.from(it) }
    }.also { clearOnUnauthorized(it) }

    suspend fun deleteHost(uuid: String): Result<Unit> = runCatching {
        service.deleteHost(prefs.getServerUrl(), uuid)
    }.also { clearOnUnauthorized(it) }

    private suspend fun <T> clearOnUnauthorized(result: Result<T>) {
        if (result.exceptionOrNull() is ApiError.Unauthorized) prefs.clearCredentials()
    }
}
