package com.ragl.divide.ui.screens.groupExpenseProperties

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.SplitMethod
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.utils.DivideTextField
import com.ragl.divide.ui.utils.FriendItem
import com.ragl.divide.ui.utils.Strings
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.toTwoDecimals
import com.ragl.divide.ui.utils.validateQuantity
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.add_expense
import dividemultiplatform.composeapp.generated.resources.amount
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.currency_es_mx
import dividemultiplatform.composeapp.generated.resources.dollar_sign
import dividemultiplatform.composeapp.generated.resources.indicate_percentages
import dividemultiplatform.composeapp.generated.resources.indicate_quantities
import dividemultiplatform.composeapp.generated.resources.one_person
import dividemultiplatform.composeapp.generated.resources.paid_by
import dividemultiplatform.composeapp.generated.resources.percent_sign
import dividemultiplatform.composeapp.generated.resources.remaining_x
import dividemultiplatform.composeapp.generated.resources.select_who_pays
import dividemultiplatform.composeapp.generated.resources.split_method
import dividemultiplatform.composeapp.generated.resources.title
import dividemultiplatform.composeapp.generated.resources.update
import dividemultiplatform.composeapp.generated.resources.update_expense
import dividemultiplatform.composeapp.generated.resources.x_exceeded
import dividemultiplatform.composeapp.generated.resources.x_of_y
import dividemultiplatform.composeapp.generated.resources.x_people
import dividemultiplatform.composeapp.generated.resources.x_per_person
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


