package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.BulkUuidsRequest
import art.yniyniyni.freedomwave.data.api.dto.CreateHostRequest
import art.yniyniyni.freedomwave.data.api.dto.HostListResponse
import art.yniyniyni.freedomwave.data.api.dto.HostResponse
import art.yniyniyni.freedomwave.data.api.dto.UpdateHostRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class HostService(private val client: HttpClient) {
    suspend fun getHosts(serverUrl: String): HostListResponse =
        client.get("$serverUrl/api/hosts").body()

    suspend fun enableHosts(serverUrl: String, uuids: List<String>): HostListResponse =
        client.post("$serverUrl/api/hosts/bulk/enable") {
            setBody(BulkUuidsRequest(uuids))
        }.body()

    suspend fun disableHosts(serverUrl: String, uuids: List<String>): HostListResponse =
        client.post("$serverUrl/api/hosts/bulk/disable") {
            setBody(BulkUuidsRequest(uuids))
        }.body()

    suspend fun getHost(serverUrl: String, uuid: String): HostResponse =
        client.get("$serverUrl/api/hosts/$uuid").body()

    suspend fun createHost(serverUrl: String, body: CreateHostRequest): HostResponse =
        client.post("$serverUrl/api/hosts") { setBody(body) }.body()

    suspend fun updateHost(serverUrl: String, body: UpdateHostRequest): HostResponse =
        client.patch("$serverUrl/api/hosts") { setBody(body) }.body()

    suspend fun deleteHost(serverUrl: String, uuid: String) {
        client.delete("$serverUrl/api/hosts/$uuid")
    }
}
