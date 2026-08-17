package org.freedomwave.data.api

// messages are for logs only; UI text comes from ui/l10n/ApiErrorText.kt
sealed class ApiError(message: String) : Exception(message) {
    class Unauthorized(message: String = "") : ApiError(message)
    class NotFound(message: String = "") : ApiError(message)
    class ServerError(message: String, val statusCode: Int? = null) : ApiError(message)
    class NetworkError(message: String) : ApiError(message)
    /**
     * The panel answered, but the payload did not match what this build expects — almost always
     * a panel version whose schema has moved. Kept distinct from [NetworkError]: reporting a
     * decode failure as "check your connection" sends people to debug the wrong thing.
     */
    class UnexpectedResponse(message: String) : ApiError(message)
    class InvalidServerUrl(message: String = "") : ApiError(message)
    class Unknown(message: String) : ApiError(message)
}
