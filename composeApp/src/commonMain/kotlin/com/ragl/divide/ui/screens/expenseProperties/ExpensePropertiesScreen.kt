package com.ragl.divide.ui.screens.expenseProperties

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.Category
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.ui.components.DateTimePickerDialog
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.utils.DivideTextField
import com.ragl.divide.ui.utils.formatDate
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.add_expense
import dividemultiplatform.composeapp.generated.resources.amount
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.category
import dividemultiplatform.composeapp.generated.resources.divided_in
import dividemultiplatform.composeapp.generated.resources.frequency
import dividemultiplatform.composeapp.generated.resources.get_reminders
import dividemultiplatform.composeapp.generated.resources.notes
import dividemultiplatform.composeapp.generated.resources.ok
import dividemultiplatform.composeapp.generated.resources.payments
import dividemultiplatform.composeapp.generated.resources.payments_plural
import dividemultiplatform.composeapp.generated.resources.reminder_permission_message
import dividemultiplatform.composeapp.generated.resources.reminder_permission_title
import dividemultiplatform.composeapp.generated.resources.select_date
import dividemultiplatform.composeapp.generated.resources.starting_from
import dividemultiplatform.composeapp.generated.resources.title
import dividemultiplatform.composeapp.generated.resources.update
import dividemultiplatform.composeapp.generated.resources.update_expense
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource

class ExpensePropertiesScreen(
    private val expenseId: String? = null
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = koinScreenModel<ExpensePropertiesViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        LaunchedEffect(Unit) {
            if (expenseId != null) {
                vm.setViewModelExpense(userViewModel.getExpenseById(expenseId))
            }
        }

        var categoryMenuExpanded by remember { mutableStateOf(false) }
        var frequencyMenuExpanded by remember { mutableStateOf(false) }
        var paymentSuffix by remember { mutableStateOf(Res.string.payments) }


        var selectDateDialogEnabled by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        var selectedDate: Long by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }

        LaunchedEffect(vm.payments) {
            paymentSuffix =
                if (vm.payments == "1") Res.string.payments else Res.string.payments_plural
        }
        val onBackClick: () -> Unit = {
            navigator.pop()
        }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(if (expenseId == null) Res.string.add_expense else Res.string.update_expense),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.reminder_permission_message),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        textContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                DivideTextField(
                    label = stringResource(Res.string.title),
                    input = vm.title,
                    error = vm.titleError,
                    onValueChange = vm::updateTitle,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = stringResource(Res.string.category),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ExposedDropdownMenuBox(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                ) {
                    TextField(
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        value = vm.category.name,
                        onValueChange = {},
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .clip(ShapeDefaults.Medium)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false },
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                            .clip(CircleShape)
                    ) {
                        Category.entries.forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = it.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    vm.updateCategory(it)
                                    categoryMenuExpanded = false
                                },
                                modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }
                    }
                }
                DivideTextField(
                    label = stringResource(Res.string.amount),
                    keyboardType = KeyboardType.Number,
                    prefix = { Text(text = "$", style = MaterialTheme.typography.bodyMedium) },
                    input = vm.amount,
                    error = vm.amountError,
                    enabled = vm.amountPaid == 0.0,
                    onValueChange = { input ->
                        if (input.isEmpty()) vm.updateAmount("") else {
                            val formatted = input.replace(",", ".")
                            val parsed = formatted.toDoubleOrNull()
                            parsed?.let {
                                val decimalPart = formatted.substringAfter(".", "")
                                if (decimalPart.length <= 2 && parsed <= 999999999.99) {
                                    vm.updateAmount(input)
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                DivideTextField(
                    label = stringResource(Res.string.divided_in),
                    input = vm.payments,
                    error = vm.paymentsError,
                    keyboardType = KeyboardType.Number,
                    suffix = {
                        Text(
                            stringResource(paymentSuffix),
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                    },
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) vm.updatePayments(
                            it
                        )
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                DivideTextField(
                    label = stringResource(Res.string.notes),
                    input = vm.notes,
                    onValueChange = vm::updateNotes,
                    imeAction = ImeAction.Default,
                    singleLine = false,
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .padding(bottom = 12.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = vm.isRemindersEnabled,
                        onCheckedChange = vm::handleReminderPermissionCheck,
                        colors = CheckboxDefaults.colors(uncheckedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = stringResource(Res.string.get_reminders),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
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
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                    value = vm.frequency.name,
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
                                    onDismissRequest = { frequencyMenuExpanded = false },
                                    modifier = Modifier
                                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Frequency.entries.forEach {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    it.name,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            onClick = {
                                                vm.updateFrequency(it)
                                                frequencyMenuExpanded = false
                                            },
                                            modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
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
                                    text = formatDate(selectedDate) ?: stringResource(Res.string.select_date),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        vm.saveExpense(
                            onSuccess = {
                                userViewModel.saveExpense(it)
                                navigator.pop()
                            },
                            onError = {
                                userViewModel.handleError(Exception(it))
                            }
                        )
                    },
                    shape = ShapeDefaults.Medium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .size(64.dp)
                ) {
                    Text(
                        text = stringResource(if (expenseId == null) Res.string.add else Res.string.update),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

    }
}