package com.ragl.divide.presentation.screens.friends

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.services.UserService
import com.ragl.divide.presentation.components.AdaptiveFAB
import com.ragl.divide.presentation.components.FriendItem
import com.ragl.divide.presentation.screens.addFriends.AddFriendsScreen
import com.ragl.divide.presentation.screens.main.Header
import com.ragl.divide.presentation.state.LocalUserState
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.accept
import dividemultiplatform.composeapp.generated.resources.add_friends
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.bar_item_2_text
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.friend_requests_received
import dividemultiplatform.composeapp.generated.resources.friend_requests_sent
import dividemultiplatform.composeapp.generated.resources.friends
import dividemultiplatform.composeapp.generated.resources.reject
import dividemultiplatform.composeapp.generated.resources.remove_friend
import dividemultiplatform.composeapp.generated.resources.remove_friend_confirm
import dividemultiplatform.composeapp.generated.resources.you_have_no_friends
import dividemultiplatform.composeapp.generated.resources.your_friends
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class FriendsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userService: UserService = koinInject()

        val userState = LocalUserState.current

        val friends = remember(userState.friends) {
            userState.friends.values.toList().sortedBy { it.name.lowercase() }
        }
        val friendRequests = userState.friendRequestsReceived.values.toList()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(Res.string.friends))
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back)
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                AdaptiveFAB(
                    text = stringResource(Res.string.add_friends),
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_friends),
                    onClick = {
                        navigator.push(AddFriendsScreen())
                    }
                )
            }
        ) { pv ->
            FriendsBody(
                friends = friends,
                friendRequestsReceived = friendRequests,
                friendRequestsSent = userState.friendRequestsSent.values.toList(),
                onAcceptFriendRequest = userService::acceptFriendRequest,
                onRejectFriendRequest = userService::rejectFriendRequest,
                onCancelFriendRequest = userService::cancelFriendRequest,
                onRemoveFriend = userService::removeFriend,
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
    onAcceptFriendRequest: (UserInfo) -> Unit = {},
    onRejectFriendRequest: (UserInfo) -> Unit = {},
    onCancelFriendRequest: (UserInfo) -> Unit = {},
    onRemoveFriend: (UserInfo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var friendToRemove by remember { mutableStateOf<UserInfo?>(null) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    LazyColumn(modifier = modifier) {
        item {
            Header(
                title = stringResource(Res.string.bar_item_2_text),
            )
        }
        if (friendRequestsReceived.isNotEmpty() || friendRequestsSent.isNotEmpty()) {
            if (friendRequestsReceived.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(Res.string.friend_requests_received))
                }
                itemsIndexed(friendRequestsReceived) { i, request ->
                    FriendItem(
                        headline = request.name,
                        photoUrl = request.photoUrl,
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
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp).padding(bottom = 2.dp)
                            .clip(
                                if (friendRequestsReceived.size == 1)
                                    RoundedCornerShape(16.dp)
                                else
                                    if (i == 0) RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 2.dp,
                                        bottomStart = 2.dp
                                    ) else {
                                        if (i == friendRequestsReceived.lastIndex)
                                            RoundedCornerShape(
                                                topStart = 2.dp,
                                                topEnd = 2.dp,
                                                bottomEnd = 16.dp,
                                                bottomStart = 16.dp
                                            )
                                        else RoundedCornerShape(2.dp)
                                    }
                            )
                    )
                }
                item{
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            if (friendRequestsSent.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(Res.string.friend_requests_sent))
                }
                itemsIndexed(friendRequestsSent) { i, request ->
                    FriendItem(
                        headline = request.name,
                        photoUrl = request.photoUrl,
                        trailingContent = {
                            TextButton(
                                onClick = { onCancelFriendRequest(request) }
                            ) {
                                Text(
                                    text = stringResource(Res.string.cancel),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp).padding(bottom = 2.dp)
                            .clip(
                                if (friendRequestsSent.size == 1)
                                    RoundedCornerShape(16.dp)
                                else
                                    if (i == 0) RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 2.dp,
                                        bottomStart = 2.dp
                                    ) else {
                                        if (i == friendRequestsSent.lastIndex)
                                            RoundedCornerShape(
                                                topStart = 2.dp,
                                                topEnd = 2.dp,
                                                bottomEnd = 16.dp,
                                                bottomStart = 16.dp
                                            )
                                        else RoundedCornerShape(2.dp)
                                    }
                            )
                    )
                }
                item{
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        item {
            SectionHeader(title = stringResource(Res.string.your_friends))
        }
        if (friends.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.you_have_no_friends),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
            }
        } else {
            itemsIndexed(friends) { i, friend ->
                var showMenu by remember { mutableStateOf(false) }
                FriendItem(
                    headline = friend.name,
                    photoUrl = friend.photoUrl,
                    trailingContent = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.remove_friend)) },
                                onClick = {
                                    showMenu = false
                                    friendToRemove = friend
                                    showRemoveDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp).padding(bottom = 2.dp)
                        .clip(
                            if (friends.size == 1)
                                RoundedCornerShape(16.dp)
                            else
                                if (i == 0) RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomEnd = 2.dp,
                                    bottomStart = 2.dp
                                ) else {
                                    if (i == friends.lastIndex)
                                        RoundedCornerShape(
                                            topStart = 2.dp,
                                            topEnd = 2.dp,
                                            bottomEnd = 16.dp,
                                            bottomStart = 16.dp
                                        )
                                    else RoundedCornerShape(2.dp)
                                }
                        )
                )
            }
        }

        // Espacio al final para que el botón de acción no tape contenido
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Diálogo de confirmación para eliminar amigo
    if (showRemoveDialog && friendToRemove != null) {
        AlertDialog(
            onDismissRequest = {
                showRemoveDialog = false
                friendToRemove = null
            },
            title = { Text(stringResource(Res.string.remove_friend)) },
            text = {
                Text(stringResource(Res.string.remove_friend_confirm, friendToRemove!!.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveFriend(friendToRemove!!)
                        showRemoveDialog = false
                        friendToRemove = null
                    }
                ) {
                    Text(
                        stringResource(Res.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        friendToRemove = null
                    }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 4.dp)
    )
}