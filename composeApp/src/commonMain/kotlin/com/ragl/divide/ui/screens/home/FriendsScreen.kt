package com.ragl.divide.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.ui.utils.ActionButton
import com.ragl.divide.ui.utils.FriendItem
import com.ragl.divide.ui.utils.Header
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.UserPlus
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.accept
import dividemultiplatform.composeapp.generated.resources.add_friends
import dividemultiplatform.composeapp.generated.resources.bar_item_friends_text
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.friend_requests_received
import dividemultiplatform.composeapp.generated.resources.friend_requests_sent
import dividemultiplatform.composeapp.generated.resources.reject
import dividemultiplatform.composeapp.generated.resources.you_have_no_friends
import dividemultiplatform.composeapp.generated.resources.your_friends
import org.jetbrains.compose.resources.stringResource

@Composable
fun FriendsBody(
    friends: List<UserInfo>,
    friendRequestsReceived: List<UserInfo>,
    friendRequestsSent: List<UserInfo>,
    onAddFriendClick: () -> Unit = {},
    onAcceptFriendRequest: (UserInfo) -> Unit = {},
    onRejectFriendRequest: (UserInfo) -> Unit = {},
    onCancelFriendRequest: (UserInfo) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Header(
                    title = stringResource(Res.string.bar_item_friends_text)
                )
            }

            // Sección de solicitudes recibidas
            if (friendRequestsReceived.isNotEmpty() || friendRequestsSent.isNotEmpty()) {
                if (friendRequestsReceived.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = stringResource(Res.string.friend_requests_received))
                    }

                    items(friendRequestsReceived, key = { it.uuid }) { request ->
                        FriendItem(
                            headline = request.name,
                            photoUrl = request.photoUrl,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { onAcceptFriendRequest(request) }) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = stringResource(Res.string.accept),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { onRejectFriendRequest(request) }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(Res.string.reject),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
                if (friendRequestsSent.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = stringResource(Res.string.friend_requests_sent))
                    }

                    items(friendRequestsSent, key = { it.uuid }) { request ->
                        FriendItem(
                            headline = request.name,
                            photoUrl = request.photoUrl,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            trailingContent = {
                                TextButton(
                                    onClick = { onCancelFriendRequest(request) }
                                ) {
                                    Text(
                                        text = stringResource(Res.string.cancel),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Sección de amigos
            if (friends.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.you_have_no_friends),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = stringResource(Res.string.your_friends))
                }
                items(friends, key = { it.uuid }) { friend ->
                    FriendItem(
                        headline = friend.name,
                        photoUrl = friend.photoUrl,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Espacio al final para que el botón de acción no tape contenido
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        ActionButton(
            onClick = onAddFriendClick,
            text = stringResource(Res.string.add_friends),
            icon = FontAwesomeIcons.Solid.UserPlus,
            contentDescription = stringResource(Res.string.add_friends),
            modifier = Modifier.padding(16.dp)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}