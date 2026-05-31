package art.yniyniyni.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HostListResponse(
    @SerialName("response") val response: List<HostDto>
)

@Serializable
data class HostDto(
    @SerialName("uuid")            val uuid: String,
    @SerialName("remark")          val remark: String,
    @SerialName("address")         val address: String,
    @SerialName("port")            val port: Int,
    @SerialName("path")            val path: String? = null,
    @SerialName("sni")             val sni: String? = null,
    @SerialName("host")            val host: String? = null,
    @SerialName("isDisabled")     val isDisabled: Boolean = false,
    @SerialName("securityLayer")   val securityLayer: String = "DEFAULT",
    @SerialName("tag")             val tag: String? = null,
    @SerialName("isHidden")        val isHidden: Boolean = false,
    @SerialName("viewPosition")    val viewPosition: Int = 0,
    @SerialName("nodes")           val nodes: List<String> = emptyList()
)
