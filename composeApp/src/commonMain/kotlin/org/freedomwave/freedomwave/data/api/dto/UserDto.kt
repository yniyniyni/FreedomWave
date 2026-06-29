package org.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserListResponse(
    @SerialName("response") val response: UserListData
)

@Serializable
data class UserListData(
    @SerialName("users") val users: List<UserDto>,
    @SerialName("total") val total: Int
)

@Serializable
data class UserResponse(
    @SerialName("response") val response: UserDto
)

@Serializable
data class UserDto(
    @SerialName("uuid")                     val uuid: String,
    @SerialName("id")                       val id: Int,
    @SerialName("shortUuid")                val shortUuid: String,
    @SerialName("username")                 val username: String,
    @SerialName("status")                   val status: String,
    @SerialName("trafficLimitBytes")        val trafficLimitBytes: Long,
    @SerialName("trafficLimitStrategy")     val trafficLimitStrategy: String,
    @SerialName("expireAt")                 val expireAt: String,
    @SerialName("telegramId")               val telegramId: Long? = null,
    @SerialName("email")                    val email: String? = null,
    @SerialName("description")              val description: String? = null,
    @SerialName("tag")                      val tag: String? = null,
    @SerialName("hwidDeviceLimit")          val hwidDeviceLimit: Int? = null,
    @SerialName("externalSquadUuid")        val externalSquadUuid: String? = null,
    @SerialName("trojanPassword")           val trojanPassword: String,
    @SerialName("vlessUuid")               val vlessUuid: String,
    @SerialName("ssPassword")              val ssPassword: String,
    @SerialName("subRevokedAt")            val subRevokedAt: String? = null,
    @SerialName("lastTrafficResetAt")      val lastTrafficResetAt: String? = null,
    @SerialName("createdAt")               val createdAt: String,
    @SerialName("updatedAt")               val updatedAt: String,
    @SerialName("subscriptionUrl")         val subscriptionUrl: String = "",
    @SerialName("activeInternalSquads")    val activeInternalSquads: List<SquadRefDto> = emptyList(),
    @SerialName("userTraffic")             val userTraffic: UserTrafficDto
)

@Serializable
data class UserTrafficDto(
    @SerialName("usedTrafficBytes")         val usedTrafficBytes: Long,
    @SerialName("lifetimeUsedTrafficBytes") val lifetimeUsedTrafficBytes: Long,
    @SerialName("onlineAt")                 val onlineAt: String? = null,
    @SerialName("firstConnectedAt")         val firstConnectedAt: String? = null,
    @SerialName("lastConnectedNodeUuid")    val lastConnectedNodeUuid: String? = null
)

@Serializable
data class SquadRefDto(
    @SerialName("uuid") val uuid: String,
    @SerialName("name") val name: String
)

// Create / Update requests
@Serializable
data class CreateUserRequest(
    @SerialName("username")               val username: String,
    @SerialName("status")                 val status: String = "ACTIVE",
    @SerialName("trafficLimitBytes")      val trafficLimitBytes: Long = 0,
    @SerialName("trafficLimitStrategy")   val trafficLimitStrategy: String = "NO_RESET",
    @SerialName("expireAt")               val expireAt: String? = null,
    @SerialName("description")            val description: String? = null,
    @SerialName("tag")                    val tag: String? = null,
    @SerialName("email")                  val email: String? = null,
    @SerialName("telegramId")             val telegramId: Long? = null,
    @SerialName("hwidDeviceLimit")        val hwidDeviceLimit: Int? = null,
    @SerialName("activeInternalSquads")   val activeInternalSquads: List<String>? = null,
)

@Serializable
data class UpdateUserRequest(
    @SerialName("uuid")                   val uuid: String,
    @SerialName("status")                 val status: String? = null,
    @SerialName("trafficLimitBytes")      val trafficLimitBytes: Long? = null,
    @SerialName("trafficLimitStrategy")   val trafficLimitStrategy: String? = null,
    @SerialName("expireAt")               val expireAt: String? = null,
    @SerialName("description")            val description: String? = null,
    @SerialName("tag")                    val tag: String? = null,
    @SerialName("email")                  val email: String? = null,
    @SerialName("telegramId")             val telegramId: Long? = null,
    @SerialName("hwidDeviceLimit")        val hwidDeviceLimit: Int? = null,
    @SerialName("activeInternalSquads")   val activeInternalSquads: List<String>? = null,
)
