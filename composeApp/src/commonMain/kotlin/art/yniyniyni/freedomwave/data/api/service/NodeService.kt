package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.CreateNodeRequest
import art.yniyniyni.freedomwave.data.api.dto.NodeListResponse
import art.yniyniyni.freedomwave.data.api.dto.NodeResponse
import art.yniyniyni.freedomwave.data.api.dto.ReorderNodeItem
import art.yniyniyni.freedomwave.data.api.dto.ReorderNodesRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateNodeRequest
import art.yniyniyni.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class NodeService(private val client: HttpClient, private val prefs: AppPreferences) {
    suspend fun getNodes(): NodeListResponse =
        client.get("${prefs.getServerUrl()}/api/nodes").body()

    suspend fun getNode(uuid: String): NodeResponse =
        client.get("${prefs.getServerUrl()}/api/nodes/$uuid").body()

    suspend fun enableNode(uuid: String): NodeResponse =
        client.post("${prefs.getServerUrl()}/api/nodes/$uuid/actions/enable").body()

    suspend fun disableNode(uuid: String): NodeResponse =
        client.post("${prefs.getServerUrl()}/api/nodes/$uuid/actions/disable").body()

    suspend fun restartNode(uuid: String): NodeResponse =
        client.post("${prefs.getServerUrl()}/api/nodes/$uuid/actions/restart").body()

    suspend fun resetTraffic(uuid: String): NodeResponse =
        client.post("${prefs.getServerUrl()}/api/nodes/$uuid/actions/reset-traffic").body()

    suspend fun createNode(body: CreateNodeRequest): NodeResponse =
        client.post("${prefs.getServerUrl()}/api/nodes") { setBody(body) }.body()

    suspend fun updateNode(body: UpdateNodeRequest): NodeResponse =
        client.patch("${prefs.getServerUrl()}/api/nodes") { setBody(body) }.body()

    suspend fun deleteNode(uuid: String) {
        client.delete("${prefs.getServerUrl()}/api/nodes/$uuid")
    }

    suspend fun reorderNodes(nodes: List<ReorderNodeItem>) {
        client.post("${prefs.getServerUrl()}/api/nodes/actions/reorder") {
            setBody(ReorderNodesRequest(nodes))
        }
    }
}
