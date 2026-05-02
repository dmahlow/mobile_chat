package dev.chungjungsoo.gptmobile.presentation.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

private const val FADE_WORD_COUNT = 8
private const val MIN_OPACITY = 0.15f

/**
 * A wrapper around ChatMarkdown that applies a per-word fade-in effect on the
 * trailing words while streaming is active. When streaming is finished, it
 * delegates entirely to ChatMarkdown for normal rendering.
 */
@Composable
fun StreamingChatMarkdown(
    content: String,
    isStreaming: Boolean,
    contentIdentity: Any = content,
    modifier: Modifier = Modifier
) {
    if (!isStreaming || content.isBlank()) {
        ChatMarkdown(
            content = content,
            contentIdentity = contentIdentity,
            modifier = modifier
        )
        return
    }

    // Split content into a stable prefix (rendered with full markdown) and
    // a fading suffix (rendered as plain annotated text with per-word opacity).
    val (stablePrefix, fadingSuffix) = remember(content) {
        splitForFade(content, FADE_WORD_COUNT)
    }

    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        if (stablePrefix.isNotEmpty()) {
            ChatMarkdown(
                content = stablePrefix,
                contentIdentity = "$contentIdentity:stable"
            )
        }

        if (fadingSuffix.isNotEmpty()) {
            val words = remember(fadingSuffix) { splitIntoWordsWithSpaces(fadingSuffix) }
            val annotatedText = remember(words, textColor) {
                buildAnnotatedString {
                    val wordCount = words.size
                    words.forEachIndexed { index, word ->
                        // index 0 is the oldest word in the fade zone (highest opacity)
                        // index wordCount-1 is the newest (lowest opacity)
                        val progress = if (wordCount <= 1) {
                            0f
                        } else {
                            index.toFloat() / (wordCount - 1).toFloat()
                        }
                        val alpha = MIN_OPACITY + (1f - MIN_OPACITY) * (1f - progress)
                        pushStyle(SpanStyle(color = textColor.copy(alpha = alpha)))
                        append(word)
                        pop()
                    }
                }
            }

            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Splits content so that the last [fadeWords] words form the suffix, and
 * everything before them (cut at a word boundary) is the prefix.
 * Returns a Pair of (stablePrefix, fadingSuffix).
 */
internal fun splitForFade(content: String, fadeWords: Int): Pair<String, String> {
    // Find word boundaries from the end
    val trimmed = content.trimEnd()
    if (trimmed.isEmpty()) return "" to ""

    // Walk backwards to find the start of the fade zone
    var wordsSeen = 0
    var splitIndex = trimmed.length

    var i = trimmed.length - 1
    var inWord = false

    while (i >= 0) {
        val isWordChar = !trimmed[i].isWhitespace()
        if (isWordChar && !inWord) {
            // Entering a word from the right
            wordsSeen++
            if (wordsSeen > fadeWords) {
                // The split point is right after this word ends (to the right)
                splitIndex = i + 1
                // Skip forward past any whitespace to get clean boundary
                while (splitIndex < trimmed.length && trimmed[splitIndex].isWhitespace()) {
                    splitIndex++
                }
                break
            }
            inWord = true
        } else if (!isWordChar && inWord) {
            inWord = false
        }
        i--
    }

    if (wordsSeen <= fadeWords) {
        // Entire content fits in the fade zone
        return "" to trimmed
    }

    val prefix = content.substring(0, splitIndex)
    val suffix = content.substring(splitIndex)
    return prefix to suffix
}

/**
 * Splits text into tokens preserving whitespace between words.
 * Each "token" is either a word or a whitespace run attached to the following word.
 * For fade purposes, we group as: [space+word] units so spacing stays natural.
 */
internal fun splitIntoWordsWithSpaces(text: String): List<String> {
    if (text.isEmpty()) return emptyList()
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inWord = false

    for (ch in text) {
        if (ch.isWhitespace()) {
            if (inWord && current.isNotEmpty()) {
                result.add(current.toString())
                current.clear()
            }
            current.append(ch)
            inWord = false
        } else {
            inWord = true
            current.append(ch)
        }
    }
    if (current.isNotEmpty()) {
        result.add(current.toString())
    }
    return result
}
