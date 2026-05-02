package dev.chungjungsoo.gptmobile.data.dto.anthropic.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentBlock(

    @SerialName("type")
    val type: ContentBlockType,

    @SerialName("text")
    val text: String? = null,

    @SerialName("thinking")
    val thinking: String? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("input")
    val input: kotlinx.serialization.json.JsonObject? = null,

    @SerialName("partial_json")
    val partialJson: String? = null
)
