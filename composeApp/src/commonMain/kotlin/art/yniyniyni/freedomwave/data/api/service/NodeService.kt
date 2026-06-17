package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.CreateNodeRequest
import art.yniyniyni.freedomwave.data.api.dto.NodeListResponse
import art.yniyniyni.freedomwave.data.api.dto.NodeResponse
import art.yniyniyni.freedomwave.data.api.dto.UpdateNodeRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class NodeService(private val client: HttpClient) {
    suspend fun getNodes(serverUrl: String): NodeListResponse =
        client.get("$serverUrl/api/nodes").body()

    suspend fun getNode(serverUrl: String, uuid: String): NodeResponse =
        client.get("$serverUrl/api/nodes/$uuid").body()

    suspend fun enableNode(serverUrl: String, uuid: String): NodeResponse =
        client.post("$serverUrl/api/nodes/$uuid/actions/enable").body()

    suspend fun disableNode(serverUrl: String, uuid: String): NodeResponse =
        client.post("$serverUrl/api/nodes/$uuid/actions/disable").body()

    suspend fun restartNode(serverUrl: String, uuid: String): NodeResponse =
        client.post("$serverUrl/api/nodes/$uuid/actions/restart").body()

    suspend fun resetTraffic(serverUrl: String, uuid: String): NodeResponse =
        client.post("$serverUrl/api/nodes/$uuid/actions/reset-traffic").body()

    suspend fun createNode(serverUrl: String, body: CreateNodeRequest): NodeResponse =
        client.post("$serverUrl/api/nodes") { setBody(body) }.body()

    suspend fun updateNode(serverUrl: String, body: UpdateNodeRequest): NodeResponse =
        client.patch("$serverUrl/api/nodes") { setBody(body) }.body()

    suspend fun deleteNode(serverUrl: String, uuid: String) {
        client.delete("$serverUrl/api/nodes/$uuid")
    }
}
