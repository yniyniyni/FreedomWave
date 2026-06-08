package art.yniyniyni.freedomwave.domain.model

/** One unique IP address aggregated from subscription request history, enriched with geo data. */
data class IpRow(
    val ip: String,
    val count: Int,
    val lastSeenAt: String?,   // ISO-8601 string of the most-recent request
    val city: String?,
    val region: String?,
    val country: String?,
    val countryCode: String?,
    val isp: String?,
    val geoLoaded: Boolean = false
)
