package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.ApiError
import art.yniyniyni.freedomwave.data.api.service.SubHistoryService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.IpRow

class SubHistoryRepository(
    private val service: SubHistoryService,
    private val prefs: AppPreferences
) {
    /**
     * Fetch subscription request history for [userUuid], aggregate by unique IP,
     * then geo-enrich each unique IP (best-effort, failures leave geo fields null).
     */
    suspend fun getIpRows(userUuid: String): Result<List<IpRow>> = api {
        val records = service.getSubHistory(prefs.getServerUrl(), userUuid).response.records

        // Aggregate by IP
        val grouped = records
            .filter { !it.requestIp.isNullOrBlank() }
            .groupBy { it.requestIp!! }

        // Build base rows (no geo yet)
        val baseRows = grouped.map { (ip, list) ->
            val mostRecent = list.maxByOrNull { it.requestAt }?.requestAt
            IpRow(ip = ip, count = list.size, lastSeenAt = mostRecent,
                city = null, region = null, country = null, countryCode = null,
                isp = null, geoLoaded = false)
        }

        // Geo-enrich unique IPs — parallel but best-effort
        baseRows.map { row ->
            val geo = service.getIpInfo(row.ip)
            if (geo != null && geo.success) {
                row.copy(
                    city        = geo.city,
                    region      = geo.region,
                    country     = geo.country,
                    countryCode = geo.countryCode,
                    isp         = geo.connection?.isp ?: geo.connection?.org,
                    geoLoaded   = true
                )
            } else {
                row.copy(geoLoaded = true)
            }
        }
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.also { result ->
            if (result.exceptionOrNull() is ApiError.Unauthorized) prefs.clearCredentials()
        }
}
