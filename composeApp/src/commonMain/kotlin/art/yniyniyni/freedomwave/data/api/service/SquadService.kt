package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.CreateExternalSquadRequest
import art.yniyniyni.freedomwave.data.api.dto.CreateInternalSquadRequest
import art.yniyniyni.freedomwave.data.api.dto.ExternalSquadListResponse
import art.yniyniyni.freedomwave.data.api.dto.ExternalSquadResponse
import art.yniyniyni.freedomwave.data.api.dto.InternalSquadListResponse
import art.yniyniyni.freedomwave.data.api.dto.InternalSquadResponse
import art.yniyniyni.freedomwave.data.api.dto.UpdateSquadRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SquadService(private val client: HttpClient) {

    // Internal squads
    suspend fun getInternalSquads(serverUrl: String): InternalSquadListResponse =
        client.get("$serverUrl/api/internal-squads").body()

    suspend fun createInternalSquad(serverUrl: String, name: String): InternalSquadResponse =
        client.post("$serverUrl/api/internal-squads") {
            setBody(CreateInternalSquadRequest(name))
        }.body()

    suspend fun updateInternalSquad(serverUrl: String, uuid: String, name: String): InternalSquadResponse =
        client.patch("$serverUrl/api/internal-squads") {
            setBody(UpdateSquadRequest(uuid, name))
        }.body()

    suspend fun deleteInternalSquad(serverUrl: String, uuid: String) {
        client.delete("$serverUrl/api/internal-squads/$uuid")
    }

    // External squads
    suspend fun getExternalSquads(serverUrl: String): ExternalSquadListResponse =
        client.get("$serverUrl/api/external-squads").body()

    suspend fun createExternalSquad(serverUrl: String, name: String): ExternalSquadResponse =
        client.post("$serverUrl/api/external-squads") {
            setBody(CreateExternalSquadRequest(name))
        }.body()

    suspend fun updateExternalSquad(serverUrl: String, uuid: String, name: String): ExternalSquadResponse =
        client.patch("$serverUrl/api/external-squads") {
            setBody(UpdateSquadRequest(uuid, name))
        }.body()

    suspend fun deleteExternalSquad(serverUrl: String, uuid: String) {
        client.delete("$serverUrl/api/external-squads/$uuid")
    }
}
