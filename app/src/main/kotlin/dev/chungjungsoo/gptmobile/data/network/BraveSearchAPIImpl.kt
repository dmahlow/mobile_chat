package dev.chungjungsoo.gptmobile.data.network

import dev.chungjungsoo.gptmobile.data.dto.brave.BraveSearchResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BraveSearchAPIImpl(
    private val networkClient: NetworkClient
) : BraveSearchAPI {

    private var token: String? = null

    override fun setToken(token: String?) {
        this.token = token
    }

    override suspend fun search(query: String, count: Int): BraveSearchResponse = withContext(Dispatchers.IO) {
        val currentToken = token ?: throw IllegalStateException("Brave Search API key not configured")

        val response = networkClient().get("https://api.search.brave.com/res/v1/web/search") {
            header("X-Subscription-Token", currentToken)
            header("Accept", "application/json")
            parameter("q", query)
            parameter("count", count)
        }

        if (!response.status.isSuccess()) {
            throw RuntimeException("Brave Search failed: HTTP ${response.status.value}")
        }

        val body = response.body<String>()
        NetworkClient.json.decodeFromString<BraveSearchResponse>(body)
    }
}
