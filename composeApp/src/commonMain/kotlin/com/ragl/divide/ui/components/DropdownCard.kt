package com.ragl.divide.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ragl.divide.ui.utils.WindowWidthSizeClass
import com.ragl.divide.ui.utils.getWindowWidthSizeClass

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CollapsedDropdownCard(
    itemContent: @Composable () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    contentKey: String,
    onClick: () -> Unit,
    showMenu: Boolean = true,
    modifier: Modifier = Modifier
) {
    with(sharedTransitionScope) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = contentKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(tween(100)),
                    exit = fadeOut(tween(300)),
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
                .clip(CardDefaults.shape)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    itemContent()
                }
                if (showMenu)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun <T> ExpandedDropdownCard(
    items: List<T>,
    selectedItem: T?,
    title: String,
    itemContent: @Composable (T, Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    contentKey: String,
    onDismiss: () -> Unit,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val size = getWindowWidthSizeClass()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onDismiss() }
            ),
        contentAlignment = Alignment.Center
    ) {
        with(sharedTransitionScope) {
            Card(
                modifier = modifier
                    .let {
                        if (size == WindowWidthSizeClass.Compact) {
                            it.fillMaxWidth()
                        } else {
                            it.width(400.dp)
                        }
                    }
                    .padding(12.dp)
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null,
                        onClick = {}
                    )
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = contentKey),
                        animatedVisibilityScope = animatedVisibilityScope,
                        enter = fadeIn(tween(100)),
                        exit = fadeOut(tween(300)),
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyColumn {
                        items(items) { item ->
                            val isSelected = item == selectedItem
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        onItemClick(item)
                                        onDismiss()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    itemContent(item, isSelected)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 