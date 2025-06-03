package com.ragl.divide.ui.screens.groupProperties

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.ui.components.AdaptiveFAB
import com.ragl.divide.ui.components.ImagePicker
import com.ragl.divide.ui.components.NetworkImage
import com.ragl.divide.ui.components.NetworkImageType
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.main.MainScreen
import com.ragl.divide.ui.utils.DivideTextField
import com.ragl.divide.ui.utils.FriendItem
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.add_friends_to_group
import dividemultiplatform.composeapp.generated.resources.add_group
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.cannot_delete_group
import dividemultiplatform.composeapp.generated.resources.cannot_leave_group
import dividemultiplatform.composeapp.generated.resources.configuration
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.delete_group
import dividemultiplatform.composeapp.generated.resources.delete_group_message
import dividemultiplatform.composeapp.generated.resources.edit
import dividemultiplatform.composeapp.generated.resources.group_members
import dividemultiplatform.composeapp.generated.resources.leave
import dividemultiplatform.composeapp.generated.resources.leave_group
import dividemultiplatform.composeapp.generated.resources.leave_group_message
import dividemultiplatform.composeapp.generated.resources.name
import dividemultiplatform.composeapp.generated.resources.search
import dividemultiplatform.composeapp.generated.resources.select_friends
import dividemultiplatform.composeapp.generated.resources.select_group_members
import dividemultiplatform.composeapp.generated.resources.simplify_debts
import dividemultiplatform.composeapp.generated.resources.update_group
import dividemultiplatform.composeapp.generated.resources.you_have_no_friends
import org.jetbrains.compose.resources.stringResource

