package com.ragl.divide.ui.screens.friends

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.ui.components.TitleRow
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.addFriends.AddFriendsScreen
import com.ragl.divide.ui.utils.FriendItem
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.accept
import dividemultiplatform.composeapp.generated.resources.add_friends
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.friend_requests_received
import dividemultiplatform.composeapp.generated.resources.friend_requests_sent
import dividemultiplatform.composeapp.generated.resources.friends
import dividemultiplatform.composeapp.generated.resources.reject
import dividemultiplatform.composeapp.generated.resources.you_have_no_friends
import dividemultiplatform.composeapp.generated.resources.your_friends
import org.jetbrains.compose.resources.stringResource

class FriendsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        val state by userViewModel.state.collectAsState()

        val friends = remember(state.friends) {
            state.friends.values.toList().sortedBy { it.name.lowercase() }
        }
        val friendRequests = state.friendRequestsReceived.values.toList()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(Res.string.friends))
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.back)
                            )
                        }
                    },
                )
            }
        ) { pv ->
            FriendsBody(
                friends = friends,
                friendRequestsReceived = friendRequests,
                friendRequestsSent = state.friendRequestsSent.values.toList(),
                onAddFriendClick = {
                    navigator.push(AddFriendsScreen())
                },
                onAcceptFriendRequest = userViewModel::acceptFriendRequest,
                onRejectFriendRequest = userViewModel::rejectFriendRequest,
                onCancelFriendRequest = userViewModel::cancelFriendRequest,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv)
            )
        }
    }

}

@Composable
fun FriendsBody(
    friends: List<UserInfo>,
    friendRequestsReceived: List<UserInfo>,
    friendRequestsSent: List<UserInfo>,
    onAddFriendClick: () -> Unit = {},
    onAcceptFriendRequest: (UserInfo) -> Unit = {},
    onRejectFriendRequest: (UserInfo) -> Unit = {},
    onCancelFriendRequest: (UserInfo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
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
        item {
            Spacer(modifier = Modifier.height(16.dp))
            TitleRow(
                Res.string.add_friends,
                Res.string.your_friends,
                onAddClick = onAddFriendClick,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
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

}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}