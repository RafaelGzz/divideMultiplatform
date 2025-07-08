package com.ragl.divide.presentation.screens.groupProperties

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.presentation.components.AdaptiveFAB
import com.ragl.divide.presentation.components.DivideTextField
import com.ragl.divide.presentation.components.EditGuestNameDialog
import com.ragl.divide.presentation.components.FriendItem
import com.ragl.divide.presentation.components.ImagePicker
import com.ragl.divide.presentation.components.NetworkImage
import com.ragl.divide.presentation.components.NetworkImageType
import com.ragl.divide.presentation.screens.main.MainScreen
import com.ragl.divide.presentation.state.LocalUserState
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.EllipsisV
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.add_friends_to_group
import dividemultiplatform.composeapp.generated.resources.add_guest
import dividemultiplatform.composeapp.generated.resources.add_photo
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.cannot_delete_group
import dividemultiplatform.composeapp.generated.resources.cannot_leave_group
import dividemultiplatform.composeapp.generated.resources.configuration
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.delete_group
import dividemultiplatform.composeapp.generated.resources.delete_group_message
import dividemultiplatform.composeapp.generated.resources.edit_name
import dividemultiplatform.composeapp.generated.resources.group_members
import dividemultiplatform.composeapp.generated.resources.guests
import dividemultiplatform.composeapp.generated.resources.leave
import dividemultiplatform.composeapp.generated.resources.leave_group
import dividemultiplatform.composeapp.generated.resources.leave_group_message
import dividemultiplatform.composeapp.generated.resources.name
import dividemultiplatform.composeapp.generated.resources.no_users_found
import dividemultiplatform.composeapp.generated.resources.remove_guest
import dividemultiplatform.composeapp.generated.resources.remove_guest_confirm
import dividemultiplatform.composeapp.generated.resources.search
import dividemultiplatform.composeapp.generated.resources.select_friends
import dividemultiplatform.composeapp.generated.resources.simplify_debts
import dividemultiplatform.composeapp.generated.resources.update
import dividemultiplatform.composeapp.generated.resources.update_group
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject

