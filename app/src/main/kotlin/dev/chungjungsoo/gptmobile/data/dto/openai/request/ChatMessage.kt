package dev.chungjungsoo.gptmobile.data.dto.openai.request

import dev.chungjungsoo.gptmobile.data.dto.openai.common.MessageContent
import dev.chungjungsoo.gptmobile.data.dto.openai.common.Role
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ChatMessage(
    @SerialName("role")
    val role: Role,

    @SerialName("content")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val content: List<MessageContent>? = null,

    @SerialName("tool_call_id")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val toolCallId: String? = null,

    @SerialName("tool_calls")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val toolCalls: List<ChatMessageToolCall>? = null
)

@Serializable
data class ChatMessageToolCall(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String = "function",

    @SerialName("function")
    val function: ChatMessageToolCallFunction
)

@Serializable
data class ChatMessageToolCallFunction(
    @SerialName("name")
    val name: String,

    @SerialName("arguments")
    val arguments: String
)
