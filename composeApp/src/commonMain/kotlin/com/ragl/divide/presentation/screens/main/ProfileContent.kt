package com.ragl.divide.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.presentation.components.NetworkImage
import com.ragl.divide.presentation.components.NetworkImageType
import com.ragl.divide.presentation.state.LocalThemeState
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Moon
import compose.icons.fontawesomeicons.solid.Sun
import compose.icons.fontawesomeicons.solid.UserFriends
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.allow_notifications
import dividemultiplatform.composeapp.generated.resources.bar_item_3_text
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.dark_mode
import dividemultiplatform.composeapp.generated.resources.light_mode
import dividemultiplatform.composeapp.generated.resources.sign_out
import dividemultiplatform.composeapp.generated.resources.sign_out_confirmation
import dividemultiplatform.composeapp.generated.resources.system_default
import dividemultiplatform.composeapp.generated.resources.theme
import dividemultiplatform.composeapp.generated.resources.your_friends
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileContent(
    user: User,
    onSignOut: () -> Unit,
    onChangeDarkMode: (Boolean?) -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    val allowNotifications = remember { mutableStateOf(true) }
    val isDarkMode = LocalThemeState.current

    Column {
        Header(
            title = stringResource(Res.string.bar_item_3_text)
        )
        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NetworkImage(
                    imageUrl = user.photoUrl,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(52.dp),
                    type = NetworkImageType.PROFILE
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
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
                IconButton(
                    onClick = { navigator.push(UserScreen()) }
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                    )
                }
            }
        }
        Column(
            modifier = Modifier
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            DarkModeSetting(
                isDarkMode = isDarkMode,
                onChangeDarkMode = onChangeDarkMode,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                var showSignOutDialog by remember { mutableStateOf(false) }

                TextButton(onClick = { showSignOutDialog = true }) {
                    Text(
                        stringResource(Res.string.sign_out),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }

                // Diálogo de confirmación para cerrar sesión
                if (showSignOutDialog) {
                    AlertDialog(
                        onDismissRequest = { showSignOutDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showSignOutDialog = false
                                onSignOut()
                            }) {
                                Text(stringResource(Res.string.sign_out))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSignOutDialog = false }) {
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
    onChangeDarkMode: (Boolean?) -> Unit,
    isDarkMode: String?,
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
        shape = ShapeDefaults.Medium
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

