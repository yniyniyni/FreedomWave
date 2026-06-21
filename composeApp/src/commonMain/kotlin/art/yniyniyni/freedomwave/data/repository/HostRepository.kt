package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.dto.CreateHostRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateHostRequest
import art.yniyniyni.freedomwave.data.api.service.HostService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.Host

class HostRepository(
    private val service: HostService,
    private val prefs: AppPreferences
) {
    suspend fun getHosts(): Result<List<Host>> = runCatching {
        service.getHosts(prefs.getServerUrl()).response.map { Host.from(it) }
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun enableHost(uuid: String): Result<List<Host>> = runCatching {
        service.enableHosts(prefs.getServerUrl(), listOf(uuid)).response.map { Host.from(it) }
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun disableHost(uuid: String): Result<List<Host>> = runCatching {
        service.disableHosts(prefs.getServerUrl(), listOf(uuid)).response.map { Host.from(it) }
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun getHost(uuid: String): Result<Host> = runCatching {
        Host.from(service.getHost(prefs.getServerUrl(), uuid).response)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun createHost(body: CreateHostRequest): Result<Host> = runCatching {
        Host.from(service.createHost(prefs.getServerUrl(), body).response)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun updateHost(body: UpdateHostRequest): Result<Host> = runCatching {
        Host.from(service.updateHost(prefs.getServerUrl(), body).response)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun deleteHost(uuid: String): Result<Unit> = runCatching {
        service.deleteHost(prefs.getServerUrl(), uuid)
    }.also { it.clearOnUnauthorized(prefs) }
}