class GroupPropertiesScreen(
    private val groupId: String
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<GroupPropertiesViewModel>()
        val appStateService: AppStateService = koinInject()

        val userState = LocalUserState.current

        LaunchedEffect(groupId) {
            viewModel.setGroup(groupId)
        }

        val groupState by viewModel.group.collectAsState()
        val temporaryImagePath by viewModel.temporaryImagePath.collectAsState()

        val selectedFriends = remember { mutableStateListOf<UserInfo>() }

        var dialogEnabled by remember { mutableStateOf(false) }
        var isDeleteDialog by remember { mutableStateOf(false) }
        var showImagePicker by remember { mutableStateOf(false) }

        var showFriendSelection by remember { mutableStateOf(false) }
        var searchText by remember { mutableStateOf("") }

        // Variables para gestión de invitados
        var showEditGuestDialog by remember { mutableStateOf(false) }
        var editingGuestId by remember { mutableStateOf<String?>(null) }
        var editingGuestName by remember { mutableStateOf("") }
        var showAddGuestDialog by remember { mutableStateOf(false) }
        var newGuestName by remember { mutableStateOf("") }
        var showRemoveGuestDialog by remember { mutableStateOf(false) }
        var guestToRemove by remember { mutableStateOf<Pair<String, String>?>(null) }

        val friends = userState.friends.values.toList()

        val canLeave = viewModel.canLeaveGroup
        val canDelete = viewModel.canDeleteGroup

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
                            appStateService.showLoading()
                            dialogEnabled = false
                            viewModel.deleteGroup({
                                appStateService.hideLoading()
                                navigator.replaceAll(MainScreen())
                            }) {
                                appStateService.hideLoading()
                                appStateService.handleError(it)
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
                            appStateService.showLoading()
                            viewModel.leaveGroup(
                                onSuccess = {
                                    appStateService.hideLoading()
                                    navigator.replaceAll(MainScreen())
                                },
                                onError = {
                                    appStateService.handleError(it)
                                    appStateService.hideLoading()
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

        // Diálogo para añadir invitado
        if (showAddGuestDialog) {
            EditGuestNameDialog(
                currentName = newGuestName,
                onDismiss = {
                    showAddGuestDialog = false
                    newGuestName = ""
                    viewModel.guestNameError = ""
                },
                onConfirm = { name ->
                    if (viewModel.addGuest(name)) {
                        showAddGuestDialog = false
                        newGuestName = ""
                    }
                },
                errorMessage = viewModel.guestNameError
            )
        }

        // Diálogo para editar nombre de invitado
        if (showEditGuestDialog && editingGuestId != null) {
            EditGuestNameDialog(
                currentName = editingGuestName,
                onDismiss = {
                    showEditGuestDialog = false
                    editingGuestId = null
                    editingGuestName = ""
                    viewModel.guestNameError = ""
                },
                onConfirm = { name ->
                    if (viewModel.updateGuestName(editingGuestId!!, name)) {
                        showEditGuestDialog = false
                        editingGuestId = null
                        editingGuestName = ""
                    }
                },
                errorMessage = viewModel.guestNameError
            )
        }

        // Diálogo de confirmación para eliminar invitado
        if (showRemoveGuestDialog && guestToRemove != null) {
            AlertDialog(
                onDismissRequest = {
                    showRemoveGuestDialog = false
                    guestToRemove = null
                },
                title = {
                    Text(stringResource(Res.string.remove_guest))
                },
                text = {
                    Text(stringResource(Res.string.remove_guest_confirm))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            guestToRemove?.let { (guestId, _) ->
                                viewModel.removeGuest(guestId)
                            }
                            showRemoveGuestDialog = false
                            guestToRemove = null
                        }
                    ) {
                        Text(stringResource(Res.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showRemoveGuestDialog = false
                            guestToRemove = null
                        }
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }

        Box {
            AnimatedVisibility(
                visible = !showFriendSelection,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(stringResource(Res.string.update_group))
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                navigationIconContentColor = MaterialTheme.colorScheme.primary,
                                actionIconContentColor = MaterialTheme.colorScheme.primary
                            ),
                            navigationIcon = {
                                IconButton(
                                    onClick = { navigator.pop() }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        AdaptiveFAB(
                            onClick = {
                                if (viewModel.validateName()) {
                                    appStateService.showLoading()
                                    viewModel.saveGroup(
                                        onSuccess = { group ->
                                            appStateService.hideLoading()
                                            navigator.pop()
                                        },
                                        onError = {
                                            appStateService.hideLoading()
                                            appStateService.handleError(it)
                                        }
                                    )
                                }
                            },
                            icon = Icons.Default.Check,
                            contentDescription = stringResource(Res.string.update),
                            text = stringResource(Res.string.update),
                            modifier = Modifier.imePadding()
                        )
                    }
                ) { paddingValues ->
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                            .imePadding(),
                    ) {
                        // Imagen del grupo
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(60.dp))
                                        .clickable { showImagePicker = true }
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (temporaryImagePath != null) {
                                        NetworkImage(
                                            imageUrl = temporaryImagePath,
                                            modifier = Modifier.fillMaxSize(),
                                            type = NetworkImageType.GROUP
                                        )
                                    } else if (groupState.image.isNotEmpty()) {
                                        NetworkImage(
                                            imageUrl = groupState.image,
                                            modifier = Modifier.fillMaxSize(),
                                            type = NetworkImageType.GROUP
                                        )
                                    } else {
                                        Icon(
                                            vectorResource(Res.drawable.add_photo),
                                            contentDescription = stringResource(Res.string.add_photo),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Campo de nombre
                        item {
                            DivideTextField(
                                label = stringResource(Res.string.name),
                                value = groupState.name,
                                error = viewModel.nameError,
                                onValueChange = { viewModel.updateName(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Miembros del grupo
                        item {
                            Text(
                                text = stringResource(Res.string.group_members),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        // Lista de miembros actuales
                        itemsIndexed(viewModel.members) { index, member ->
                            FriendItem(
                                headline = member.name,
                                photoUrl = member.photoUrl,
                                modifier = Modifier
                                    .padding(bottom = 2.dp)
                                    .clip(
                                        if (viewModel.members.size == 1 || index == 0)
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = 2.dp,
                                                bottomEnd = 2.dp
                                            )
                                        else RoundedCornerShape(2.dp)
                                    )
                            )
                        }

                        // Botón para añadir más amigos
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showFriendSelection = true }
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(
                                            topStart = 2.dp,
                                            topEnd = 2.dp,
                                            bottomStart = 12.dp,
                                            bottomEnd = 12.dp
                                        )
                                    )
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = stringResource(Res.string.add),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(Res.string.add_friends_to_group),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Gestión de invitados
                        item {
                            Text(
                                text = stringResource(Res.string.guests),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                            )
                        }

                        // Lista de invitados
                        if (viewModel.guests.isNotEmpty()) {
                            val guestsList = viewModel.guests.toList()
                            itemsIndexed(guestsList) { index, (guestId, guestName) ->
                                var showGuestMenu by remember { mutableStateOf(false) }
                                val canRemove =
                                    remember(guestId) { viewModel.canRemoveGuest(guestId) }

                                FriendItem(
                                    headline = guestName,
                                    photoUrl = "",
                                    trailingContent = {
                                        Box {
                                            IconButton(onClick = {
                                                showGuestMenu = true
                                            }) {
                                                Icon(
                                                    FontAwesomeIcons.Solid.EllipsisV,
                                                    contentDescription = "Settings",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = showGuestMenu,
                                                onDismissRequest = { showGuestMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text(stringResource(Res.string.edit_name)) },
                                                    onClick = {
                                                        editingGuestId = guestId
                                                        editingGuestName = guestName
                                                        showEditGuestDialog = true
                                                        showGuestMenu = false
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Filled.Settings,
                                                            contentDescription = null
                                                        )
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            stringResource(Res.string.remove_guest),
                                                            color = if (canRemove) MaterialTheme.colorScheme.error
                                                            else MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.38f
                                                            )
                                                        )
                                                    },
                                                    onClick = {
                                                        if (canRemove) {
                                                            guestToRemove = guestId to guestName
                                                            showRemoveGuestDialog = true
                                                        } else {
                                                            val errorMessage =
                                                                viewModel.getRemoveGuestErrorMessage()
                                                            appStateService.handleError(errorMessage)
                                                        }
                                                        showGuestMenu = false
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Filled.Delete,
                                                            contentDescription = null,
                                                            tint = if (canRemove) MaterialTheme.colorScheme.error
                                                            else MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.38f
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .clip(
                                            if (guestsList.size == 1 || index == 0)
                                                RoundedCornerShape(
                                                    topStart = 12.dp,
                                                    topEnd = 12.dp,
                                                    bottomStart = 2.dp,
                                                    bottomEnd = 2.dp
                                                )
                                            else RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }

                        // Botón para añadir invitado
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        newGuestName = ""
                                        showAddGuestDialog = true
                                    }
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        if (viewModel.guests.isEmpty()) RoundedCornerShape(12.dp)
                                        else RoundedCornerShape(
                                            topStart = 2.dp,
                                            topEnd = 2.dp,
                                            bottomStart = 12.dp,
                                            bottomEnd = 12.dp
                                        )
                                    )
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = stringResource(Res.string.add),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(Res.string.add_guest),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Sección de configuración
                        item {
                            Text(
                                text = stringResource(Res.string.configuration),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                            )
                        }

                        // Switch para simplificar deudas
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainer,
                                        ShapeDefaults.Medium
                                    )
                                    .padding(16.dp)
                            ) {
                                Switch(
                                    checked = viewModel.simplifyDebts,
                                    onCheckedChange = {
                                        viewModel.updateSimplifyDebts(it)
                                    }
                                )
                                Text(
                                    text = stringResource(Res.string.simplify_debts),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }

                        // Botón para salir del grupo
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
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
                                    if (!canLeave) MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                                    else MaterialTheme.colorScheme.error
                                ),
                                enabled = canLeave,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = stringResource(Res.string.leave_group),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }

                        if (!canLeave) {
                            item {
                                Text(
                                    text = stringResource(Res.string.cannot_leave_group),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }

                        // Botón para eliminar grupo
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
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
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = stringResource(Res.string.delete_group),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }

                        if (!canDelete) {
                            item {
                                Text(
                                    text = stringResource(Res.string.cannot_delete_group),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
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
                )
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
    ) {
        var filteredFriends by remember {
            mutableStateOf(friends.filterNot { it.uuid in members.map { m -> m.uuid } })
        }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(Res.string.select_friends)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    selectedFriends.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    AdaptiveFAB(
                        onClick = onAddClick,
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.add),
                        text = stringResource(Res.string.add),
                        modifier = Modifier.imePadding()
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
                    value = searchText,
                    onValueChange = {
                        onSearchTextChange(it) {
                            filteredFriends =
                                if (it.isEmpty()) friends.filterNot { it.uuid in members.map { m -> m.uuid } } else friends.filter { friend ->
                                    friend.name.contains(
                                        it,
                                        ignoreCase = true
                                    ) && members.find { it.uuid == friend.uuid } == null
                                }
                        }
                    },
                    onAction = {
                        filteredFriends = searchText.let {
                            if (it.isEmpty()) friends.filterNot { it.uuid in members.map { m -> m.uuid } } else friends.filter { friend ->
                                friend.name.contains(
                                    it,
                                    ignoreCase = true
                                ) && members.find { it.uuid == friend.uuid } == null
                            }
                        }
                    },
                    prefix = { Icon(Icons.Default.Search, contentDescription = null) },
                    imeAction = ImeAction.Search,
                    label = stringResource(Res.string.search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp).padding(bottom = 8.dp)
                )
                if (filteredFriends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.no_users_found),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        itemsIndexed(filteredFriends) { i, friend ->
                            val isSelected = selectedFriends.contains(friend)
                            FriendItem(
                                headline = friend.name,
                                photoUrl = friend.photoUrl,
                                onClick = { onFriendClick(friend) },
                                trailingContent = {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { onFriendClick(friend) },
                                    )
                                },
                                modifier = Modifier
                                    .clip(
                                        if (filteredFriends.size == 1)
                                            RoundedCornerShape(16.dp)
                                        else
                                            if (i == 0) RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomEnd = 2.dp,
                                                bottomStart = 2.dp
                                            ) else {
                                                if (i == filteredFriends.lastIndex)
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
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
            }
        }
    }

}