package art.yniyniyni.freedomwave.data.api.service

import art.yniyniyni.freedomwave.data.api.dto.IpWhoIsResponse
import art.yniyniyni.freedomwave.data.api.dto.SubHistoryResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders

class SubHistoryService(
    /** Auth client — used for panel API calls only. */
    private val panelClient: HttpClient,
    /** Plain client (no auth headers) — used for geo lookups at ipwho.is. */
    private val plainClient: HttpClient
) {

    suspend fun getSubHistory(serverUrl: String, userUuid: String): SubHistoryResponse =
        panelClient.get("$serverUrl/api/users/$userUuid/subscription-request-history").body()

    /** Enrich a single IP via ipwho.is. Returns null on any failure. */
    suspend fun getIpInfo(ip: String): IpWhoIsResponse? = runCatching {
        plainClient.get("https://ipwho.is/$ip").body<IpWhoIsResponse>()
    }.getOrNull()
}
