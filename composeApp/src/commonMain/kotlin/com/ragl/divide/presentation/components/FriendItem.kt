package com.ragl.divide.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun FriendItem(
    headline: String,
    photoUrl: String = "",
    supporting: String = "",
    modifier: Modifier = Modifier,
    hasLeadingContent: Boolean = true,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val supportingContent: @Composable (() -> Unit)? = if (supporting.isNotEmpty()) {
        @Composable {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        null
    }
    Card(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null) {
                if (onClick != null) {
                    onClick()
                }
            },
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = colors
    ) {
        ListItem(
            modifier = if (hasLeadingContent) Modifier.padding(vertical = 4.dp) else Modifier,
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            headlineContent = {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.5f
                    ),
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                    maxLines = 1
                )
            },
            supportingContent = supportingContent,
            leadingContent = if (hasLeadingContent) {
                {
                    NetworkImage(
                        imageUrl = photoUrl,
                        modifier = Modifier.size(52.dp).clip(CircleShape),
                        type = NetworkImageType.PROFILE
                    )
                }
            } else {
                null
            },
            trailingContent = trailingContent
        )
    }
}