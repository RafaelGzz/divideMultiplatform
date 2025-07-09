package com.ragl.divide.presentation.screens.expenseReminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.presentation.components.AdaptiveFAB
import com.ragl.divide.presentation.components.DateTimePickerDialog
import com.ragl.divide.presentation.screens.expenseProperties.ExpensePropertiesViewModel
import com.ragl.divide.presentation.utils.formatDate
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.frequency
import dividemultiplatform.composeapp.generated.resources.get_reminders
import dividemultiplatform.composeapp.generated.resources.notification_permission_rejected_message
import dividemultiplatform.composeapp.generated.resources.notification_permission_rejected_title
import dividemultiplatform.composeapp.generated.resources.ok
import dividemultiplatform.composeapp.generated.resources.reminder_permission_message
import dividemultiplatform.composeapp.generated.resources.reminder_permission_title
import dividemultiplatform.composeapp.generated.resources.reminders
import dividemultiplatform.composeapp.generated.resources.save
import dividemultiplatform.composeapp.generated.resources.starting_from
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class ExpenseReminderScreen(
    private val expenseId: String
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = koinScreenModel<ExpensePropertiesViewModel>()
        val appStateService: AppStateService = koinInject()

        LaunchedEffect(Unit) {
            vm.setViewModelExpense(expenseId)
        }

        var frequencyMenuExpanded by remember { mutableStateOf(false) }
        var selectDateDialogEnabled by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        var selectedDate = vm.startingDate

        Scaffold(
            topBar = {
                AppBar(
                    onBackClick = { navigator.pop() }
                )
            },
            floatingActionButton = {
                AdaptiveFAB(
                    onClick = {
                        appStateService.showLoading()
                        vm.saveExpense(
                            onSuccess = {
                                appStateService.hideLoading()
                                navigator.pop()
                            },
                            onError = {
                                appStateService.hideLoading()
                                appStateService.handleError(it)
                            }
                        )
                    },
                    icon = Icons.Default.Check,
                    contentDescription = stringResource(Res.string.save),
                    text = stringResource(Res.string.save),
                )
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .imePadding()
                    .verticalScroll(state = scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            ) {
                if (selectDateDialogEnabled) {
                    DateTimePickerDialog(
                        initialTime = selectedDate,
                        onDismissRequest = { selectDateDialogEnabled = false },
                        onConfirmClick = {
                            vm.updateStartingDate(it)
                            selectedDate = it
                        }
                    )
                }
                
                if (vm.reminderPermissionMessageDialogEnabled) {
                    AlertDialog(
                        onDismissRequest = vm::onPermissionDialogDismiss,
                        confirmButton = {
                            TextButton(onClick = vm::onPermissionDialogConfirm) {
                                Text(stringResource(Res.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = vm::onPermissionDialogDismiss) {
                                Text(stringResource(Res.string.cancel))
                            }
                        },
                        title = {
                            Text(
                                text = stringResource(Res.string.reminder_permission_title),
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.reminder_permission_message),
                            )
                        }
                    )
                }

                if (vm.notificationPermissionRejectedDialogEnabled) {
                    AlertDialog(
                        onDismissRequest = vm::onNotificationPermissionRejectedDialogDismiss,
                        confirmButton = {
                            TextButton(onClick = vm::onNotificationPermissionRejectedDialogConfirm) {
                                Text(stringResource(Res.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = vm::onNotificationPermissionRejectedDialogDismiss) {
                                Text(stringResource(Res.string.cancel))
                            }
                        },
                        title = {
                            Text(
                                text = stringResource(Res.string.notification_permission_rejected_title),
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.notification_permission_rejected_message),
                            )
                        }
                    )
                }

                // Card con switch de recordatorios
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.get_reminders),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = vm.isRemindersEnabled,
                            onCheckedChange = vm::handleReminderPermissionCheck
                        )
                    }
                }

                // Controles de frecuencia y fecha (sin fondo)
                if (vm.isRemindersEnabled) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            Modifier.weight(.55f)
                        ) {
                            Text(
                                text = stringResource(Res.string.frequency),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = frequencyMenuExpanded,
                                onExpandedChange = {
                                    frequencyMenuExpanded = !frequencyMenuExpanded
                                }
                            ) {
                                TextField(
                                    value = stringResource(vm.frequency.resId),
                                    onValueChange = {},
                                    singleLine = true,
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = frequencyMenuExpanded
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                                        .clip(ShapeDefaults.Medium)
                                )
                                ExposedDropdownMenu(
                                    expanded = frequencyMenuExpanded,
                                    onDismissRequest = { frequencyMenuExpanded = false }
                                ) {
                                    Frequency.entries.forEach {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    stringResource(it.resId),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            onClick = {
                                                vm.updateFrequency(it)
                                                frequencyMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            Modifier.weight(.45f)
                        ) {
                            Text(
                                text = stringResource(Res.string.starting_from),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Button(
                                onClick = { selectDateDialogEnabled = true },
                                shape = ShapeDefaults.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            ) {
                                Text(
                                    text = formatDate(selectedDate),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AppBar(onBackClick: () -> Unit) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    stringResource(Res.string.reminders),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
} 