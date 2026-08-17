package org.freedomwave.data.api.service

import org.freedomwave.data.api.ApiError
import org.freedomwave.data.api.PanelVersion
import org.freedomwave.data.api.dto.RecapResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

/**
 * Connection check used at login / "Change key". Runs on the plain (no-auth-plugin) client
 * so the bearer below is exactly the supplied [apiKey] — the Auth plugin would otherwise
 * strip a manually-set Authorization header and substitute the cached/old token.
 */
class AuthService(private val client: HttpClient) {

    /**
     * Verifies the URL/key pair and returns the panel's version.
     *
     * The recap payload carries `version` on both 2.8.x and 3.x, so the check that proves the
     * key works also tells us which user-route contract to speak. Callers persist the result;
     * see [PanelVersion].
     */
    suspend fun verifyConnection(serverUrl: String, apiKey: String): PanelVersion {
        val response = try {
            client.get("$serverUrl/api/system/stats/recap") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                // API keys are rejected for the "browser" client type; must be "android".
                header("x-remnawave-client-type", "android")
            }
        } catch (e: ApiError) {
            throw e
        } catch (e: Exception) {
            throw ApiError.NetworkError(e.message ?: "Network error")
        }

        return when (response.status.value) {
            in 200..299 ->
                // Deserialize so a 200 carrying reverse-proxy HTML can't pass as success.
                try {
                    PanelVersion.parse(response.body<RecapResponse>().response.version)
                } catch (e: Exception) {
                    throw ApiError.ServerError("")
                }
            401, 403 -> throw ApiError.Unauthorized()
            404      -> throw ApiError.NotFound()
            else     -> throw ApiError.ServerError("", response.status.value)
        }
    }
}
