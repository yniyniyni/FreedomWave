package org.freedomwave.data.api.service

import org.freedomwave.data.api.dto.CreateUserRequest
import org.freedomwave.data.api.dto.UpdateUserRequest
import org.freedomwave.data.api.dto.UserDto
import org.freedomwave.data.api.dto.UserListResponse
import org.freedomwave.data.api.dto.UserResponse
import org.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class UserService(private val client: HttpClient, private val prefs: AppPreferences) {

    suspend fun getUsers(start: Int = 0, size: Int = 500): UserListResponse =
        client.get("${prefs.getServerUrl()}/api/users") {
            parameter("start", start)
            parameter("size", size)
        }.body()

    suspend fun getUser(uuid: String): UserResponse =
        client.get("${prefs.getServerUrl()}/api/users/$uuid").body()

    suspend fun createUser(request: CreateUserRequest): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users") { setBody(request) }.body()

    suspend fun updateUser(request: UpdateUserRequest): UserResponse =
        client.patch("${prefs.getServerUrl()}/api/users") { setBody(request) }.body()

    suspend fun deleteUser(uuid: String) {
        client.delete("${prefs.getServerUrl()}/api/users/$uuid")
    }

    suspend fun enableUser(uuid: String): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users/$uuid/actions/enable").body()

    suspend fun disableUser(uuid: String): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users/$uuid/actions/disable").body()

    suspend fun resetTraffic(uuid: String): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users/$uuid/actions/reset-traffic").body()

    suspend fun revokeSubscription(uuid: String): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users/$uuid/actions/revoke-subscription").body()
}
