package dev.chungjungsoo.gptmobile.presentation.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StreamingChatMarkdown(
    content: String,
    isStreaming: Boolean,
    contentIdentity: Any = content,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val fadeHeight = 48.dp

    Box(
        modifier = modifier.then(
            if (isStreaming && content.isNotBlank()) {
                Modifier.drawWithContent {
                    drawContent()
                    val fadeHeightPx = fadeHeight.toPx()
                    if (size.height > fadeHeightPx) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, backgroundColor),
                                startY = size.height - fadeHeightPx,
                                endY = size.height
                            ),
                            topLeft = Offset(0f, size.height - fadeHeightPx),
                            size = Size(size.width, fadeHeightPx)
                        )
                    }
                }
            } else {
                Modifier
            }
        )
    ) {
        ChatMarkdown(
            content = content,
            contentIdentity = contentIdentity
        )
    }
}
