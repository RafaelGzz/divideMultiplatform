package com.ragl.divide.ui.screens.groupPaymentProperties

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.utils.DivideTextField
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.amount
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.from
import dividemultiplatform.composeapp.generated.resources.make_a_payment
import dividemultiplatform.composeapp.generated.resources.to
import dividemultiplatform.composeapp.generated.resources.update
import dividemultiplatform.composeapp.generated.resources.update_payment
import org.jetbrains.compose.resources.stringResource

class GroupPaymentPropertiesScreen(
    private val groupId: String,
    private val paymentId: String? = null,
    private val eventId: String? = null
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = koinScreenModel<GroupPaymentPropertiesViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        LaunchedEffect(groupId, paymentId, eventId) {
            val group = userViewModel.getGroupById(groupId)
            val members = userViewModel.getGroupMembers(groupId)
            val payment = userViewModel.getGroupPaymentById(groupId, paymentId, eventId)
            val event = userViewModel.getEventById(groupId, eventId)
            vm.setGroupAndPayment(group, members, payment, event)
        }

        var fromMenuExpanded by remember { mutableStateOf(false) }
        var toMenuExpanded by remember { mutableStateOf(false) }

        val sortedMembers = remember(vm.members) {
            vm.members.sortedWith(compareBy { it.name.lowercase() })
        }

        LaunchedEffect(vm.from) {
            vm.updateTo(sortedMembers.firstOrNull { it.uuid != vm.from.uuid } ?: UserInfo())
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(if (vm.isUpdate.value) Res.string.update_payment else Res.string.make_a_payment),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Amount field
                DivideTextField(
                    input = vm.amount,
                    keyboardType = KeyboardType.Number,
                    prefix = { Text(text = "$", style = MaterialTheme.typography.bodyMedium) },
                    label = stringResource(Res.string.amount),
                    error = vm.amountError,
                    validate = vm::validateAmount,
                    onValueChange = { input ->
                        if (input.isEmpty()) vm.updateAmount("") else {
                            val formatted = input.replace(",", ".")
                            val parsed = formatted.toDoubleOrNull()
                            parsed?.let {
                                val decimalPart = formatted.substringAfter(".", "")
                                if (decimalPart.length <= 2 && parsed <= 999999.99) {
                                    vm.updateAmount(input)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp).padding(bottom = 20.dp)
                )

                // Solo mostrar selectores si la cantidad es válida
                AnimatedVisibility(
                    vm.amount.toDoubleOrNull() != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        // From field (sender)
                        Text(
                            text = stringResource(Res.string.from),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        ExposedDropdownMenuBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            expanded = fromMenuExpanded,
                            onExpandedChange = { fromMenuExpanded = !fromMenuExpanded }
                        ) {
                            TextField(
                                value = vm.from.name,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                onValueChange = {},
                                singleLine = true,
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                                    .clip(ShapeDefaults.Medium)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = fromMenuExpanded,
                                onDismissRequest = { fromMenuExpanded = false },
                            ) {
                                sortedMembers.forEach { user ->
                                    DropdownMenuItem(
                                        text = { Text(user.name) },
                                        onClick = {
                                            vm.updateFrom(user)
                                            fromMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }

                        // To field (receiver)
                        Text(
                            text = stringResource(Res.string.to),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        ExposedDropdownMenuBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            expanded = toMenuExpanded,
                            onExpandedChange = { toMenuExpanded = !toMenuExpanded }
                        ) {
                            TextField(
                                value = vm.to.name,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                onValueChange = {},
                                singleLine = true,
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                                    .clip(ShapeDefaults.Medium)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = toMenuExpanded,
                                onDismissRequest = { toMenuExpanded = false }
                            ) {
                                // Filtrar la lista para excluir al emisor (from)
                                sortedMembers.filter { user -> user.uuid != vm.from.uuid }
                                    .forEach { user ->
                                        DropdownMenuItem(
                                            text = { Text(user.name) },
                                            onClick = {
                                                vm.updateTo(user)
                                                toMenuExpanded = false
                                            }
                                        )
                                    }
                            }
                        }

                    }
                    Column {
                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                userViewModel.showLoading()
                                vm.savePayment(
                                    onSuccess = { savedPayment ->
                                        userViewModel.saveGroupPayment(groupId, savedPayment)
                                        userViewModel.hideLoading()
                                        navigator.pop()
                                    },
                                    onError = {
                                        userViewModel.hideLoading()
                                        userViewModel.handleError(it)
                                    }
                                )
                            },
                            shape = ShapeDefaults.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    if (vm.isUpdate.value) Res.string.update else Res.string.add,
                                ),
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