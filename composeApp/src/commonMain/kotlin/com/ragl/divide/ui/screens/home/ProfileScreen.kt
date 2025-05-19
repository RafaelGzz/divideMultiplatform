package com.ragl.divide.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.utils.Header
import com.ragl.divide.ui.utils.ProfileImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Moon
import compose.icons.fontawesomeicons.solid.Sun
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.allow_notifications
import dividemultiplatform.composeapp.generated.resources.bar_item_profile_text
import dividemultiplatform.composeapp.generated.resources.dark_mode
import dividemultiplatform.composeapp.generated.resources.light_mode
import dividemultiplatform.composeapp.generated.resources.no
import dividemultiplatform.composeapp.generated.resources.sign_out
import dividemultiplatform.composeapp.generated.resources.sign_out_confirmation
import dividemultiplatform.composeapp.generated.resources.system_default
import dividemultiplatform.composeapp.generated.resources.theme
import dividemultiplatform.composeapp.generated.resources.yes
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProfileBody(
    modifier: Modifier = Modifier,
    user: User,
    onSignOut: () -> Unit,
    isDarkMode: String?,
    onChangeDarkMode: (Boolean?) -> Unit
) {
    val allowNotifications = remember { mutableStateOf(true) }
    var isSignOutDialogVisible by remember { mutableStateOf(false) }
    LazyColumn {
        if (isSignOutDialogVisible) {
            item {
                AlertDialog(
                    onDismissRequest = { isSignOutDialogVisible = false },
                    confirmButton = {
                        TextButton(onClick = {
                            isSignOutDialogVisible = false
                            onSignOut()
                        }) {
                            Text(stringResource(Res.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isSignOutDialogVisible = false }) {
                            Text(stringResource(Res.string.no))
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
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    textContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        item {
            Header(
                title = stringResource(Res.string.bar_item_profile_text)
            )
        }
        item {
            Box(modifier = modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (user.photoUrl.isNotEmpty()) {
                        ProfileImage(user.photoUrl)
                    } else {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .padding(12.dp)
                                .clip(CircleShape)
                        )
                    }
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
            Spacer(modifier = Modifier.height(8.dp))
            NotificationSetting(allowNotifications)
            Spacer(modifier = Modifier.height(16.dp))
            DarkModeSetting(isDarkMode, onChangeDarkMode)
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
    onChangeDarkMode: (Boolean?) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }
    val isDarkModeEnabled = isDarkMode == "true" || isSystemInDarkTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = { isExpanded.value = !isExpanded.value },
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
                    Modifier.padding(horizontal = 12.dp),
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
                    Modifier.padding(horizontal = 12.dp),
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
                    Modifier.padding(horizontal = 12.dp),
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