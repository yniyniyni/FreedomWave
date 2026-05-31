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
                prefs.getToken()?.let { BearerTokens(it, "") }
            }
            // No silent refresh — 401 clears token and re-login is required
            refreshTokens { null }
        }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis  = 30_000
    }

    defaultRequest {
        headers.append("x-remnawave-client-type", "browser")
        contentType(ContentType.Application.Json)
    }

    HttpResponseValidator {
        validateResponse { response ->
            when (response.status.value) {
                401         -> throw ApiError.Unauthorized()
                404         -> throw ApiError.NotFound()
                in 500..599 -> throw ApiError.ServerError("Server error ${response.status.value}: ${response.bodyAsText()}")
            }
        }
        handleResponseExceptionWithRequest { cause, _ ->
            if (cause !is ApiError) throw ApiError.NetworkError(cause.message ?: "Network error")
        }
    }
}
