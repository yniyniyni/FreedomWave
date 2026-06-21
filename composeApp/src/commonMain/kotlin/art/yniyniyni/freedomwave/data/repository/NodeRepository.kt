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
        service.getNodes().response.map { Node.from(it) }
    }

    suspend fun enableNode(uuid: String): Result<Node>   = api { Node.from(service.enableNode(uuid).response) }
    suspend fun disableNode(uuid: String): Result<Node>  = api { Node.from(service.disableNode(uuid).response) }
    suspend fun restartNode(uuid: String): Result<Node>  = api { Node.from(service.restartNode(uuid).response) }
    suspend fun resetTraffic(uuid: String): Result<Node> = api { Node.from(service.resetTraffic(uuid).response) }

    suspend fun getNode(uuid: String): Result<Node> = api { Node.from(service.getNode(uuid).response) }
    suspend fun createNode(body: CreateNodeRequest): Result<Node> = api { Node.from(service.createNode(body).response) }
    suspend fun updateNode(body: UpdateNodeRequest): Result<Node> = api { Node.from(service.updateNode(body).response) }
    suspend fun deleteNode(uuid: String): Result<Unit> = api { service.deleteNode(uuid) }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.also { it.clearOnUnauthorized(prefs) }
}
