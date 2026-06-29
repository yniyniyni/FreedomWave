package org.freedomwave.data.repository

import org.freedomwave.data.api.dto.CreateHostRequest
import org.freedomwave.data.api.dto.UpdateHostRequest
import org.freedomwave.data.api.dto.reorderHostsPayload
import org.freedomwave.data.api.service.HostService
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.domain.model.Host

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

    suspend fun reorderHosts(orderedUuids: List<String>): Result<Unit> = runCatching {
        service.reorderHosts(reorderHostsPayload(orderedUuids))
    }.clearOnUnauthorized(prefs)
}
