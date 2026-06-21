package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
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
        service.getInternalSquads(prefs.getServerUrl()).response.internalSquads.map { Squad.from(it) }
    }.also { clearOnUnauthorized(it) }

    suspend fun createInternalSquad(name: String): Result<Squad> = runCatching {
        Squad.from(service.createInternalSquad(prefs.getServerUrl(), name).response)
    }.also { clearOnUnauthorized(it) }

    suspend fun updateInternalSquad(uuid: String, name: String): Result<Squad> = runCatching {
        Squad.from(service.updateInternalSquad(prefs.getServerUrl(), uuid, name).response)
    }.also { clearOnUnauthorized(it) }

    suspend fun deleteInternalSquad(uuid: String): Result<Unit> = runCatching {
        service.deleteInternalSquad(prefs.getServerUrl(), uuid)
    }.also { clearOnUnauthorized(it) }

    suspend fun getExternalSquads(): Result<List<Squad>> = runCatching {
        service.getExternalSquads(prefs.getServerUrl()).response.externalSquads.map { Squad.from(it) }
    }.also { clearOnUnauthorized(it) }

    suspend fun createExternalSquad(name: String): Result<Squad> = runCatching {
        Squad.from(service.createExternalSquad(prefs.getServerUrl(), name).response)
    }.also { clearOnUnauthorized(it) }

    suspend fun updateExternalSquad(uuid: String, name: String): Result<Squad> = runCatching {
        Squad.from(service.updateExternalSquad(prefs.getServerUrl(), uuid, name).response)
    }.also { clearOnUnauthorized(it) }

    suspend fun deleteExternalSquad(uuid: String): Result<Unit> = runCatching {
        service.deleteExternalSquad(prefs.getServerUrl(), uuid)
    }.also { clearOnUnauthorized(it) }

    suspend fun getInternalSquadDetail(uuid: String): Result<InternalSquadDetail> = runCatching {
        InternalSquadDetail.from(service.getInternalSquad(prefs.getServerUrl(), uuid).response)
    }.also { clearOnUnauthorized(it) }

    suspend fun getExternalSquadDetail(uuid: String): Result<ExternalSquadDetail> = runCatching {
        ExternalSquadDetail.from(service.getExternalSquad(prefs.getServerUrl(), uuid).response)
    }.also { clearOnUnauthorized(it) }

    suspend fun updateInternalSquadFull(req: UpdateInternalSquadFullRequest): Result<Unit> = runCatching {
        service.updateInternalSquadFull(prefs.getServerUrl(), req); Unit
    }.also { clearOnUnauthorized(it) }

    suspend fun updateExternalSquadFull(req: UpdateExternalSquadRequest): Result<Unit> = runCatching {
        service.updateExternalSquadFull(prefs.getServerUrl(), req); Unit
    }.also { clearOnUnauthorized(it) }

    private suspend fun <T> clearOnUnauthorized(result: Result<T>) {
        if (result.exceptionOrNull() is ApiError.Unauthorized) prefs.clearCredentials()
    }
}
