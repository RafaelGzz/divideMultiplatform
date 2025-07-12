package com.ragl.divide.presentation.screens.groupProperties

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ragl.divide.presentation.state.LocalUserState
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add_guest
import dividemultiplatform.composeapp.generated.resources.add_photo
import dividemultiplatform.composeapp.generated.resources.create_group
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.guests
import dividemultiplatform.composeapp.generated.resources.name
import dividemultiplatform.composeapp.generated.resources.select_group_members
import dividemultiplatform.composeapp.generated.resources.you_have_no_friends
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject

class CreateGroupScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<GroupPropertiesViewModel>()
        val appStateService: AppStateService = koinInject()
        val userState = LocalUserState.current

        val groupState by viewModel.group.collectAsState()
        val temporaryImagePath by viewModel.temporaryImagePath.collectAsState()
        val selectedFriends = remember { mutableStateListOf<UserInfo>() }

        var showImagePicker by remember { mutableStateOf(false) }
        var showAddGuestDialog by remember { mutableStateOf(false) }
        var newGuestName by remember { mutableStateOf("") }

        val friends = userState.friends.values.toList()

        if (showImagePicker) {
            ImagePicker(
                onImageSelected = { imagePath ->
                    viewModel.updateImage(imagePath)
                    showImagePicker = false
                },
                onDismiss = { showImagePicker = false }
            )
        }

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

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(Res.string.create_group))
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                if (selectedFriends.isNotEmpty() || viewModel.guests.isNotEmpty()) {
                    AdaptiveFAB(
                        onClick = {
                            if (viewModel.validateName()) {
                                appStateService.showLoading()
                                viewModel.saveGroup(
                                    onSuccess = {
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
                        contentDescription = stringResource(Res.string.create_group),
                        text = stringResource(Res.string.create_group),
                        modifier = Modifier.imePadding()
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .imePadding(),
            ) {
                // Imagen del grupo centrada
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
                            } else {
                                Icon(
                                    vectorResource(Res.drawable.add_photo),
                                    contentDescription = stringResource(Res.string.add_photo),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de nombre
                        DivideTextField(
                            label = stringResource(Res.string.name),
                            value = groupState.name,
                            error = viewModel.nameError,
                            onValueChange = { viewModel.updateName(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Selección de amigos
                item {
                    Text(
                        text = stringResource(Res.string.select_group_members),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (friends.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.you_have_no_friends),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    itemsIndexed(friends) { index, friend ->
                        val isSelected = selectedFriends.contains(friend)
                        FriendItem(
                            headline = friend.name,
                            photoUrl = friend.photoUrl,
                            onClick = {
                                if (isSelected) {
                                    viewModel.removeMember(friend)
                                    selectedFriends.remove(friend)
                                } else {
                                    viewModel.addMember(friend)
                                    selectedFriends.add(friend)
                                }
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (isSelected) {
                                            viewModel.removeMember(friend)
                                            selectedFriends.remove(friend)
                                        } else {
                                            viewModel.addMember(friend)
                                            selectedFriends.add(friend)
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .padding(bottom = 2.dp)
                                .clip(
                                    if (friends.size == 1) RoundedCornerShape(12.dp)
                                    else if (index == 0) RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = 4.dp,
                                        bottomEnd = 4.dp
                                    )
                                    else if (index == friends.lastIndex) RoundedCornerShape(
                                        topStart = 4.dp,
                                        topEnd = 4.dp,
                                        bottomStart = 12.dp,
                                        bottomEnd = 12.dp
                                    )
                                    else RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(Res.string.guests),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                    )
                }
                if (viewModel.guests.isNotEmpty()) {
                    val guestsList = viewModel.guests.toList()
                    itemsIndexed(guestsList) { index, (guestId, guestName) ->
                        FriendItem(
                            headline = guestName,
                            photoUrl = "",
                            trailingContent = {
                                IconButton(
                                    onClick = { viewModel.removeGuest(guestId) }
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(Res.string.delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(bottom = 2.dp)
                                .clip(
                                    if (index == 0 || guestsList.size == 1)
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
                            contentDescription = "Añadir",
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

                // Espaciado final
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
} 