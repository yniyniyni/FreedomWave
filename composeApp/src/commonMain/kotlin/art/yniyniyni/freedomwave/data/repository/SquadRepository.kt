package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.dto.UpdateExternalSquadRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateInternalSquadFullRequest
import art.yniyniyni.freedomwave.data.api.service.SquadService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.ExternalSquadDetail
import art.yniyniyni.freedomwave.domain.model.InternalSquadDetail
import art.yniyniyni.freedomwave.domain.model.Squad

class SquadRepository(
    private val service: SquadService,
    private val prefs: AppPreferences
) {
    suspend fun getInternalSquads(): Result<List<Squad>> = runCatching {
        service.getInternalSquads().response.internalSquads.map { Squad.from(it) }
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun createInternalSquad(name: String): Result<Squad> = runCatching {
        Squad.from(service.createInternalSquad(name).response)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun updateInternalSquad(uuid: String, name: String): Result<Squad> = runCatching {
        Squad.from(service.updateInternalSquad(uuid, name).response)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun deleteInternalSquad(uuid: String): Result<Unit> = runCatching {
        service.deleteInternalSquad(uuid)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun getExternalSquads(): Result<List<Squad>> = runCatching {
        service.getExternalSquads().response.externalSquads.map { Squad.from(it) }
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun createExternalSquad(name: String): Result<Squad> = runCatching {
        Squad.from(service.createExternalSquad(name).response)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun updateExternalSquad(uuid: String, name: String): Result<Squad> = runCatching {
        Squad.from(service.updateExternalSquad(uuid, name).response)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun deleteExternalSquad(uuid: String): Result<Unit> = runCatching {
        service.deleteExternalSquad(uuid)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun getInternalSquadDetail(uuid: String): Result<InternalSquadDetail> = runCatching {
        InternalSquadDetail.from(service.getInternalSquad(uuid).response)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun getExternalSquadDetail(uuid: String): Result<ExternalSquadDetail> = runCatching {
        ExternalSquadDetail.from(service.getExternalSquad(uuid).response)
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun updateInternalSquadFull(req: UpdateInternalSquadFullRequest): Result<Unit> = runCatching {
        service.updateInternalSquadFull(req)
        Unit
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun updateExternalSquadFull(req: UpdateExternalSquadRequest): Result<Unit> = runCatching {
        service.updateExternalSquadFull(req)
        Unit
    }.also { it.clearOnUnauthorized(prefs) }
}
