package art.yniyniyni.freedomwave.data.api

sealed class ApiError(message: String) : Exception(message) {
    class Unauthorized(message: String = "Session expired, please log in again") : ApiError(message)
    class NotFound(message: String = "Resource not found") : ApiError(message)
    class ServerError(message: String) : ApiError(message)
    class NetworkError(message: String) : ApiError(message)
    class Unknown(message: String) : ApiError(message)
}
