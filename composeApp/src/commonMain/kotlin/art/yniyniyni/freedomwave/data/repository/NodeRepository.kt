package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.dto.CreateNodeRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateNodeRequest
import art.yniyniyni.freedomwave.data.api.service.NodeService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.Node

class NodeRepository(
    private val service: NodeService,
    private val prefs: AppPreferences
) {
    suspend fun getNodes(): Result<List<Node>> = api {
        service.getNodes(prefs.getServerUrl()).response.map { Node.from(it) }
    }

    suspend fun enableNode(uuid: String): Result<Node>   = api { Node.from(service.enableNode(prefs.getServerUrl(), uuid).response) }
    suspend fun disableNode(uuid: String): Result<Node>  = api { Node.from(service.disableNode(prefs.getServerUrl(), uuid).response) }
    suspend fun restartNode(uuid: String): Result<Node>  = api { Node.from(service.restartNode(prefs.getServerUrl(), uuid).response) }
    suspend fun resetTraffic(uuid: String): Result<Node> = api { Node.from(service.resetTraffic(prefs.getServerUrl(), uuid).response) }

    suspend fun getNode(uuid: String): Result<Node> = api { Node.from(service.getNode(prefs.getServerUrl(), uuid).response) }
    suspend fun createNode(body: CreateNodeRequest): Result<Node> = api { Node.from(service.createNode(prefs.getServerUrl(), body).response) }
    suspend fun updateNode(body: UpdateNodeRequest): Result<Node> = api { Node.from(service.updateNode(prefs.getServerUrl(), body).response) }
    suspend fun deleteNode(uuid: String): Result<Unit> = api { service.deleteNode(prefs.getServerUrl(), uuid) }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.also { it.clearOnUnauthorized(prefs) }
}
