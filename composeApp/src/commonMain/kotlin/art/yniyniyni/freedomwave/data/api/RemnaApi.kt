package art.yniyniyni.freedomwave.data.api

import art.yniyniyni.freedomwave.data.store.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Pull a human-readable message out of a Remnawave/NestJS error body, e.g.
 * `{"message":"Username must be at least 3 characters","error":"Bad Request","statusCode":400}`
 * or a zod-style `{"message":["err a","err b"]}`. Falls back to null on anything unexpected.
 */
private fun extractApiMessage(body: String): String? = runCatching {
    val root = Json.parseToJsonElement(body).jsonObject
    when (val msg = root["message"]) {
        is JsonArray -> msg.joinToString("; ") { it.jsonPrimitive.content }
        null         -> root["error"]?.jsonPrimitive?.content
        else         -> msg.jsonPrimitive.content
    }
}.getOrNull()?.takeIf { it.isNotBlank() }

fun buildHttpClient(prefs: AppPreferences): HttpClient = HttpClient {

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        })
    }

    install(Auth) {
        bearer {
            loadTokens {
                prefs.getApiKey()?.let { BearerTokens(it, "") }
            }
            refreshTokens { null }
        }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis  = 30_000
    }

    defaultRequest {
        headers.append("x-remnawave-client-type", "android")
        contentType(ContentType.Application.Json)
    }

    HttpResponseValidator {
        validateResponse { response ->
            when (response.status.value) {
                401         -> throw ApiError.Unauthorized()
                404         -> throw ApiError.NotFound()
                in 400..499 -> {
                    val body = response.bodyAsText()
                    throw ApiError.ServerError(
                        extractApiMessage(body) ?: "Request failed (${response.status.value})"
                    )
                }
                in 500..599 -> throw ApiError.ServerError("Server error ${response.status.value}: ${response.bodyAsText()}")
            }
        }
        handleResponseExceptionWithRequest { cause, _ ->
            if (cause !is ApiError) throw ApiError.NetworkError(cause.message ?: "Network error")
        }
    }
}
