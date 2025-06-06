package com.ragl.divide.ui.screens.eventProperties

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.ui.components.AdaptiveFAB
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.group.GroupScreen
import com.ragl.divide.ui.utils.DivideTextField
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add_event
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.cannot_delete_event
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.delete_event
import dividemultiplatform.composeapp.generated.resources.delete_event_message
import dividemultiplatform.composeapp.generated.resources.description
import dividemultiplatform.composeapp.generated.resources.edit_event
import dividemultiplatform.composeapp.generated.resources.event_title
import dividemultiplatform.composeapp.generated.resources.new_event
import dividemultiplatform.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource

class EventPropertiesScreen(
    private val groupId: String,
    private val eventId: String? = null
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EventPropertiesViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        // Si es un evento existente, cargarlo
        LaunchedEffect(eventId) {
            if (eventId != null) {
                val event = userViewModel.getEventById(groupId, eventId)
                viewModel.setEvent(groupId, event)
            }
        }

        var dialogEnabled by remember { mutableStateOf(false) }

        val canDelete = viewModel.canDeleteGroup

        val eventState by viewModel.event.collectAsState()
        val isEditMode by viewModel.isUpdate.collectAsState()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    title = {
                        Text(
                            text = if (isEditMode) stringResource(Res.string.edit_event) else stringResource(Res.string.new_event),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                        }
                    }
                )
            },
            floatingActionButton = {
                AdaptiveFAB(
                    onClick = {
                        viewModel.saveEvent(
                            groupId = groupId,
                            onSuccess = {
                                userViewModel.saveEvent(groupId, it)
                                navigator.pop()
                            },
                            onError = { userViewModel.handleError(it) }
                        )
                    },
                    icon = Icons.Default.Check,
                    contentDescription = if (!isEditMode) stringResource(Res.string.add_event) else stringResource(Res.string.save),
                    text = if (!isEditMode) stringResource(Res.string.add_event) else stringResource(Res.string.save),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        ) { paddingValues ->
            if (dialogEnabled)
                AlertDialog(
                    onDismissRequest = { dialogEnabled = false },
                    title = {
                        Text(
                            stringResource(Res.string.delete_event),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            stringResource(Res.string.delete_event_message),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            userViewModel.showLoading()
                            dialogEnabled = false
                            viewModel.deleteEvent(
                                onSuccess = {
                                    userViewModel.deleteEvent(groupId, eventId!!)
                                    userViewModel.hideLoading()
                                    navigator.popUntil {
                                        it.key == GroupScreen("").key
                                    }
                                },
                                onError = {
                                    userViewModel.hideLoading()
                                    userViewModel.handleError(it)
                                }
                            )
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
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item{
                    DivideTextField(
                        input = eventState.title,
                        error = viewModel.titleError,
                        onValueChange = viewModel::updateTitle,
                        validate = viewModel::validateTitle,
                        label = stringResource(Res.string.event_title),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de descripción
                    DivideTextField(
                        input = eventState.description,
                        onValueChange = viewModel::updateDescription,
                        label = stringResource(Res.string.description),
                        imeAction = ImeAction.Default,
                        singleLine = false,
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                            .padding(bottom = 12.dp)
                    )
                    if (isEditMode) {
                        Button(
                            onClick = {
                                dialogEnabled = true
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
                                text = stringResource(Res.string.delete_event),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                        if (!canDelete) {
                            Text(
                                text = stringResource(Res.string.cannot_delete_event),
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
}