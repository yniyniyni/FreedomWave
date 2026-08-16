package org.freedomwave.data.api.service

import org.freedomwave.data.api.dto.CreateNodeRequest
import org.freedomwave.data.api.dto.NodeListResponse
import org.freedomwave.data.api.dto.NodeResponse
import org.freedomwave.data.api.dto.ReorderNodeItem
import org.freedomwave.data.api.dto.ReorderNodesRequest
import org.freedomwave.data.api.dto.UpdateNodeRequest
import org.freedomwave.data.store.AppPreferences
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

    // Panel 3.x answers these with no body at all — restart is 202 Accepted (the restart is
    // asynchronous) and reset-traffic is 204 No Content, where 2.8.x returned 200 with the node.
    // Deserializing an empty body throws, so neither reads one; callers re-read via getNode().

    suspend fun restartNode(uuid: String) {
        client.post("${prefs.getServerUrl()}/api/nodes/$uuid/actions/restart")
    }

    suspend fun resetTraffic(uuid: String) {
        client.post("${prefs.getServerUrl()}/api/nodes/$uuid/actions/reset-traffic")
    }

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
