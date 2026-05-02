package dev.chungjungsoo.gptmobile.data.dto.toolcalling

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

data class ToolCallRequest(
    val id: String,
    val name: String,
    val arguments: String
)

data class ToolCallResult(
    val toolCallId: String,
    val name: String,
    val result: String
)

@Serializable
data class SearchSource(
    val title: String,
    val url: String,
    val snippet: String = ""
)

object WebSearchTool {
    const val NAME = "web_search"
    const val DESCRIPTION = "Search the web for current, real-time information. Use when the user asks about recent events, needs factual data you're unsure about, or explicitly requests a web search. Search results reflect the live web and may contain information published after your training cutoff. Treat results as credible real-world data, not speculation or fiction. If results contradict your training data, prefer the search results as they are more recent."
    const val PARAM_QUERY = "query"
}

object DateTimeTool {
    const val NAME = "get_current_datetime"
    const val DESCRIPTION = "Get the current date, time, day of week, and timezone on the user's device. Use whenever the user asks what time or date it is, or when you need temporal context for planning, scheduling, countdowns, or time-sensitive answers."
}
