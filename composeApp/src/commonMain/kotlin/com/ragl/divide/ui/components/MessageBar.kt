package com.ragl.divide.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class MessageBarPosition {
    TOP, BOTTOM
}

enum class MessageType {
    SUCCESS, ERROR
}

data class MessageData(
    val message: String,
    val type: MessageType,
    val throwable: Throwable? = null
)

class MessageBarState {
    var currentMessage: MutableState<MessageData?> = mutableStateOf(null)
    
    fun addSuccess(message: String) {
        currentMessage.value = MessageData(message = message, type = MessageType.SUCCESS)
    }
    
    fun addError(throwable: Throwable) {
        currentMessage.value = MessageData(
            message = throwable.message ?: "Error desconocido",
            type = MessageType.ERROR,
            throwable = throwable
        )
    }
    
    fun clearMessage() {
        currentMessage.value = null
    }
}

@Composable
fun rememberMessageBarState(): MessageBarState {
    return remember { MessageBarState() }
}

@Composable
fun MessageBar(
    messageBarState: MessageBarState,
    position: MessageBarPosition = MessageBarPosition.BOTTOM,
    successContainerColor: Color = MaterialTheme.colorScheme.primary,
    successContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    errorContainerColor: Color = MaterialTheme.colorScheme.error,
    errorContentColor: Color = MaterialTheme.colorScheme.onError,
    maxLines: Int = 2,
    autoDismiss: Boolean = true,
    autoDismissDuration: Long = 3000L,
    showDismissButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    val message by messageBarState.currentMessage
    
    message?.let { messageData ->
        val messageVisibilityState = remember { MutableTransitionState(false) }
        
        LaunchedEffect(messageData) {
            messageVisibilityState.targetState = true
            if (autoDismiss) {
                delay(autoDismissDuration)
                messageVisibilityState.targetState = false
                delay(500) // Esperar a que termine la animación
                messageBarState.clearMessage()
            }
        }

        val containerColor = when (messageData.type) {
            MessageType.SUCCESS -> successContainerColor
            MessageType.ERROR -> errorContainerColor
        }
        
        val contentColor = when (messageData.type) {
            MessageType.SUCCESS -> successContentColor
            MessageType.ERROR -> errorContentColor
        }
        
        val icon = when (messageData.type) {
            MessageType.SUCCESS -> Icons.Filled.Check
            MessageType.ERROR -> Icons.Filled.Warning
        }
        
        val enterTransition = when (position) {
            MessageBarPosition.TOP -> expandVertically(
                animationSpec = tween(300),
                expandFrom = Alignment.Top
            ) + fadeIn(animationSpec = tween(300))
            MessageBarPosition.BOTTOM -> expandVertically(
                animationSpec = tween(300),
                expandFrom = Alignment.Bottom
            ) + fadeIn(animationSpec = tween(300))
        }
        
        val exitTransition = when (position) {
            MessageBarPosition.TOP -> shrinkVertically(
                animationSpec = tween(300),
                shrinkTowards = Alignment.Top
            ) + fadeOut(animationSpec = tween(300))
            MessageBarPosition.BOTTOM -> shrinkVertically(
                animationSpec = tween(300),
                shrinkTowards = Alignment.Bottom
            ) + fadeOut(animationSpec = tween(300))
        }
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = when (position) {
                        MessageBarPosition.TOP -> 16.dp
                        MessageBarPosition.BOTTOM -> 16.dp
                    }
                ),
            contentAlignment = when (position) {
                MessageBarPosition.TOP -> Alignment.TopCenter
                MessageBarPosition.BOTTOM -> Alignment.BottomCenter
            }
        ) {
            AnimatedVisibility(
                visibleState = messageVisibilityState,
                enter = enterTransition,
                exit = exitTransition
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (messageData.type == MessageType.SUCCESS) "Éxito" else "Error",
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = messageData.message,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (showDismissButton) {
                            IconButton(
                                onClick = {
                                    messageVisibilityState.targetState = false
                                    // Eliminar el mensaje después de la animación
                                    messageBarState.clearMessage()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = contentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContentWithMessageBar(
    messageBarState: MessageBarState,
    position: MessageBarPosition = MessageBarPosition.BOTTOM,
    successContainerColor: Color = MaterialTheme.colorScheme.primary,
    successContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    errorContainerColor: Color = MaterialTheme.colorScheme.error,
    errorContentColor: Color = MaterialTheme.colorScheme.onError,
    maxLines: Int = 2,
    autoDismiss: Boolean = true,
    autoDismissDuration: Long = 3000L,
    showDismissButton: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        content()
        
        MessageBar(
            messageBarState = messageBarState,
            position = position,
            successContainerColor = successContainerColor,
            successContentColor = successContentColor,
            errorContainerColor = errorContainerColor,
            errorContentColor = errorContentColor,
            maxLines = maxLines,
            autoDismiss = autoDismiss,
            autoDismissDuration = autoDismissDuration,
            showDismissButton = showDismissButton,
            modifier = Modifier.align(
                when (position) {
                    MessageBarPosition.TOP -> Alignment.TopCenter
                    MessageBarPosition.BOTTOM -> Alignment.BottomCenter
                }
            )
        )
    }
} 