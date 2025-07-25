package com.ragl.divide.presentation.screens.eventPaymentProperties

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.presentation.components.AdaptiveFAB
import com.ragl.divide.presentation.components.CollapsedDropdownCard
import com.ragl.divide.presentation.components.DebtInfo
import com.ragl.divide.presentation.components.DivideTextField
import com.ragl.divide.presentation.components.ExpandedDropdownCard
import com.ragl.divide.presentation.components.NetworkImage
import com.ragl.divide.presentation.components.NetworkImageType
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.HandHoldingUsd
import compose.icons.fontawesomeicons.solid.PeopleArrows
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.amount
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.description_optional
import dividemultiplatform.composeapp.generated.resources.from
import dividemultiplatform.composeapp.generated.resources.loan
import dividemultiplatform.composeapp.generated.resources.make_a_payment
import dividemultiplatform.composeapp.generated.resources.payment
import dividemultiplatform.composeapp.generated.resources.to
import dividemultiplatform.composeapp.generated.resources.update
import dividemultiplatform.composeapp.generated.resources.update_payment
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class EventPaymentPropertiesScreen(
    private val groupId: String,
    private val paymentId: String? = null,
    private val eventId: String,
    private val currentDebtInfo: DebtInfo? = null,
) : Screen {
    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
        InternalVoyagerApi::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = koinScreenModel<EventPaymentPropertiesViewModel>()
        val appStateService: AppStateService = koinInject()

        LaunchedEffect(Unit) {
            vm.setGroupAndPayment(groupId, paymentId, eventId, currentDebtInfo)
        }

        var fromDropdownExpanded by remember { mutableStateOf(false) }
        var toDropdownExpanded by remember { mutableStateOf(false) }

        val sortedMembers = remember(vm.members) {
            vm.members.sortedWith(compareBy { it.name.lowercase() })
        }

        LaunchedEffect(vm.from) {
            if (currentDebtInfo == null)
                vm.updateTo(sortedMembers.firstOrNull { it.uuid != vm.from.uuid } ?: UserInfo())
        }

        BackHandler(enabled = fromDropdownExpanded || toDropdownExpanded) {
            if (fromDropdownExpanded) fromDropdownExpanded = false
            if (toDropdownExpanded) toDropdownExpanded = false
        }

        val payment by vm.payment.collectAsState()

        SharedTransitionLayout {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(if (vm.isUpdate) Res.string.update_payment else Res.string.make_a_payment),
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
                },
                floatingActionButton = {
                    AnimatedVisibility(
                        vm.amount.toDoubleOrNull() != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        AdaptiveFAB(
                            onClick = {
                                if (vm.validateAmount().and(vm.validateDescription())) {
                                    appStateService.showLoading()
                                    vm.savePayment(
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
                            contentDescription = stringResource(
                                if (vm.isUpdate) Res.string.update else Res.string.add,
                            ),
                            text = stringResource(
                                if (vm.isUpdate) Res.string.update else Res.string.add,
                            ),
                            modifier = Modifier.imePadding()
                        )
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                        .imePadding()
                ) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Button(
                                onClick = { vm.updateIsLoan(false) },
                                shape = ShapeDefaults.Medium,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (!payment.isLoan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (!payment.isLoan) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f).height(52.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        FontAwesomeIcons.Solid.HandHoldingUsd,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(stringResource(Res.string.payment))
                                }
                            }
                            Button(
                                onClick = { vm.updateIsLoan(true) },
                                shape = ShapeDefaults.Medium,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (payment.isLoan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (payment.isLoan) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f).height(52.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        FontAwesomeIcons.Solid.PeopleArrows,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(stringResource(Res.string.loan))
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            text = stringResource(Res.string.from),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AnimatedVisibility(
                            visible = !fromDropdownExpanded
                        ) {
                            CollapsedDropdownCard(
                                itemContent = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        NetworkImage(
                                            imageUrl = vm.from.photoUrl,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape),
                                            type = NetworkImageType.PROFILE
                                        )
                                        Text(
                                            text = vm.from.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@AnimatedVisibility,
                                contentKey = "from_dropdown",
                                showMenu = currentDebtInfo == null,
                                onClick = {
                                    if (currentDebtInfo == null) fromDropdownExpanded = true
                                },
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(Res.string.to),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AnimatedVisibility(
                            visible = !toDropdownExpanded
                        ) {
                            CollapsedDropdownCard(
                                itemContent = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        NetworkImage(
                                            imageUrl = vm.to.photoUrl,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape),
                                            type = NetworkImageType.PROFILE
                                        )
                                        Text(
                                            text = vm.to.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@AnimatedVisibility,
                                contentKey = "to_dropdown",
                                showMenu = currentDebtInfo == null,
                                onClick = {
                                    if (currentDebtInfo == null) toDropdownExpanded = true
                                },
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                        }
                    }
                    item {
                        DivideTextField(
                            value = vm.amount,
                            keyboardType = KeyboardType.Number,
                            prefix = {
                                Text(
                                    text = "$",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
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
                                .padding(bottom = 20.dp)
                        )
                        DivideTextField(
                            value = vm.description,
                            label = stringResource(Res.string.description_optional),
                            characterLimit = vm.descriptionCharacterLimit,
                            error = vm.descriptionError,
                            singleLine = false,
                            validate = vm::validateDescription,
                            onValueChange = vm::updateDescription,
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .heightIn(max = 200.dp)
                        )
                    }
                }
            }
            // Overlays para los dropdowns expandidos
            AnimatedVisibility(
                visible = fromDropdownExpanded,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                ExpandedDropdownCard(
                    items = sortedMembers,
                    selectedItem = vm.from,
                    title = stringResource(Res.string.from),
                    itemContent = { user, isSelected ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NetworkImage(
                                imageUrl = user.photoUrl,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape),
                                type = NetworkImageType.PROFILE
                            )
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedVisibility,
                    contentKey = "from_dropdown",
                    onDismiss = { fromDropdownExpanded = false },
                    onItemClick = { selectedUser ->
                        vm.updateFrom(selectedUser)
                    }
                )
            }

            AnimatedVisibility(
                visible = toDropdownExpanded,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                ExpandedDropdownCard(
                    items = sortedMembers.filter { it.uuid != vm.from.uuid },
                    selectedItem = vm.to,
                    title = stringResource(Res.string.to),
                    itemContent = { user, isSelected ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NetworkImage(
                                imageUrl = user.photoUrl,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape),
                                type = NetworkImageType.PROFILE
                            )
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedVisibility,
                    contentKey = "to_dropdown",
                    onDismiss = { toDropdownExpanded = false },
                    onItemClick = { selectedUser ->
                        vm.updateTo(selectedUser)
                    }
                )
            }

        }
    }
}