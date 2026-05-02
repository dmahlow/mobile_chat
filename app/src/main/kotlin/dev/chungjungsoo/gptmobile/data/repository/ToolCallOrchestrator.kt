package dev.chungjungsoo.gptmobile.data.repository

import dev.chungjungsoo.gptmobile.data.dto.ApiState
import dev.chungjungsoo.gptmobile.data.dto.brave.BraveSearchResponse
import dev.chungjungsoo.gptmobile.data.dto.toolcalling.DateTimeTool
import dev.chungjungsoo.gptmobile.data.dto.toolcalling.SearchSource
import dev.chungjungsoo.gptmobile.data.dto.toolcalling.ToolCallRequest
import dev.chungjungsoo.gptmobile.data.dto.toolcalling.ToolCallResult
import dev.chungjungsoo.gptmobile.data.dto.toolcalling.WebSearchTool
import dev.chungjungsoo.gptmobile.data.network.BraveSearchAPI
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ToolCallOrchestrator(
    private val braveSearchAPI: BraveSearchAPI,
    private val settingRepository: SettingRepository
) {
    private val maxIterations = 3

    fun orchestrate(
        initialFlow: Flow<ApiState>,
        continueWithToolResults: suspend (List<ToolCallResult>) -> Flow<ApiState>
    ): Flow<ApiState> = flow {
        var currentFlow = initialFlow
        var iterations = 0

        while (iterations < maxIterations) {
            iterations++
            val collectedToolCalls = mutableListOf<ToolCallRequest>()
            val textBuffer = StringBuilder()
            var hadError = false

            currentFlow.collect { state ->
                when (state) {
                    is ApiState.Done -> {}
                    is ApiState.Error -> {
                        hadError = true
                        emit(state)
                    }
                    else -> emit(state)
                }

                if (state is ApiState.Success) {
                    textBuffer.append(state.textChunk)
                }
            }

            if (hadError || collectedToolCalls.isEmpty()) {
                emit(ApiState.Done)
                return@flow
            }

            val toolResults = executeToolCalls(collectedToolCalls)
            if (toolResults.isEmpty()) {
                emit(ApiState.Done)
                return@flow
            }

            currentFlow = continueWithToolResults(toolResults)
        }

        emit(ApiState.Done)
    }

    fun orchestrateWithToolDetection(
        initialFlow: Flow<ApiState>,
        onToolCallDetected: (List<ToolCallRequest>) -> Unit,
        continueWithToolResults: suspend (List<ToolCallResult>) -> Flow<ApiState>
    ): Flow<ApiState> = flow {
        var currentFlow = initialFlow
        var iterations = 0

        while (iterations < maxIterations) {
            iterations++
            val accumulator = ToolCallAccumulator()
            var hadError = false

            currentFlow.collect { state ->
                when (state) {
                    is ApiState.Done -> {}
                    is ApiState.Error -> {
                        hadError = true
                        emit(state)
                    }
                    is ApiState.ToolCallChunk -> {
                        accumulator.append(state)
                    }
                    else -> emit(state)
                }
            }

            if (hadError) {
                emit(ApiState.Done)
                return@flow
            }

            val toolCalls = accumulator.build()
            if (toolCalls.isEmpty()) {
                emit(ApiState.Done)
                return@flow
            }

            onToolCallDetected(toolCalls)

            val toolResults = executeToolCalls(toolCalls)
            if (toolResults.isEmpty()) {
                emit(ApiState.Done)
                return@flow
            }

            currentFlow = continueWithToolResults(toolResults)
        }

        emit(ApiState.Done)
    }

    private suspend fun executeToolCalls(toolCalls: List<ToolCallRequest>): List<ToolCallResult> {
        val results = mutableListOf<ToolCallResult>()

        for (toolCall in toolCalls) {
            when (toolCall.name) {
                WebSearchTool.NAME -> {
                    val query = extractSearchQuery(toolCall.arguments) ?: continue
                    try {
                        val token = settingRepository.getBraveSearchToken() ?: continue
                        braveSearchAPI.setToken(token)
                        val response = braveSearchAPI.search(query)
                        val resultText = formatSearchResults(response)
                        results.add(ToolCallResult(toolCallId = toolCall.id, name = toolCall.name, result = resultText))
                    } catch (_: Exception) {
                        results.add(
                            ToolCallResult(
                                toolCallId = toolCall.id,
                                name = toolCall.name,
                                result = "Search failed. Please answer based on your existing knowledge."
                            )
                        )
                    }
                }
                DateTimeTool.NAME -> {
                    results.add(ToolCallResult(toolCallId = toolCall.id, name = toolCall.name, result = getCurrentDateTime()))
                }
            }
        }

        return results
    }

    private fun getCurrentDateTime(): String {
        val now = ZonedDateTime.now()
        val dayOfWeek = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val timezone = now.zone.id
        val utcOffset = now.offset.id
        return """{"date": "$date", "time": "$time", "day_of_week": "$dayOfWeek", "timezone": "$timezone", "utc_offset": "$utcOffset"}"""
    }

    private fun extractSearchQuery(arguments: String): String? = try {
        val json = Json.parseToJsonElement(arguments)
        val obj = json as? kotlinx.serialization.json.JsonObject
        val queryElement = obj?.get(WebSearchTool.PARAM_QUERY)
        (queryElement as? kotlinx.serialization.json.JsonPrimitive)?.content
    } catch (_: Exception) {
        null
    }

    private fun formatSearchResults(response: BraveSearchResponse): String {
        val results = response.web?.results?.take(5) ?: return "No results found."
        val sources = results.map { result ->
            SearchSource(title = result.title, url = result.url, snippet = result.description)
        }
        return Json.encodeToString(sources)
    }
}

class ToolCallAccumulator {
    private val calls = mutableMapOf<Int, PendingToolCall>()

    fun append(chunk: ApiState.ToolCallChunk) {
        val pending = calls.getOrPut(chunk.index) { PendingToolCall() }
        chunk.id?.let { pending.id = it }
        chunk.name?.let { pending.name = it }
        chunk.argumentsChunk?.let { pending.arguments.append(it) }
    }

    fun build(): List<ToolCallRequest> = calls.values
        .filter { it.id != null && it.name != null }
        .map { ToolCallRequest(id = it.id!!, name = it.name!!, arguments = it.arguments.toString()) }

    private class PendingToolCall {
        var id: String? = null
        var name: String? = null
        val arguments = StringBuilder()
    }
}
