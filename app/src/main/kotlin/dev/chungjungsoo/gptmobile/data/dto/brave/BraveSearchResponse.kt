package dev.chungjungsoo.gptmobile.data.dto.brave

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BraveSearchResponse(
    @SerialName("web")
    val web: BraveWebResults? = null
)

@Serializable
data class BraveWebResults(
    @SerialName("results")
    val results: List<BraveWebResult> = emptyList()
)

@Serializable
data class BraveWebResult(
    @SerialName("title")
    val title: String,

    @SerialName("url")
    val url: String,

    @SerialName("description")
    val description: String = ""
)
