package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.BulkUuidsRequest
import art.yniyniyni.freedomwave.data.api.dto.CreateHostRequest
import art.yniyniyni.freedomwave.data.api.dto.HostListResponse
import art.yniyniyni.freedomwave.data.api.dto.HostResponse
import art.yniyniyni.freedomwave.data.api.dto.ReorderHostItem
import art.yniyniyni.freedomwave.data.api.dto.ReorderHostsRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateHostRequest
import art.yniyniyni.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class HostService(private val client: HttpClient, private val prefs: AppPreferences) {
    suspend fun getHosts(): HostListResponse =
        client.get("${prefs.getServerUrl()}/api/hosts").body()

    suspend fun enableHosts(uuids: List<String>): HostListResponse =
        client.post("${prefs.getServerUrl()}/api/hosts/bulk/enable") {
            setBody(BulkUuidsRequest(uuids))
        }.body()

    suspend fun disableHosts(uuids: List<String>): HostListResponse =
        client.post("${prefs.getServerUrl()}/api/hosts/bulk/disable") {
            setBody(BulkUuidsRequest(uuids))
        }.body()

    suspend fun getHost(uuid: String): HostResponse =
        client.get("${prefs.getServerUrl()}/api/hosts/$uuid").body()

    suspend fun createHost(body: CreateHostRequest): HostResponse =
        client.post("${prefs.getServerUrl()}/api/hosts") { setBody(body) }.body()

    suspend fun updateHost(body: UpdateHostRequest): HostResponse =
        client.patch("${prefs.getServerUrl()}/api/hosts") { setBody(body) }.body()

    suspend fun deleteHost(uuid: String) {
        client.delete("${prefs.getServerUrl()}/api/hosts/$uuid")
    }

    suspend fun reorderHosts(hosts: List<ReorderHostItem>) {
        client.post("${prefs.getServerUrl()}/api/hosts/actions/reorder") {
            setBody(ReorderHostsRequest(hosts))
        }
    }
}
