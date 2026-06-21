package art.yniyniyni.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class SubPageConfigsResponse(@SerialName("response") val response: SubPageConfigsData)
@Serializable data class SubPageConfigsData(
    @SerialName("total") val total: Int = 0,
    @SerialName("configs") val configs: List<SubPageConfigDto> = emptyList(),
)
@Serializable data class SubPageConfigDto(@SerialName("uuid") val uuid: String, @SerialName("name") val name: String)
