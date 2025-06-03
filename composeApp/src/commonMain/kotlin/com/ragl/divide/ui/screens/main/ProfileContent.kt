package com.ragl.divide.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.ui.components.ImagePicker
import com.ragl.divide.ui.utils.Header
import com.ragl.divide.ui.utils.ProfileImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Moon
import compose.icons.fontawesomeicons.solid.Sun
import compose.icons.fontawesomeicons.solid.UserFriends
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.allow_notifications
import dividemultiplatform.composeapp.generated.resources.bar_item_profile_text
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.dark_mode
import dividemultiplatform.composeapp.generated.resources.light_mode
import dividemultiplatform.composeapp.generated.resources.sign_out
import dividemultiplatform.composeapp.generated.resources.sign_out_confirmation
import dividemultiplatform.composeapp.generated.resources.system_default
import dividemultiplatform.composeapp.generated.resources.theme
import dividemultiplatform.composeapp.generated.resources.your_friends
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProfileContent(
    user: User,
    isDarkMode: String?,
    friendRequests: List<UserInfo>,
    onSignOut: () -> Unit,
    onChangeDarkMode: (Boolean?) -> Unit,
    onImageSelected: (String, () -> Unit, (String) -> Unit) -> Unit,
    onFriendsButtonClick: () -> Unit,
) {
    val allowNotifications = remember { mutableStateOf(true) }
    var isSignOutDialogVisible by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados para controlar las animaciones escalonadas
    var showHeader by remember { mutableStateOf(true) }
    var showProfileInfo by remember { mutableStateOf(true) }
    var showSettingsSection by remember { mutableStateOf(true) }

    // Animaciones con Modifier para mejor rendimiento
    val headerAlpha by animateFloatAsState(
        targetValue = if (showHeader) 1f else 0f,
        animationSpec = tween(300),
        label = "headerAlpha"
    )
    val headerOffsetY by animateIntAsState(
        targetValue = if (showHeader) 0 else -50,
        animationSpec = tween(300),
        label = "headerOffsetY"
    )

    val profileInfoAlpha by animateFloatAsState(
        targetValue = if (showProfileInfo) 1f else 0f,
        animationSpec = tween(350),
        label = "profileInfoAlpha"
    )
    val profileInfoOffsetY by animateIntAsState(
        targetValue = if (showProfileInfo) 0 else -100,
        animationSpec = tween(350),
        label = "profileInfoOffsetY"
    )

    val settingsSectionAlpha by animateFloatAsState(
        targetValue = if (showSettingsSection) 1f else 0f,
        animationSpec = tween(350),
        label = "settingsSectionAlpha"
    )
    val settingsSectionOffsetY by animateIntAsState(
        targetValue = if (showSettingsSection) 0 else -100,
        animationSpec = tween(350),
        label = "settingsSectionOffsetY"
    )

    // Efecto para iniciar las animaciones escalonadas
    LaunchedEffect(user) {
        showHeader = true
        delay(50)
        showProfileInfo = true
        delay(30)
        showSettingsSection = true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isSignOutDialogVisible) {
            item {
                AlertDialog(
                    onDismissRequest = { isSignOutDialogVisible = false },
                    confirmButton = {
                        TextButton(onClick = {
                            isSignOutDialogVisible = false
                            onSignOut()
                        }) {
                            Text(stringResource(Res.string.sign_out))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isSignOutDialogVisible = false }) {
                            Text(stringResource(Res.string.cancel))
                        }
                    },
                    title = {
                        Text(
                            stringResource(Res.string.sign_out),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            stringResource(Res.string.sign_out_confirmation),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }
        }

        // ImagePicker (cuando showImagePicker es true)
        if (showImagePicker) {
            item {
                ImagePicker(
                    onImageSelected = { imagePath ->
                        onImageSelected(
                            imagePath,
                            { showImagePicker = false },
                            { error ->
                                errorMessage = error
                                showImagePicker = false
                            })
                    },
                    onDismiss = { showImagePicker = false }
                )
            }
        }

        // Mostrar error si existe
        if (errorMessage != null) {
            item {
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

        item {
            Header(
                title = stringResource(Res.string.bar_item_profile_text),
                modifier = Modifier
                    .alpha(headerAlpha)
                    .offset(y = headerOffsetY.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .alpha(profileInfoAlpha)
                    .offset(y = profileInfoOffsetY.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileImage(user.photoUrl, modifier = Modifier.clickable {
                        showImagePicker = true
                    })
                    Column {
                        Text(
                            user.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            user.email,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { isSignOutDialogVisible = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .alpha(settingsSectionAlpha)
                    .offset(y = settingsSectionOffsetY.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                DarkModeSetting(
                    isDarkMode = isDarkMode,
                    onChangeDarkMode = onChangeDarkMode,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FriendsButton(
                    friendRequests = friendRequests,
                    onFriendsButtonClick = onFriendsButtonClick,
                )
            }
        }
    }
}

@Composable
fun FriendsButton(
    friendRequests: List<UserInfo>,
    onFriendsButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = onFriendsButtonClick
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (friendRequests.isNotEmpty()) {
                BadgedBox(
                    badge = {
                        Badge {
                            Text(
                                "${friendRequests.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.semantics {
                                    contentDescription =
                                        "${friendRequests.size} friend requests"
                                })
                        }
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Friend requests"
                    }
                ) {
                    Icon(
                        FontAwesomeIcons.Solid.UserFriends,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else Icon(
                FontAwesomeIcons.Solid.UserFriends,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.your_friends),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
            )

        }
    }
}

@Composable
private fun NotificationSetting(allowNotifications: MutableState<Boolean>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = { allowNotifications.value = !allowNotifications.value }
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.allow_notifications),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = allowNotifications.value,
                onCheckedChange = { allowNotifications.value = it })
        }
    }
}

@Composable
private fun DarkModeSetting(
    isDarkMode: String?,
    onChangeDarkMode: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpanded = remember { mutableStateOf(false) }
    val isDarkModeEnabled = isDarkMode == "true" || isSystemInDarkTheme()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(
                enabled = true,
                onClick = { isExpanded.value = !isExpanded.value },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = if (isExpanded.value) 0.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isDarkModeEnabled) FontAwesomeIcons.Solid.Moon else FontAwesomeIcons.Solid.Sun,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.theme),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                Icon(
                    if (isExpanded.value) Icons.Filled.KeyboardArrowUp
                    else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }
        AnimatedVisibility(visible = isExpanded.value) {
            Column {
                Row(
                    Modifier.padding(horizontal = 12.dp).clickable { onChangeDarkMode(true) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(36.dp))
                    Text(
                        stringResource(Res.string.dark_mode),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    RadioButton(
                        selected = isDarkMode == "true",
                        onClick = { onChangeDarkMode(true) })
                }
                Row(
                    Modifier.padding(horizontal = 12.dp).clickable { onChangeDarkMode(false) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(36.dp))
                    Text(
                        stringResource(Res.string.light_mode),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    RadioButton(
                        selected = isDarkMode == "false",
                        onClick = { onChangeDarkMode(false) })
                }
                Row(
                    Modifier.padding(horizontal = 12.dp).clickable { onChangeDarkMode(null) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(36.dp))
                    Text(
                        stringResource(Res.string.system_default),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    RadioButton(
                        selected = isDarkMode == null,
                        onClick = { onChangeDarkMode(null) })
                }
            }
        }
    }
}