package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.service.SubHistoryService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.IpRow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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

        // Geo lookup is opt-in: when off, never send client IPs to the third-party ipwho.is.
        if (!prefs.getGeoLookupEnabled()) return@api baseRows

        // Geo-enrich unique IPs — best-effort, parallelized
        coroutineScope {
            baseRows.map { row ->
                async {
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
            }.awaitAll()
        }
    }

    private suspend fun <T> api(block: suspend () -> T): Result<T> =
        runCatching { block() }.also { it.clearOnUnauthorized(prefs) }
}
