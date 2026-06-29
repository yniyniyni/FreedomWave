package org.freedomwave.data.repository

import org.freedomwave.data.api.dto.UpdateExternalSquadRequest
import org.freedomwave.data.api.dto.UpdateInternalSquadFullRequest
import org.freedomwave.data.api.dto.reorderSquadsPayload
import org.freedomwave.data.api.service.SquadService
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.domain.model.ExternalSquadDetail
import org.freedomwave.domain.model.InternalSquadDetail
import org.freedomwave.domain.model.Squad

class SquadRepository(
    private val service: SquadService,
    private val prefs: AppPreferences
) {
    suspend fun getInternalSquads(): Result<List<Squad>> = runCatching {
        service.getInternalSquads().response.internalSquads.map { Squad.from(it) }
    }.clearOnUnauthorized(prefs)

    suspend fun createInternalSquad(name: String): Result<Squad> = runCatching {
        Squad.from(service.createInternalSquad(name).response)
    }.clearOnUnauthorized(prefs)

    suspend fun updateInternalSquad(uuid: String, name: String): Result<Squad> = runCatching {
        Squad.from(service.updateInternalSquad(uuid, name).response)
    }.clearOnUnauthorized(prefs)

    suspend fun deleteInternalSquad(uuid: String): Result<Unit> = runCatching {
        service.deleteInternalSquad(uuid)
    }.clearOnUnauthorized(prefs)

    suspend fun getExternalSquads(): Result<List<Squad>> = runCatching {
        service.getExternalSquads().response.externalSquads.map { Squad.from(it) }
    }.clearOnUnauthorized(prefs)

    suspend fun createExternalSquad(name: String): Result<Squad> = runCatching {
        Squad.from(service.createExternalSquad(name).response)
    }.clearOnUnauthorized(prefs)

    suspend fun updateExternalSquad(uuid: String, name: String): Result<Squad> = runCatching {
        Squad.from(service.updateExternalSquad(uuid, name).response)
    }.clearOnUnauthorized(prefs)

    suspend fun deleteExternalSquad(uuid: String): Result<Unit> = runCatching {
        service.deleteExternalSquad(uuid)
    }.clearOnUnauthorized(prefs)

    suspend fun getInternalSquadDetail(uuid: String): Result<InternalSquadDetail> = runCatching {
        InternalSquadDetail.from(service.getInternalSquad(uuid).response)
    }.clearOnUnauthorized(prefs)

    suspend fun getExternalSquadDetail(uuid: String): Result<ExternalSquadDetail> = runCatching {
        ExternalSquadDetail.from(service.getExternalSquad(uuid).response)
    }.clearOnUnauthorized(prefs)

    suspend fun updateInternalSquadFull(req: UpdateInternalSquadFullRequest): Result<Unit> = runCatching {
        service.updateInternalSquadFull(req)
        Unit
    }.clearOnUnauthorized(prefs)

    suspend fun updateExternalSquadFull(req: UpdateExternalSquadRequest): Result<Unit> = runCatching {
        service.updateExternalSquadFull(req)
        Unit
    }.clearOnUnauthorized(prefs)

    suspend fun reorderInternalSquads(orderedUuids: List<String>): Result<Unit> = runCatching {
        service.reorderInternalSquads(reorderSquadsPayload(orderedUuids))
    }.clearOnUnauthorized(prefs)

    suspend fun reorderExternalSquads(orderedUuids: List<String>): Result<Unit> = runCatching {
        service.reorderExternalSquads(reorderSquadsPayload(orderedUuids))
    }.clearOnUnauthorized(prefs)
}
