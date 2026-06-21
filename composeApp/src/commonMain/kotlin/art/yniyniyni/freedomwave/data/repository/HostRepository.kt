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
        service.getHosts().response.map { Host.from(it) }
    }.clearOnUnauthorized(prefs)

    suspend fun enableHost(uuid: String): Result<List<Host>> = runCatching {
        service.enableHosts(listOf(uuid)).response.map { Host.from(it) }
    }.clearOnUnauthorized(prefs)

    suspend fun disableHost(uuid: String): Result<List<Host>> = runCatching {
        service.disableHosts(listOf(uuid)).response.map { Host.from(it) }
    }.clearOnUnauthorized(prefs)

    suspend fun getHost(uuid: String): Result<Host> = runCatching {
        Host.from(service.getHost(uuid).response)
    }.clearOnUnauthorized(prefs)

    suspend fun createHost(body: CreateHostRequest): Result<Host> = runCatching {
        Host.from(service.createHost(body).response)
    }.clearOnUnauthorized(prefs)

    suspend fun updateHost(body: UpdateHostRequest): Result<Host> = runCatching {
        Host.from(service.updateHost(body).response)
    }.clearOnUnauthorized(prefs)

    suspend fun deleteHost(uuid: String): Result<Unit> = runCatching {
        service.deleteHost(uuid)
    }.clearOnUnauthorized(prefs)
}
