package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.CreateUserRequest
import art.yniyniyni.freedomwave.data.api.dto.UpdateUserRequest
import art.yniyniyni.freedomwave.data.api.dto.UserDto
import art.yniyniyni.freedomwave.data.api.dto.UserListResponse
import art.yniyniyni.freedomwave.data.api.dto.UserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class UserService(private val client: HttpClient) {

    suspend fun getUsers(serverUrl: String, start: Int = 0, size: Int = 500): UserListResponse =
        client.get("$serverUrl/api/users") {
            parameter("start", start)
            parameter("size", size)
        }.body()

    suspend fun getUser(serverUrl: String, uuid: String): UserResponse =
        client.get("$serverUrl/api/users/$uuid").body()

    suspend fun createUser(serverUrl: String, request: CreateUserRequest): UserResponse =
        client.post("$serverUrl/api/users") { setBody(request) }.body()

    suspend fun updateUser(serverUrl: String, request: UpdateUserRequest): UserResponse =
        client.patch("$serverUrl/api/users") { setBody(request) }.body()

    suspend fun deleteUser(serverUrl: String, uuid: String) {
        client.delete("$serverUrl/api/users/$uuid")
    }

    suspend fun enableUser(serverUrl: String, uuid: String): UserResponse =
        client.post("$serverUrl/api/users/$uuid/actions/enable").body()

    suspend fun disableUser(serverUrl: String, uuid: String): UserResponse =
        client.post("$serverUrl/api/users/$uuid/actions/disable").body()

    suspend fun resetTraffic(serverUrl: String, uuid: String): UserResponse =
        client.post("$serverUrl/api/users/$uuid/actions/reset-traffic").body()

    suspend fun revokeSubscription(serverUrl: String, uuid: String): UserResponse =
        client.post("$serverUrl/api/users/$uuid/actions/revoke-subscription").body()
}
