package dev.chungjungsoo.gptmobile.presentation.ui.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dev.chungjungsoo.gptmobile.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToolUseIndicator(
    modifier: Modifier = Modifier,
    toolNames: List<String>,
    isActive: Boolean = false
) {
    if (toolNames.isEmpty()) return

    FlowRow(
        modifier = modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        toolNames.forEach { name ->
            ToolChip(name = formatToolName(name), isActive = isActive)
        }
    }
}

@Composable
private fun ToolChip(name: String, isActive: Boolean) {
    val pulseAlpha = if (isActive) {
        val transition = rememberInfiniteTransition(label = "tool_pulse")
        val alpha by transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        alpha
    } else {
        1f
    }

    Row(
        modifier = Modifier
            .alpha(pulseAlpha)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_select),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

private fun formatToolName(name: String): String = when (name) {
    "web_search" -> "Web Search"
    "get_current_datetime" -> "Date/Time"
    else -> name.replace("_", " ").replaceFirstChar { it.uppercase() }
}
