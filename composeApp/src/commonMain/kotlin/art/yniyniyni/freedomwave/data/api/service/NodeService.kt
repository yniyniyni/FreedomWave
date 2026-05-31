package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.NodeListResponse
import art.yniyniyni.freedomwave.data.api.dto.NodeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post

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
}
