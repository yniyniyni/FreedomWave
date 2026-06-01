package art.yniyniyni.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BandwidthNodesResponse(
    @SerialName("response") val response: BandwidthNodesData
)

@Serializable
data class BandwidthNodesData(
    @SerialName("categories")    val categories:    List<String>,
    @SerialName("sparklineData") val sparklineData: List<Double>,
    @SerialName("topNodes")      val topNodes:      List<BandwidthNodeSummaryDto>,
    @SerialName("series")        val series:        List<BandwidthNodeSeriesDto>
)

@Serializable
data class BandwidthNodeSummaryDto(
    @SerialName("uuid")        val uuid:        String,
    @SerialName("color")       val color:       String,
    @SerialName("name")        val name:        String,
    @SerialName("countryCode") val countryCode: String,
    @SerialName("total")       val total:       Double
)

@Serializable
data class BandwidthNodeSeriesDto(
    @SerialName("uuid")        val uuid:        String,
    @SerialName("name")        val name:        String,
    @SerialName("color")       val color:       String,
    @SerialName("countryCode") val countryCode: String,
    @SerialName("total")       val total:       Double,
    @SerialName("data")        val data:        List<Double>
)
