package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.CreateExternalSquadRequest
import art.yniyniyni.freedomwave.data.api.dto.CreateInternalSquadRequest
import art.yniyniyni.freedomwave.data.api.dto.ExternalSquadDetailResponse
import art.yniyniyni.freedomwave.data.api.dto.ExternalSquadListResponse
import art.yniyniyni.freedomwave.data.api.dto.ExternalSquadResponse
import art.yniyniyni.freedomwave.data.api.dto.InternalSquadDetailResponse
import art.yniyniyni.freedomwave.data.api.dto.InternalSquadListResponse
import art.yniyniyni.freedomwave.data.api.dto.InternalSquadResponse
import art.yniyniyni.freedomwave.data.api.dto.ReorderSquadItem
import art.yniyniyni.freedomwave.data.api.dto.ReorderSquadsRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateExternalSquadRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateInternalSquadFullRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateSquadRequest
import art.yniyniyni.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SquadService(private val client: HttpClient, private val prefs: AppPreferences) {

    // Internal squads
    suspend fun getInternalSquads(): InternalSquadListResponse =
        client.get("${prefs.getServerUrl()}/api/internal-squads").body()

    suspend fun createInternalSquad(name: String): InternalSquadResponse =
        client.post("${prefs.getServerUrl()}/api/internal-squads") {
            setBody(CreateInternalSquadRequest(name, emptyList()))
        }.body()

    suspend fun updateInternalSquad(uuid: String, name: String): InternalSquadResponse =
        client.patch("${prefs.getServerUrl()}/api/internal-squads") {
            setBody(UpdateSquadRequest(uuid, name))
        }.body()

    suspend fun deleteInternalSquad(uuid: String) {
        client.delete("${prefs.getServerUrl()}/api/internal-squads/$uuid")
    }

    // External squads
    suspend fun getExternalSquads(): ExternalSquadListResponse =
        client.get("${prefs.getServerUrl()}/api/external-squads").body()

    suspend fun createExternalSquad(name: String): ExternalSquadResponse =
        client.post("${prefs.getServerUrl()}/api/external-squads") {
            setBody(CreateExternalSquadRequest(name))
        }.body()

    suspend fun updateExternalSquad(uuid: String, name: String): ExternalSquadResponse =
        client.patch("${prefs.getServerUrl()}/api/external-squads") {
            setBody(UpdateSquadRequest(uuid, name))
        }.body()

    suspend fun deleteExternalSquad(uuid: String) {
        client.delete("${prefs.getServerUrl()}/api/external-squads/$uuid")
    }

    suspend fun getInternalSquad(uuid: String): InternalSquadDetailResponse =
        client.get("${prefs.getServerUrl()}/api/internal-squads/$uuid").body()

    suspend fun getExternalSquad(uuid: String): ExternalSquadDetailResponse =
        client.get("${prefs.getServerUrl()}/api/external-squads/$uuid").body()

    suspend fun updateInternalSquadFull(req: UpdateInternalSquadFullRequest): InternalSquadResponse =
        client.patch("${prefs.getServerUrl()}/api/internal-squads") { setBody(req) }.body()

    suspend fun updateExternalSquadFull(req: UpdateExternalSquadRequest): ExternalSquadResponse =
        client.patch("${prefs.getServerUrl()}/api/external-squads") { setBody(req) }.body()

    suspend fun reorderInternalSquads(items: List<ReorderSquadItem>) {
        client.post("${prefs.getServerUrl()}/api/internal-squads/actions/reorder") {
            setBody(ReorderSquadsRequest(items))
        }
    }

    suspend fun reorderExternalSquads(items: List<ReorderSquadItem>) {
        client.post("${prefs.getServerUrl()}/api/external-squads/actions/reorder") {
            setBody(ReorderSquadsRequest(items))
        }
    }
}
