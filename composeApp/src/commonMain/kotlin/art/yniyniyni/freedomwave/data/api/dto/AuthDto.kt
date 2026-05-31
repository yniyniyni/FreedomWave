package art.yniyniyni.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("response") val response: LoginResponseData
)

@Serializable
data class LoginResponseData(
    @SerialName("accessToken") val accessToken: String
)
