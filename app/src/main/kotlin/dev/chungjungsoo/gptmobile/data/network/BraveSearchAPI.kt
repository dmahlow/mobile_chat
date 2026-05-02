package dev.chungjungsoo.gptmobile.data.network

import dev.chungjungsoo.gptmobile.data.dto.brave.BraveSearchResponse

interface BraveSearchAPI {
    fun setToken(token: String?)
    suspend fun search(query: String, count: Int = 5): BraveSearchResponse
}
