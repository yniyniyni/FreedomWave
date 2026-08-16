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

    suspend fun getUser(userRef: String): UserResponse =
        client.get("${prefs.getServerUrl()}/api/users/$userRef").body()

    suspend fun createUser(request: CreateUserRequest): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users") { setBody(request) }.body()

    suspend fun updateUser(request: UpdateUserRequest): UserResponse =
        client.patch("${prefs.getServerUrl()}/api/users") { setBody(request) }.body()

    suspend fun deleteUser(userRef: String) {
        client.delete("${prefs.getServerUrl()}/api/users/$userRef")
    }

    suspend fun enableUser(userRef: String): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users/$userRef/actions/enable").body()

    suspend fun disableUser(userRef: String): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users/$userRef/actions/disable").body()

    suspend fun resetTraffic(userRef: String): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users/$userRef/actions/reset-traffic").body()

    // `revoke`, not `revoke-subscription`: the route was renamed in panel 2.8.0, so the old
    // path 404s on every currently supported version. Still 200 with the user on 2.8.x and 3.x.
    suspend fun revokeSubscription(userRef: String): UserResponse =
        client.post("${prefs.getServerUrl()}/api/users/$userRef/actions/revoke").body()
}