class GroupPropertiesScreen(
    private val groupId: String? = null
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<GroupPropertiesViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        LaunchedEffect(groupId) {
            if (groupId != null) {
                val group = userViewModel.getGroupById(groupId)
                val members = userViewModel.getGroupMembers(groupId)
                val uuid = userViewModel.getUUID()
                viewModel.setGroup(group, members, uuid)
            }
        }

        val isLoading by viewModel.isLoading.collectAsState()
        val groupState by viewModel.group.collectAsState()
        val temporaryImagePath by viewModel.temporaryImagePath.collectAsState()

        val defaultColors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        val selectedColors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        val selectedFriends = remember { mutableStateListOf<UserInfo>() }

        var dialogEnabled by remember { mutableStateOf(false) }
        var isDeleteDialog by remember { mutableStateOf(false) }
        var showImagePicker by remember { mutableStateOf(false) }

        var showFriendSelection by remember { mutableStateOf(false) }
        var searchText by remember { mutableStateOf("") }

        val isUpdate by remember { mutableStateOf(groupId != null) }

        val onDeleteGroup = {
            userViewModel.removeGroup(groupId!!)
            userViewModel.hideLoading()
            navigator.replaceAll(MainScreen())
        }

        val friends: List<UserInfo> = userViewModel.state.value.friends.values.toList()

        val canLeave = viewModel.canLeaveGroup
        val canDelete = viewModel.canDeleteGroup

        Box {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(animationSpec = tween(500)),
                exit = ExitTransition.None
            ) {
                AnimatedVisibility(
                    visible = !showFriendSelection,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(stringResource(if (!isUpdate) Res.string.add_group else Res.string.update_group))
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                                ),
                                navigationIcon = {
                                    IconButton(
                                        onClick = { navigator.pop() }
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        },
                        floatingActionButton = {
                            AnimatedVisibility(
                                visible = (!isUpdate && selectedFriends.isNotEmpty()) || isUpdate,
                                enter = fadeIn(animationSpec = tween(300)),
                                exit = fadeOut(animationSpec = tween(300))
                            ) {
                                AdaptiveFAB(
                                    onClick = {
                                        if (viewModel.validateName()) {
                                            userViewModel.showLoading()
                                            viewModel.saveGroup(
                                                onSuccess = { group ->
                                                    userViewModel.setGroupMembers(group)
                                                    userViewModel.saveGroup(group)
                                                    userViewModel.hideLoading()
                                                    navigator.pop()
                                                },
                                                onError = {
                                                    userViewModel.hideLoading()
                                                    userViewModel.handleError(it)
                                                }
                                            )
                                        }
                                    },
                                    icon = Icons.Default.Check,
                                    contentDescription = if (!isUpdate) stringResource(Res.string.add_group) else stringResource(Res.string.edit),
                                    text = if (!isUpdate) stringResource(Res.string.add_group) else stringResource(Res.string.edit),
                                )
                            }
                        }
                    ) { paddingValues ->
                        if (showImagePicker) {
                            ImagePicker(
                                onImageSelected = { imagePath ->
                                    viewModel.updateImage(imagePath)
                                    showImagePicker = false
                                },
                                onDismiss = { showImagePicker = false }
                            )
                        } else if (dialogEnabled) {
                            if (isDeleteDialog)
                                AlertDialog(
                                    onDismissRequest = { dialogEnabled = false },
                                    title = {
                                        Text(
                                            stringResource(Res.string.delete_group),
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    },
                                    text = {
                                        Text(
                                            stringResource(Res.string.delete_group_message),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            userViewModel.showLoading()
                                            dialogEnabled = false
                                            viewModel.deleteGroup(onDeleteGroup) {
                                                userViewModel.hideLoading()
                                                userViewModel.handleError(it)
                                            }
                                        }) {
                                            Text(stringResource(Res.string.delete))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { dialogEnabled = false }) {
                                            Text(stringResource(Res.string.cancel))
                                        }
                                    }
                                )
                            else
                                AlertDialog(
                                    onDismissRequest = { dialogEnabled = false },
                                    title = {
                                        Text(
                                            stringResource(Res.string.leave_group),
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    },
                                    text = {
                                        Text(
                                            stringResource(Res.string.leave_group_message),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            dialogEnabled = false
                                            userViewModel.showLoading()
                                            viewModel.leaveGroup(
                                                onSuccessful = onDeleteGroup,
                                                onError = {
                                                    userViewModel.handleError(it)
                                                    userViewModel.hideLoading()
                                                })
                                        }) {
                                            Text(stringResource(Res.string.leave))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { dialogEnabled = false }) {
                                            Text(stringResource(Res.string.cancel))
                                        }
                                    }
                                )
                        }
                        LazyColumn(
                            modifier = Modifier
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp)
                                .imePadding()
                        ) {
                            item {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .wrapContentHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clickable { showImagePicker = true }
                                            .clip(ShapeDefaults.Medium)
                                            .background(MaterialTheme.colorScheme.surfaceContainer),
                                    ) {
                                        if (temporaryImagePath != null) {
                                            // Mostrar la imagen temporal seleccionada
                                            NetworkImage(
                                                imageUrl = temporaryImagePath,
                                                modifier = Modifier.fillMaxSize(),
                                                type = NetworkImageType.GROUP
                                            )
                                        } else if (groupState.image.isNotEmpty()) {
                                            // Mostrar la imagen actual del grupo
                                            NetworkImage(
                                                imageUrl = groupState.image,
                                                modifier = Modifier.fillMaxSize(),
                                                type = NetworkImageType.GROUP
                                            )
                                        } else {
                                            // No hay imagen
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = "Add image button",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .align(Alignment.Center)
                                            )
                                        }
                                    }
                                    DivideTextField(
                                        label = stringResource(Res.string.name),
                                        input = groupState.name,
                                        error = viewModel.nameError,
                                        onValueChange = { viewModel.updateName(it) })
                                }
                                Text(
                                    text = stringResource(if (!isUpdate) Res.string.select_group_members else Res.string.group_members),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .wrapContentHeight()
                                )
                            }
                            if (!isUpdate) {
                                if (friends.isEmpty()) {
                                    item {
                                        Text(
                                            stringResource(Res.string.you_have_no_friends),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(vertical = 32.dp)
                                        )
                                    }
                                }
                                items(friends) { friend ->
                                    val isSelected = selectedFriends.contains(friend)
                                    FriendItem(
                                        headline = friend.name,
                                        photoUrl = friend.photoUrl,
                                        colors = if (isSelected) selectedColors else defaultColors,
                                        onClick = {
                                            if (isSelected) {
                                                userViewModel.showLoading()
                                                viewModel.removeMember(friend)
                                                selectedFriends.remove(friend)
                                                userViewModel.hideLoading()
                                            } else {
                                                userViewModel.showLoading()
                                                viewModel.addMember(friend)
                                                selectedFriends.add(friend)
                                                userViewModel.hideLoading()
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            } else {
                                items(viewModel.members) { member ->
                                    FriendItem(
                                        headline = member.name,
                                        photoUrl = member.photoUrl,
                                        colors = defaultColors
                                    )
                                }
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showFriendSelection = true }
                                            .padding(horizontal = 32.dp, vertical = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Add,
                                            "Add friend",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Text(
                                            stringResource(Res.string.add_friends_to_group),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        text = stringResource(Res.string.configuration),
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier
                                            .padding(top = 16.dp)
                                            .wrapContentHeight()
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Switch(
                                            checked = viewModel.simplifyDebts,
                                            onCheckedChange = {
                                                viewModel.updateSimplifyDebts(it)
                                            },
//                                            colors = SwitchDefaults.colors(
//                                                checkedThumbColor = MaterialTheme.colorScheme.primary,
//                                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
//                                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
//                                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
//                                            )
                                        )
                                        Text(
                                            text = stringResource(Res.string.simplify_debts),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            isDeleteDialog = false
                                            dialogEnabled = true
                                        },
                                        shape = ShapeDefaults.Medium,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error,
                                            disabledContentColor = MaterialTheme.colorScheme.error.copy(
                                                alpha = 0.38f
                                            )
                                        ),
                                        border = BorderStroke(
                                            2.dp,
                                            if (!canLeave) MaterialTheme.colorScheme.error.copy(
                                                alpha = 0.38f
                                            )
                                            else MaterialTheme.colorScheme.error
                                        ),
                                        enabled = canLeave,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = stringResource(Res.string.leave_group),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    }

                                    // Mensaje explicativo si los botones están deshabilitados
                                    if (!canLeave) {
                                        Text(
                                            text = stringResource(Res.string.cannot_leave_group),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            isDeleteDialog = true
                                            dialogEnabled = true
                                        },
                                        shape = ShapeDefaults.Medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.error,
                                            disabledContainerColor = MaterialTheme.colorScheme.errorContainer.copy(
                                                alpha = 0.38f
                                            ),
                                            disabledContentColor = MaterialTheme.colorScheme.error.copy(
                                                alpha = 0.38f
                                            )
                                        ),
                                        enabled = canDelete,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = stringResource(Res.string.delete_group),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    }

                                    // Mensaje explicativo si los botones están deshabilitados
                                    if (!canDelete) {
                                        Text(
                                            text = stringResource(Res.string.cannot_delete_group),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
                AnimatedVisibility(
                    visible = showFriendSelection,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    FriendSelectionScreen(
                        friends = friends,
                        selectedFriends = selectedFriends,
                        members = viewModel.members,
                        searchText = searchText,
                        onSearchTextChange = { text, filter ->
                            searchText = text
                            filter()
                        },
                        onFriendClick = { friendId ->
                            if (selectedFriends.contains(friendId)) {
                                selectedFriends.remove(friendId)
                            } else {
                                selectedFriends.add(friendId)
                            }
                        },
                        onAddClick = {
                            showFriendSelection = false
                            friends.filter { selectedFriends.contains(it) }.map {
                                viewModel.addMember(it)
                            }
                            selectedFriends.clear()
                        },
                        onBackClick = { showFriendSelection = false },
                        selectedColors = selectedColors,
                        defaultColors = defaultColors
                    )
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FriendSelectionScreen(
        friends: List<UserInfo>,
        members: List<UserInfo>,
        selectedFriends: List<UserInfo>,
        searchText: String,
        onSearchTextChange: (String, () -> Unit) -> Unit,
        onFriendClick: (UserInfo) -> Unit,
        onAddClick: () -> Unit,
        onBackClick: () -> Unit,
        selectedColors: CardColors,
        defaultColors: CardColors
    ) {
        var filteredFriends by remember {
            mutableStateOf(friends.filterNot { it.uuid in members.map { m -> m.uuid } })
        }
        var scrollState = rememberScrollState()
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(Res.string.select_friends)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = {
                if (selectedFriends.isNotEmpty())
                    Button(
                        onClick = onAddClick,
                        shape = ShapeDefaults.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            stringResource(Res.string.add),
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .imePadding()
            ) {
                DivideTextField(
                    input = searchText,
                    onValueChange = {
                        onSearchTextChange(it) {
                            filteredFriends =
                                if (it.isEmpty()) friends else friends.filter { friend ->
                                    friend.name.contains(it, ignoreCase = true)
                                }
                        }
                    },
                    onAction = {
                        filteredFriends = searchText.let {
                            if (it.isEmpty()) friends else friends.filter { friend ->
                                friend.name.contains(it, ignoreCase = true)
                            }
                        }
                    },
                    imeAction = ImeAction.Search,
                    label = stringResource(Res.string.search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredFriends, key = { it.uuid }) { friend ->
                        val isSelected = selectedFriends.contains(friend)
                        val isFriendInGroup = members.find { it.uuid == friend.uuid } != null
                        val friendItemColors = if (isFriendInGroup) {
                            defaultColors
                        } else if (isSelected) {
                            selectedColors
                        } else {
                            defaultColors
                        }
                        if (!isFriendInGroup)
                            FriendItem(
                                headline = friend.name,
                                photoUrl = friend.photoUrl,
                                colors = friendItemColors,
                                onClick = { onFriendClick(friend) }
                            )
                    }
                }
            }
        }
    }
}