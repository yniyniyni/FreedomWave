package art.yniyniyni.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// GET /api/system/stats/system-stats
@Serializable
data class SystemStatsResponse(
    @SerialName("response") val response: SystemStatsData
)

@Serializable
data class SystemStatsData(
    @SerialName("cpu")         val cpu: CpuData,
    @SerialName("memory")      val memory: MemoryData,
    @SerialName("uptime")      val uptime: Double,
    @SerialName("timestamp")   val timestamp: Long,
    @SerialName("users")       val users: UsersData,
    @SerialName("onlineStats") val onlineStats: OnlineStatsData,
    @SerialName("nodes")       val nodes: NodesData
)

@Serializable
data class CpuData(
    @SerialName("cores") val cores: Int
)

@Serializable
data class MemoryData(
    @SerialName("total") val total: Long,
    @SerialName("free")  val free: Long,
    @SerialName("used")  val used: Long
)

@Serializable
data class UsersData(
    @SerialName("statusCounts") val statusCounts: Map<String, Int>,
    @SerialName("totalUsers")   val totalUsers: Int
)

@Serializable
data class OnlineStatsData(
    @SerialName("lastDay")     val lastDay: Int,
    @SerialName("lastWeek")    val lastWeek: Int,
    @SerialName("neverOnline") val neverOnline: Int,
    @SerialName("onlineNow")   val onlineNow: Int
)

@Serializable
data class NodesData(
    @SerialName("totalOnline")       val totalOnline: Int,
    @SerialName("totalBytesLifetime") val totalBytesLifetime: String
)

// GET /api/system/stats/recap
@Serializable
data class RecapResponse(
    @SerialName("response") val response: RecapData
)

@Serializable
data class RecapData(
    @SerialName("thisMonth") val thisMonth: RecapPeriod,
    @SerialName("total")     val total: RecapTotal,
    @SerialName("version")   val version: String,
    @SerialName("initDate")  val initDate: String
)

@Serializable
data class RecapPeriod(
    @SerialName("users")   val users: Int,
    @SerialName("traffic") val traffic: String
)

@Serializable
data class RecapTotal(
    @SerialName("users")              val users: Int,
    @SerialName("nodes")              val nodes: Int,
    @SerialName("traffic")            val traffic: String,
    @SerialName("nodesRam")           val nodesRam: String,
    @SerialName("nodesCpuCores")      val nodesCpuCores: Int,
    @SerialName("distinctCountries")  val distinctCountries: Int
)
