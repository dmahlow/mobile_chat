package dev.chungjungsoo.gptmobile.presentation.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.presentation.theme.GPTMobileTheme
import java.io.File

@Composable
fun UserChatBubble(
    modifier: Modifier = Modifier,
    text: String,
    files: List<String> = emptyList(),
    onLongPress: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        Surface(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress.invoke() })
                },
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            ChatMarkdown(
                content = text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
        MessageFileThumbnailRow(
            files = files,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun OpponentChatBubble(
    modifier: Modifier = Modifier,
    canRetry: Boolean,
    isLoading: Boolean,
    isError: Boolean = false,
    text: String,
    thoughts: String = "",
    attachments: List<String> = emptyList(),
    contentIdentity: Any = text,
    canEdit: Boolean = false,
    revisionIndexLabel: String? = null,
    canShowPreviousRevision: Boolean = false,
    canShowNextRevision: Boolean = false,
    onCopyClick: () -> Unit = {},
    onSelectClick: () -> Unit = {},
    onRetryClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onShowPreviousRevision: () -> Unit = {},
    onShowNextRevision: () -> Unit = {}
) {
    val toolsUsed = parseToolMarkers(thoughts)
    val cleanThoughts = stripToolMarkers(thoughts)
    val isThinking = isLoading && cleanThoughts.isNotBlank() && text.isBlank()

    Column(modifier = modifier) {
        ToolUseIndicator(
            toolNames = toolsUsed,
            isActive = isLoading
        )

        if (cleanThoughts.isNotBlank()) {
            ThinkingBlock(
                modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
                thoughts = cleanThoughts,
                contentIdentity = contentIdentity,
                isLoading = isThinking
            )
        }

        Column(modifier = Modifier.padding(start = 4.dp, end = 12.dp)) {
            StreamingChatMarkdown(
                content = text,
                isStreaming = isLoading,
                contentIdentity = contentIdentity,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            MessageFileThumbnailRow(
                files = attachments,
                usePrimaryColors = false,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (!isLoading) {
            var actionsExpanded by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { actionsExpanded = !actionsExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = stringResource(R.string.options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (actionsExpanded) {
                    if (!isError) {
                        CopyTextIcon(onCopyClick)
                        SelectTextIcon(onSelectClick)
                        if (canEdit) {
                            EditTextIcon(onEditClick)
                        }
                    }
                    if (canRetry) {
                        RetryIcon(onRetryClick)
                    }
                }
            }
            if (actionsExpanded) {
                revisionIndexLabel?.let { label ->
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            enabled = canShowPreviousRevision,
                            onClick = onShowPreviousRevision
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = stringResource(R.string.previous_revision)
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            enabled = canShowNextRevision,
                            onClick = onShowNextRevision
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.next_revision)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GPTMobileIcon(loading: Boolean) {
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        } else {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_chat),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun PlatformButton(
    isLoading: Boolean,
    name: String,
    selected: Boolean,
    onPlatformClick: () -> Unit
) {
    val buttonContent: @Composable RowScope.() -> Unit = {
        Spacer(modifier = Modifier.width(12.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        if (isLoading) Spacer(modifier = Modifier.width(4.dp))
    }

    TextButton(
        modifier = Modifier.widthIn(max = 160.dp),
        onClick = onPlatformClick,
        colors = if (selected) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.textButtonColors(),
        content = buttonContent
    )
}

@Composable
private fun CopyTextIcon(onCopyClick: () -> Unit) {
    IconButton(onClick = onCopyClick) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_copy),
            contentDescription = stringResource(R.string.copy_text)
        )
    }
}

@Composable
private fun SelectTextIcon(onSelectClick: () -> Unit) {
    IconButton(onClick = onSelectClick) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_select),
            contentDescription = stringResource(R.string.select_text)
        )
    }
}

@Composable
private fun RetryIcon(onRetryClick: () -> Unit) {
    IconButton(onClick = onRetryClick) {
        Icon(
            Icons.Rounded.Refresh,
            contentDescription = stringResource(R.string.retry)
        )
    }
}

@Composable
private fun EditTextIcon(onEditClick: () -> Unit) {
    IconButton(onClick = onEditClick) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = stringResource(R.string.edit)
        )
    }
}

@Preview
@Composable
fun UserChatBubblePreview() {
    val sampleText = """
        How can I print hello world
        in Python?
    """.trimIndent()
    GPTMobileTheme {
        UserChatBubble(text = sampleText, files = emptyList(), onLongPress = {})
    }
}

@Preview
@Composable
fun OpponentChatBubblePreview() {
    val sampleText = """
        # Demo
    
        Emphasis, aka italics, with *asterisks* or _underscores_. Strong emphasis, aka bold, with **asterisks** or __underscores__. Combined emphasis with **asterisks and _underscores_**. [Links with two blocks, text in square-brackets, destination is in parentheses.](https://www.example.com). Inline `code` has `back-ticks around` it.
    
        1. First ordered list item
        2. Another item
            * Unordered sub-list.
        3. And another item.
            You can have properly indented paragraphs within list items. Notice the blank line above, and the leading spaces (at least one, but we'll use three here to also align the raw Markdown).
    
        * Unordered list can use asterisks
        - Or minuses
        + Or pluses
    """.trimIndent()
    GPTMobileTheme {
        OpponentChatBubble(
            text = sampleText,
            canRetry = true,
            isLoading = false,
            revisionIndexLabel = "Revision 1/1",
            onCopyClick = {},
            onRetryClick = {}
        )
    }
}

@Composable
internal fun MessageFileThumbnailRow(
    files: List<String>,
    modifier: Modifier = Modifier,
    usePrimaryColors: Boolean = true
) {
    // Filter out empty strings and check if we have valid files
    val validFiles = files.filter { it.isNotEmpty() && it.isNotBlank() }

    if (validFiles.isEmpty()) {
        return
    }

    Row(
        modifier = modifier
            .wrapContentHeight()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        validFiles.forEach { filePath ->
            MessageFileThumbnail(
                filePath = filePath,
                usePrimaryColors = usePrimaryColors
            )
        }
    }
}

@Composable
private fun MessageFileThumbnail(
    filePath: String,
    usePrimaryColors: Boolean
) {
    val file = File(filePath)
    val isImage = isImageFile(file.extension)
    val containerColor = if (usePrimaryColors) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (usePrimaryColors) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.width(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(containerColor)
        ) {
            if (isImage) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_image),
                    contentDescription = file.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    tint = contentColor
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_file),
                    contentDescription = file.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    tint = contentColor
                )
            }
        }

        Text(
            text = file.name,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .padding(top = 4.dp)
                .width(56.dp)
        )
    }
}

private fun isImageFile(extension: String?): Boolean {
    val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    return extension?.lowercase() in imageExtensions
}

private val toolMarkerRegex = Regex("""\[tools:([^\]]+)]""")

internal fun parseToolMarkers(thoughts: String): List<String> {
    val match = toolMarkerRegex.find(thoughts) ?: return emptyList()
    return match.groupValues[1].split(",").filter { it.isNotBlank() }
}

internal fun stripToolMarkers(thoughts: String): String =
    thoughts.replace(Regex("""\[tools:[^\]]+]\n?"""), "")
