package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.LoginRequest
import art.yniyniyni.freedomwave.data.api.dto.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthService(private val client: HttpClient) {
    suspend fun login(serverUrl: String, username: String, password: String): LoginResponse =
        client.post("$serverUrl/api/auth/login") {
            setBody(LoginRequest(username, password))
        }.body()
}
