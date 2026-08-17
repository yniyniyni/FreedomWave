package org.freedomwave.data.repository

import org.freedomwave.data.api.dto.CreateNodeRequest
import org.freedomwave.data.api.dto.UpdateNodeRequest
import org.freedomwave.data.api.dto.reorderNodesPayload
import org.freedomwave.data.api.service.NodeService
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.domain.model.Node

class NodeRepository(
    private val service: NodeService,
    private val prefs: AppPreferences
) {
    suspend fun getNodes(): Result<List<Node>> = api {
        service.getNodes().response.map { Node.from(it) }
    }

    suspend fun enableNode(uuid: String): Result<Node>   = api { Node.from(service.enableNode(uuid).response) }
    suspend fun disableNode(uuid: String): Result<Node>  = api { Node.from(service.disableNode(uuid).response) }
    // Both actions return no body on panel 3.x (202 / 204), so the fresh node comes from a
    // follow-up read. Restart is asynchronous even on success — the node returned here reflects
    // the moment the restart was accepted, not a completed restart.
    suspend fun restartNode(uuid: String): Result<Node> = api {
        service.restartNode(uuid)
        Node.from(service.getNode(uuid).response)
    }

    suspend fun resetTraffic(uuid: String): Result<Node> = api {
        service.resetTraffic(uuid)
        Node.from(service.getNode(uuid).response)
    }

    suspend fun getNode(uuid: String): Result<Node> = api { Node.from(service.getNode(uuid).response) }
    suspend fun createNode(body: CreateNodeRequest): Result<Node> = api { Node.from(service.createNode(body).response) }
    suspend fun updateNode(body: UpdateNodeRequest): Result<Node> = api { Node.from(service.updateNode(body).response) }
    suspend fun deleteNode(uuid: String): Result<Unit> = api { service.deleteNode(uuid) }

    suspend fun reorderNodes(orderedUuids: List<String>): Result<Unit> = api {
        service.reorderNodes(reorderNodesPayload(orderedUuids))
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.clearOnUnauthorized(prefs)
}