class GroupExpensePropertiesScreen(
    private val groupId: String,
    private val expenseId: String? = null,
    private val eventId: String? = null
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = koinScreenModel<GroupExpensePropertiesViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        val strings = koinInject<Strings>()

        LaunchedEffect(groupId, expenseId, eventId) {
            val group = userViewModel.getGroupById(groupId)
            val uuid = userViewModel.getUUID()
            val members = userViewModel.getGroupMembers(groupId)
            val expense = userViewModel.getGroupExpenseById(groupId, expenseId, eventId)
            val event = userViewModel.getEventById(groupId, eventId)

            vm.setGroupAndExpense(group, uuid, members, expense, event)
        }

        var paidByMenuExpanded by remember { mutableStateOf(false) }
        var methodMenuExpanded by remember { mutableStateOf(false) }

        val sortedMembers = remember(vm.members) {
            vm.members.sortedWith(compareBy({ it.uuid != vm.userId }, { it.name.lowercase() }))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                stringResource(if (!vm.isUpdate.value) Res.string.add_expense else Res.string.update_expense),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { navigator.pop() }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(Res.string.back),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                },
                bottomBar = {

                }
            ) { paddingValues ->
                Column(
                    Modifier
                        .padding(paddingValues)
                        .imePadding()
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    DivideTextField(
                        label = stringResource(Res.string.title),
                        input = vm.title,
                        error = vm.titleError,
                        onValueChange = vm::updateTitle,
                        validate = vm::validateTitle,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                    )
                    DivideTextField(
                        label = stringResource(Res.string.amount),
                        keyboardType = KeyboardType.Number,
                        prefix = {
                            Text(
                                text = stringResource(Res.string.dollar_sign),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        input = vm.amount,
                        error = vm.amountError,
                        validate = vm::validateAmount,
                        onValueChange = { input ->
                            validateQuantity(input, vm::updateAmount)
                            vm.updateAmountPerPerson(
                                if (vm.selectedMembers.isEmpty()) 0.0 else {
                                    ((vm.amount.toDoubleOrNull()
                                        ?: 0.0) / vm.selectedMembers.size).toTwoDecimals()
                                }
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = stringResource(Res.string.paid_by),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ExposedDropdownMenuBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            expanded = paidByMenuExpanded,
                            onExpandedChange = { paidByMenuExpanded = !paidByMenuExpanded }) {
                            TextField(
//                                colors = TextFieldDefaults.colors(
//                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                                    focusedIndicatorColor = Color.Transparent,
//                                    unfocusedIndicatorColor = Color.Transparent,
//                                ),
                                value = vm.payer.name,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                onValueChange = {},
                                singleLine = true,
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paidByMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                                    .clip(ShapeDefaults.Medium)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = paidByMenuExpanded,
                                onDismissRequest = { paidByMenuExpanded = false },
//                                modifier = Modifier
//                                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                sortedMembers.forEach {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = it.name,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }, onClick = {
                                            vm.updatePayer(it)
                                            paidByMenuExpanded = false
                                        },
//                                        modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                }
                            }
                        }
                    }
                    AnimatedVisibility(
                        vm.amount.toDoubleOrNull() != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = stringResource(Res.string.split_method),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                ExposedDropdownMenuBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 20.dp),
                                    expanded = methodMenuExpanded,
                                    onExpandedChange = {
                                        methodMenuExpanded = !methodMenuExpanded
                                    }) {
                                    TextField(
//                                        colors = TextFieldDefaults.colors(
//                                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                                            focusedIndicatorColor = Color.Transparent,
//                                            unfocusedIndicatorColor = Color.Transparent,
//                                        ),
                                        value = stringResource(vm.splitMethod.resId),
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        onValueChange = {},
                                        singleLine = true,
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = methodMenuExpanded
                                            )
                                        },
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                                            .clip(ShapeDefaults.Medium)
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = methodMenuExpanded,
                                        onDismissRequest = { methodMenuExpanded = false },
                                        modifier = Modifier
//                                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        SplitMethod.entries.forEach {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        stringResource(it.resId),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }, onClick = {
                                                    vm.updateMethod(it)
                                                    if (it == SplitMethod.EQUALLY) {
                                                        vm.updateAmountPerPerson(
                                                            if (vm.selectedMembers.isEmpty()) 0.0 else {
                                                                ((vm.amount.toDoubleOrNull()
                                                                    ?: 0.0) / vm.selectedMembers.size).toTwoDecimals()
                                                            }
                                                        )
                                                    }

                                                    methodMenuExpanded = false
                                                },
//                                                modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = when (vm.splitMethod) {
                                    SplitMethod.EQUALLY -> stringResource(Res.string.select_who_pays)
                                    SplitMethod.PERCENTAGES -> stringResource(Res.string.indicate_percentages)
                                    SplitMethod.CUSTOM -> stringResource(Res.string.indicate_quantities)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .padding(horizontal = 16.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                when (vm.splitMethod) {
                                    SplitMethod.EQUALLY -> {
                                        Text(
                                            stringResource(
                                                Res.string.x_per_person,
                                                formatCurrency(
                                                    vm.amountPerPerson,
                                                    stringResource(Res.string.currency_es_mx)
                                                )
                                            ),
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                                        )
                                        Text(
                                            if (vm.selectedMembers.size == 1) stringResource(
                                                Res.string.one_person,
                                                vm.selectedMembers.size
                                            ) else stringResource(
                                                Res.string.x_people,
                                                vm.selectedMembers.size
                                            ),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    SplitMethod.PERCENTAGES -> {
                                        val percentageSum = vm.percentages.values.sum()
                                        val remainingPercentage = 100 - percentageSum
                                        val exceeded = percentageSum - 100
                                        Text(
                                            stringResource(
                                                Res.string.x_of_y,
                                                percentageSum.toString() + stringResource(Res.string.percent_sign),
                                                "100" + stringResource(Res.string.percent_sign)
                                            ),
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                                        )
                                        if (percentageSum <= 100.0) {
                                            Text(
                                                stringResource(
                                                    Res.string.remaining_x,
                                                    remainingPercentage.toString() + stringResource(
                                                        Res.string.percent_sign
                                                    )
                                                ),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        } else {
                                            Text(
                                                stringResource(
                                                    Res.string.x_exceeded,
                                                    exceeded.toString() + stringResource(Res.string.percent_sign)
                                                ),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            )
                                        }
                                    }

                                    SplitMethod.CUSTOM -> {
                                        val amount = vm.amount.toDoubleOrNull() ?: 0.0
                                        val quantitiesSum = vm.quantities.values.sum()
                                        val remainingQuantity =
                                            (amount - quantitiesSum).toTwoDecimals()
                                        val exceeded = (quantitiesSum - amount).toTwoDecimals()
                                        Text(
                                            stringResource(
                                                Res.string.x_of_y,
                                                stringResource(Res.string.dollar_sign) + quantitiesSum.toString(),
                                                stringResource(Res.string.dollar_sign) + amount.toString()
                                            ),
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                                        )
                                        if (quantitiesSum <= amount)
                                            Text(
                                                stringResource(
                                                    Res.string.remaining_x,
                                                    stringResource(Res.string.dollar_sign) + remainingQuantity.toString()
                                                ),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        else Text(
                                            stringResource(
                                                Res.string.x_exceeded,
                                                stringResource(Res.string.dollar_sign) + exceeded.toString()
                                            ),
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                                        )
                                    }
                                }
                            }
                            sortedMembers.forEach { friend ->
                                val friendQuantity = vm.percentages[friend.uuid] ?: 0
                                var percentage by remember { mutableStateOf(friendQuantity.toString()) }
                                var quantity by remember { mutableStateOf(vm.quantities[friend.uuid]!!.toString()) }
                                val amount = vm.amount.toDoubleOrNull() ?: 0.0
                                FriendItem(
                                    headline = friend.name,
                                    supporting = when (vm.splitMethod) {
                                        SplitMethod.PERCENTAGES -> stringResource(Res.string.dollar_sign) + (amount * friendQuantity / 100).toTwoDecimals()

                                        else -> ""
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    trailingContent = {
                                        when (vm.splitMethod) {
                                            SplitMethod.EQUALLY -> Checkbox(
                                                checked = friend.uuid in vm.selectedMembers,
                                                onCheckedChange = {
                                                    vm.updateSelectedMembers(
                                                        if (it) vm.selectedMembers + friend.uuid
                                                        else vm.selectedMembers - friend.uuid
                                                    )

                                                    vm.updateAmountPerPerson(
                                                        if (vm.selectedMembers.isEmpty()) 0.0 else {
                                                            ((vm.amount.toDoubleOrNull()
                                                                ?: 0.0) / vm.selectedMembers.size).toTwoDecimals()
                                                        }
                                                    )
                                                })

                                            SplitMethod.PERCENTAGES -> Row(
                                                horizontalArrangement = Arrangement.spacedBy(
                                                    8.dp
                                                ), verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                BasicTextField(
                                                    value = percentage,
                                                    onValueChange = {
                                                        if (it.isEmpty()) {
                                                            percentage = ""
                                                            vm.updatePercentages(vm.percentages.mapValues { (key, value) ->
                                                                if (key == friend.uuid) 0
                                                                else value
                                                            })
                                                        } else {
                                                            val input = it.toIntOrNull()
                                                            if (input != null && input in 0..100) {
                                                                percentage = it
                                                                vm.updatePercentages(vm.percentages.mapValues { (key, value) ->
                                                                    if (key == friend.uuid) input
                                                                    else value
                                                                })
                                                            }
                                                        }
                                                    },
                                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                    modifier = Modifier
                                                        .width(80.dp)
                                                        .height(40.dp)
                                                        .clip(ShapeDefaults.Medium)
                                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                                        .padding(vertical = 10.dp)
                                                )
                                                Text(
                                                    text = stringResource(Res.string.percent_sign),
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }

                                            SplitMethod.CUSTOM -> Row(
                                                horizontalArrangement = Arrangement.spacedBy(
                                                    8.dp
                                                ), verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.dollar_sign),
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                BasicTextField(
                                                    value = quantity,
                                                    onValueChange = {
                                                        validateQuantity(it) { res ->
                                                            quantity = res
                                                            vm.updateQuantities(vm.quantities.mapValues { (key, value) ->
                                                                if (key == friend.uuid) if (res.isEmpty()) 0.0 else res.toDouble()
                                                                else value
                                                            })
                                                        }
                                                    },
                                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                    modifier = Modifier
                                                        .width(80.dp)
                                                        .height(40.dp)
                                                        .clip(ShapeDefaults.Medium)
                                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                                        .padding(vertical = 10.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                            Button(
                                onClick = {
                                    if (vm.validateTitle().and(vm.validateAmount())) {
                                        when (vm.splitMethod) {
                                            SplitMethod.EQUALLY -> {
                                                if (vm.selectedMembers.size < 2) {
                                                    userViewModel.handleError(strings.getTwoSelected())
                                                    return@Button
                                                }
                                            }

                                            SplitMethod.PERCENTAGES -> {
                                                if (vm.percentages.values.sum() != 100) {
                                                    userViewModel.handleError(strings.getPercentagesSum())
                                                    return@Button
                                                } else if (vm.percentages.values.any { it == 100 }) {
                                                    userViewModel.handleError(strings.getTwoMustPay())
                                                    return@Button
                                                }
                                            }

                                            SplitMethod.CUSTOM -> {
                                                if (vm.quantities.values.any { it == vm.amount.toDouble() }) {
                                                    userViewModel.handleError(strings.getTwoMustPay())
                                                    return@Button
                                                } else if (vm.quantities.values.sum() != (vm.amount.toDouble())) {
                                                    userViewModel.handleError(strings.getSumMustBe(vm.amount))
                                                    return@Button
                                                }
                                            }
                                        }
                                        userViewModel.showLoading()
                                        vm.saveExpense(
                                            onSuccess = { groupExpense ->
                                                userViewModel.saveGroupExpense(
                                                    groupId,
                                                    groupExpense
                                                )
                                                userViewModel.hideLoading()
                                                navigator.pop()
                                            },
                                            onError = { 
                                                userViewModel.handleError(it) 
                                                userViewModel.hideLoading()
                                            }
                                        )
                                    }
                                },
                                shape = ShapeDefaults.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(if (!vm.isUpdate.value) Res.string.add else Res.string.update),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}