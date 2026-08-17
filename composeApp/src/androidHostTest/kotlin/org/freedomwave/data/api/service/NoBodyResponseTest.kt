package org.freedomwave.data.api.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Panel 3.x changed several actions the app calls to answer with no body at all:
 * node restart -> `202 Accepted`, and node reset-traffic plus host bulk enable/disable ->
 * `204 No Content`. On 2.8.x those same endpoints returned `200` with a payload.
 *
 * These pin the contract the services must honour — fire the action without reading a body,
 * then read fresh state from a separate GET — and demonstrate the failure mode being fixed.
 */
class NoBodyResponseTest {

    @Serializable
    private data class Wrapper(val response: Payload)

    @Serializable
    private data class Payload(val uuid: String)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    /** A client answering each successive call with the next status in [statuses]. */
    private fun clientReturning(vararg statuses: HttpStatusCode): Pair<HttpClient, List<String>> {
        val seen = mutableListOf<String>()
        var call = 0
        val engine = MockEngine { request ->
            seen += "${request.method.value} ${request.url.encodedPath}"
            when (val status = statuses.getOrElse(call++) { statuses.last() }) {
                HttpStatusCode.OK -> respond(
                    content = """{"response":{"uuid":"n1"}}""",
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
                else -> respond(content = "", status = status)
            }
        }
        return HttpClient(engine) { install(ContentNegotiation) { json(json) } } to seen
    }

    @Test
    fun `202 Accepted succeeds when the body is not read`() = runBlocking {
        val (client, _) = clientReturning(HttpStatusCode.Accepted)
        val response = client.post("https://panel.example.com/api/nodes/n1/actions/restart")
        assertEquals(HttpStatusCode.Accepted, response.status)
    }

    @Test
    fun `204 No Content succeeds when the body is not read`() = runBlocking {
        val (client, _) = clientReturning(HttpStatusCode.NoContent)
        val response = client.post("https://panel.example.com/api/hosts/bulk/enable")
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `reading a body from an empty 202 throws — the regression being fixed`() {
        val (client, _) = clientReturning(HttpStatusCode.Accepted)
        assertFailsWith<Exception> {
            runBlocking {
                client.post("https://panel.example.com/api/nodes/n1/actions/restart").body<Wrapper>()
            }
        }
        Unit
    }

    @Test
    fun `reading a body from an empty 204 throws — the regression being fixed`() {
        val (client, _) = clientReturning(HttpStatusCode.NoContent)
        assertFailsWith<Exception> {
            runBlocking {
                client.post("https://panel.example.com/api/hosts/bulk/enable").body<Wrapper>()
            }
        }
        Unit
    }

    @Test
    fun `action then refetch issues both calls in order`() = runBlocking {
        // The shape every no-body action now follows: POST the action, then GET fresh state.
        val (client, seen) = clientReturning(HttpStatusCode.NoContent, HttpStatusCode.OK)
        client.post("https://panel.example.com/api/nodes/n1/actions/reset-traffic")
        val refreshed = client.get("https://panel.example.com/api/nodes/n1").body<Wrapper>()

        assertEquals(
            listOf("POST /api/nodes/n1/actions/reset-traffic", "GET /api/nodes/n1"),
            seen,
        )
        assertEquals("n1", refreshed.response.uuid)
    }

    @Test
    fun `a 200 with a payload still parses — panel 2_8 keeps working`() = runBlocking {
        val (client, _) = clientReturning(HttpStatusCode.OK)
        val body = client.post("https://panel.example.com/api/nodes/n1/actions/restart").body<Wrapper>()
        assertTrue(body.response.uuid == "n1")
    }
}
