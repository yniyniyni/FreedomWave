package org.freedomwave.data.api

import org.freedomwave.data.store.AppPreferences
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
        else         -> if (msg is kotlinx.serialization.json.JsonPrimitive) msg.content else null
    }
}.getOrNull()?.takeIf { it.isNotBlank() }

/**
 * Minimal client with no auth/custom headers — used for third-party calls (e.g. ipwho.is).
 * Must not send the Authorization bearer token or x-remnawave-client-type to external hosts.
 */
/**
 * The single JSON configuration used for every Remnawave request and response.
 *
 * `encodeDefaults` is left at its kotlinx default of `false`, which is what lets request DTOs
 * declare every optional field as `null` and have unset ones dropped from the body rather than
 * sent as explicit nulls — `UpdateUserRequest` relies on this to name its target by either
 * `id` or `uuid` but never both.
 */
internal val remnaJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

fun buildPlainHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(remnaJson)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 10_000
        connectTimeoutMillis = 5_000
        socketTimeoutMillis  = 10_000
    }
}

fun buildHttpClient(prefs: AppPreferences): HttpClient = HttpClient {

    install(ContentNegotiation) {
        json(remnaJson)
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
                in 500..599 -> throw ApiError.ServerError("", response.status.value)
                in 400..499 -> throw ApiError.ServerError(extractApiMessage(response.bodyAsText()) ?: "", response.status.value)
            }
        }
        handleResponseExceptionWithRequest { cause, _ ->
            if (cause !is ApiError) throw ApiError.NetworkError(cause.message ?: "Network error")
        }
    }
}
