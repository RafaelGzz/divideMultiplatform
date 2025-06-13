package com.ragl.divide.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.ui.components.ImagePicker
import com.ragl.divide.ui.components.NetworkImage
import com.ragl.divide.ui.components.NetworkImageType
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.utils.DivideTextField
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.email_address_text
import dividemultiplatform.composeapp.generated.resources.save_changes
import dividemultiplatform.composeapp.generated.resources.user_profile
import dividemultiplatform.composeapp.generated.resources.username
import dividemultiplatform.composeapp.generated.resources.username_empty_error
import dividemultiplatform.composeapp.generated.resources.username_special_chars_error
import dividemultiplatform.composeapp.generated.resources.username_too_long_error
import org.jetbrains.compose.resources.stringResource

class UserScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        val state by userViewModel.state.collectAsState()
        val user = state.user

        var showImagePicker by remember { mutableStateOf(false) }
        var temporaryImagePath by remember { mutableStateOf<String?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showSaveDialog by remember { mutableStateOf(false) }

        // Estados para editar nombre
        var tempUserName by remember { mutableStateOf(user.name) }
        var usernameError by remember { mutableStateOf<String?>(null) }

        // Strings de validación
        val usernameEmptyError = stringResource(Res.string.username_empty_error)
        val usernameSpecialCharsError = stringResource(Res.string.username_special_chars_error)
        val usernameTooLongError = stringResource(Res.string.username_too_long_error)

        // Función de validación para username
        fun validateUsername() {
            usernameError = when {
                tempUserName.isBlank() -> usernameEmptyError
                tempUserName.length > 20 -> usernameTooLongError
                !tempUserName.matches(Regex("^[a-zA-Z0-9_]+$")) -> usernameSpecialCharsError
                else -> null
            }
        }

        // Verificar si hay cambios pendientes y si el username es válido
        val isUsernameValid = usernameError == null
        val hasChanges =
            temporaryImagePath != null || (tempUserName != user.name && isUsernameValid)

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.user_profile),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                AnimatedVisibility(hasChanges, enter = fadeIn(), exit = fadeOut()) {
                    FloatingActionButton(
                        onClick = { showSaveDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(Res.string.save_changes)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))

                        // Imagen principal (clickeable)
                        Card(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showImagePicker = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                        ) {
                            NetworkImage(
                                imageUrl = temporaryImagePath ?: user.photoUrl,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                type = NetworkImageType.PROFILE
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        // Información del usuario
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                DivideTextField(
                                    value = tempUserName,
                                    onValueChange = {
                                        tempUserName = it
                                        // Validar en tiempo real
                                        usernameError = when {
                                            it.isBlank() -> usernameEmptyError
                                            it.length > 20 -> usernameTooLongError
                                            !it.matches(Regex("^[a-zA-Z0-9_]+$")) -> usernameSpecialCharsError
                                            else -> null
                                        }
                                    },
                                    label = stringResource(Res.string.username),
                                    modifier = Modifier.fillMaxWidth(),
                                    imeAction = ImeAction.Done,
                                    error = usernameError,
                                    validate = ::validateUsername
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                DivideTextField(
                                    value = user.email,
                                    label = stringResource(Res.string.email_address_text),
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                if (showImagePicker) {
                    ImagePicker(
                        onImageSelected = { imagePath ->
                            temporaryImagePath = imagePath
                            showImagePicker = false
                        },
                        onDismiss = { showImagePicker = false }
                    )
                }

                // Diálogo de confirmación para guardar cambios
                if (showSaveDialog) {
                    AlertDialog(
                        onDismissRequest = { showSaveDialog = false },
                        title = {
                            Text(
                                stringResource(Res.string.save_changes),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        text = {
                            Text(
                                "¿Deseas guardar los cambios realizados?",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    // Guardar imagen si hay una nueva
                                    if (temporaryImagePath != null) {
                                        userViewModel.updateProfileImage(
                                            temporaryImagePath!!,
                                            onSuccess = {
                                                temporaryImagePath = null
                                            },
                                            onError = { error ->
                                                errorMessage = error
                                            }
                                        )
                                    }

                                    // Guardar nombre si cambió y es válido
                                    if (tempUserName != user.name && isUsernameValid) {
                                        userViewModel.updateUserName(
                                            tempUserName,
                                            onSuccess = {},
                                            onError = { error ->
                                                errorMessage = error
                                            }
                                        )
                                    }

                                    showSaveDialog = false
                                    navigator.pop()
                                }
                            ) {
                                Text(stringResource(Res.string.save_changes))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSaveDialog = false }) {
                                Text(stringResource(Res.string.cancel))
                            }
                        }
                    )
                }

                // Diálogo de error
                if (errorMessage != null) {
                    AlertDialog(
                        onDismissRequest = { errorMessage = null },
                        title = {
                            Text("Error", style = MaterialTheme.typography.titleLarge)
                        },
                        text = {
                            Text(errorMessage!!, style = MaterialTheme.typography.bodyMedium)
                        },
                        confirmButton = {
                            TextButton(onClick = { errorMessage = null }) {
                                Text("OK")
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
                        textContentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
} 