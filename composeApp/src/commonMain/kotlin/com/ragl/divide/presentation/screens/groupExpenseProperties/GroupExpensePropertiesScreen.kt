package com.ragl.divide.presentation.screens.groupExpenseProperties

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
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
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.ragl.divide.data.models.SplitMethod
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.presentation.components.AdaptiveFAB
import com.ragl.divide.presentation.components.CollapsedDropdownCard
import com.ragl.divide.presentation.components.DivideTextField
import com.ragl.divide.presentation.components.ExpandedDropdownCard
import com.ragl.divide.presentation.components.FriendItem
import com.ragl.divide.presentation.components.NetworkImage
import com.ragl.divide.presentation.components.NetworkImageType
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.formatCurrency
import com.ragl.divide.presentation.utils.toTwoDecimals
import com.ragl.divide.presentation.utils.validateQuantity
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.Equals
import compose.icons.fontawesomeicons.solid.Percent
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.add_expense
import dividemultiplatform.composeapp.generated.resources.amount
import dividemultiplatform.composeapp.generated.resources.currency_es_mx
import dividemultiplatform.composeapp.generated.resources.dollar_sign
import dividemultiplatform.composeapp.generated.resources.indicate_percentages
import dividemultiplatform.composeapp.generated.resources.indicate_quantities
import dividemultiplatform.composeapp.generated.resources.next
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
    private val eventId: String
) : Screen {
    @OptIn(
        ExperimentalMaterial3Api::class,
        InternalVoyagerApi::class,
        ExperimentalSharedTransitionApi::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = koinScreenModel<GroupExpensePropertiesViewModel>()
        val strings = koinInject<Strings>()
        val appStateService: AppStateService = koinInject()

        var currentStep by remember { mutableStateOf(0) }
        var paidByDropdownExpanded by remember { mutableStateOf(false) }
        var methodDropdownExpanded by remember { mutableStateOf(false) }
        val totalSteps = 3
        val validateCurrentStep: () -> Boolean = {
            when (currentStep) {
                0 -> vm.validateTitle()
                1 -> vm.validateAmount()
                2 -> true
                else -> true
            }
        }

        if (expenseId == null) {
            BackHandler(enabled = currentStep > 0 || paidByDropdownExpanded || methodDropdownExpanded) {
                when {
                    paidByDropdownExpanded -> paidByDropdownExpanded = false
                    methodDropdownExpanded -> methodDropdownExpanded = false
                    currentStep > 0 -> currentStep--
                }
            }
        } else {
            BackHandler(enabled = paidByDropdownExpanded || methodDropdownExpanded) {
                when {
                    paidByDropdownExpanded -> paidByDropdownExpanded = false
                    methodDropdownExpanded -> methodDropdownExpanded = false
                }
            }
        }

        LaunchedEffect(groupId, expenseId, eventId) {

            vm.setGroupAndExpense(groupId, eventId, expenseId)
        }

        val sortedMembers = remember(vm.members) {
            vm.members.sortedWith(compareBy({ it.uuid != vm.userId }, { it.name.lowercase() }))
        }

        SharedTransitionLayout {
            Scaffold(
                topBar = {
                    AppBar(
                        onBackClick = {
                            when {
                                paidByDropdownExpanded -> paidByDropdownExpanded = false
                                methodDropdownExpanded -> methodDropdownExpanded = false
                                expenseId == null && currentStep > 0 -> currentStep--
                                else -> navigator.pop()
                            }
                        },
                        currentStep = if (expenseId == null) currentStep else null,
                        totalSteps = if (expenseId == null) totalSteps else null
                    )
                },
                floatingActionButton = {
                    if (expenseId != null || currentStep == totalSteps - 1) {
                        AdaptiveFAB(
                            onClick = {
                                if (vm.validateTitle().and(vm.validateAmount())) {
                                    when (vm.splitMethod) {
                                        SplitMethod.EQUALLY -> {
                                            if (vm.selectedMembers.size < 2) {
                                                appStateService.handleError(strings.getTwoSelected())
                                                return@AdaptiveFAB
                                            }
                                        }

                                        SplitMethod.PERCENTAGES -> {
                                            if (vm.percentages.values.sum() != 100) {
                                                appStateService.handleError(strings.getPercentagesSum())
                                                return@AdaptiveFAB
                                            } else if (vm.percentages.values.any { it == 100 }) {
                                                appStateService.handleError(strings.getTwoMustPay())
                                                return@AdaptiveFAB
                                            }
                                        }

                                        SplitMethod.CUSTOM -> {
                                            if (vm.quantities.values.any { it == vm.amount.toDouble() }) {
                                                appStateService.handleError(strings.getTwoMustPay())
                                                return@AdaptiveFAB
                                            } else if (vm.quantities.values.sum() != (vm.amount.toDouble())) {
                                                appStateService.handleError(strings.getSumMustBe(vm.amount))
                                                return@AdaptiveFAB
                                            }
                                        }
                                    }
                                    appStateService.showLoading()
                                    vm.saveExpense(
                                        onSuccess = {
                                            appStateService.hideLoading()
                                            navigator.pop()
                                        },
                                        onError = {
                                            appStateService.handleError(it)
                                            appStateService.hideLoading()
                                        }
                                    )
                                }
                            },
                            icon = Icons.Default.Check,
                            contentDescription = if (expenseId == null) stringResource(Res.string.add_expense) else stringResource(
                                Res.string.update_expense
                            ),
                            text = if (expenseId == null) stringResource(Res.string.add) else stringResource(
                                Res.string.update
                            ),
                            modifier = Modifier.imePadding()
                        )
                    }
                }
            ) { paddingValues ->
                Column(
                    Modifier
                        .imePadding()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                ) {
                    if (expenseId == null) {
                        StepByStepLayout(
                            vm = vm,
                            currentStep = currentStep,
                            onNextStep = {
                                if (validateCurrentStep() && currentStep < totalSteps - 1) {
                                    currentStep++
                                }
                            },
                            paidByDropdownExpanded = paidByDropdownExpanded,
                            onPaidByDropdownExpandedChange = { paidByDropdownExpanded = it },
                            methodDropdownExpanded = methodDropdownExpanded,
                            onMethodDropdownExpandedChange = { methodDropdownExpanded = it },
                            sortedMembers = sortedMembers,
                            sharedTransitionScope = this@SharedTransitionLayout
                        )

                    } else {
                        ExistingExpenseLayout(
                            vm = vm,
                            paidByDropdownExpanded = paidByDropdownExpanded,
                            onPaidByDropdownExpandedChange = { paidByDropdownExpanded = it },
                            methodDropdownExpanded = methodDropdownExpanded,
                            onMethodDropdownExpandedChange = { methodDropdownExpanded = it },
                            sortedMembers = sortedMembers,
                            sharedTransitionScope = this@SharedTransitionLayout
                        )
                    }
                }
            }
            // Overlays para los dropdowns expandidos
            AnimatedVisibility(
                visible = paidByDropdownExpanded,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                ExpandedDropdownCard(
                    items = sortedMembers,
                    selectedItem = vm.payer,
                    title = stringResource(Res.string.paid_by),
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
                    contentKey = if (expenseId == null) "paid_by_dropdown_step" else "paid_by_dropdown_existing",
                    onDismiss = { paidByDropdownExpanded = false },
                    onItemClick = { selectedUser ->
                        vm.updatePayer(selectedUser)
                    }
                )
            }

            AnimatedVisibility(
                visible = methodDropdownExpanded,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                ExpandedDropdownCard(
                    items = SplitMethod.entries.toList(),
                    selectedItem = vm.splitMethod,
                    title = stringResource(Res.string.split_method),
                    itemContent = { method, isSelected ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Icon(
                                imageVector = when (method) {
                                    SplitMethod.EQUALLY -> FontAwesomeIcons.Solid.Equals
                                    SplitMethod.PERCENTAGES -> FontAwesomeIcons.Solid.Percent
                                    SplitMethod.CUSTOM -> FontAwesomeIcons.Solid.DollarSign
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(method.resId),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedVisibility,
                    contentKey = if (expenseId == null) "method_dropdown_step" else "method_dropdown_existing",
                    onDismiss = { methodDropdownExpanded = false },
                    onItemClick = { selectedMethod ->
                        vm.updateMethod(selectedMethod)
                        if (selectedMethod == SplitMethod.EQUALLY) {
                            vm.updateAmountPerPerson(
                                if (vm.selectedMembers.isEmpty()) 0.0 else {
                                    ((vm.amount.toDoubleOrNull()
                                        ?: 0.0) / vm.selectedMembers.size).toTwoDecimals()
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    private fun StepByStepLayout(
        vm: GroupExpensePropertiesViewModel,
        currentStep: Int,
        onNextStep: () -> Unit,
        paidByDropdownExpanded: Boolean,
        onPaidByDropdownExpandedChange: (Boolean) -> Unit,
        methodDropdownExpanded: Boolean,
        onMethodDropdownExpandedChange: (Boolean) -> Unit,
        sortedMembers: List<UserInfo>,
        sharedTransitionScope: SharedTransitionScope
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = (currentStep + 1f) / 3f,
            animationSpec = tween(durationMillis = 300),
            label = "progress"
        )

        LazyColumn {
            item {
                Text(
                    text = "Paso ${currentStep + 1} de 3",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }
            item {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(300)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(300)
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(300)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(300)
                            )
                        }
                    },
                    label = "step_content"
                ) { step ->
                    when (step) {
                        0 -> {
                            Column {
                                DivideTextField(
                                    label = stringResource(Res.string.title),
                                    value = vm.title,
                                    error = vm.titleError,
                                    onValueChange = vm::updateTitle,
                                    validate = vm::validateTitle,
                                    modifier = Modifier.padding(bottom = 32.dp)
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Button(
                                    onClick = onNextStep,
                                    shape = ShapeDefaults.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    Text(stringResource(Res.string.next))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }

                        1 -> {
                            Column {
                                DivideTextField(
                                    label = stringResource(Res.string.amount),
                                    keyboardType = KeyboardType.Number,
                                    prefix = {
                                        Text(
                                            text = stringResource(Res.string.dollar_sign),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    value = vm.amount,
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
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )

                                Text(
                                    text = stringResource(Res.string.paid_by),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                AnimatedVisibility(
                                    visible = !paidByDropdownExpanded
                                ) {
                                    CollapsedDropdownCard(
                                        itemContent = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                NetworkImage(
                                                    imageUrl = vm.payer.photoUrl,
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(CircleShape),
                                                    type = NetworkImageType.PROFILE
                                                )
                                                Text(
                                                    text = vm.payer.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        },
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                        contentKey = "paid_by_dropdown_step",
                                        onClick = { onPaidByDropdownExpandedChange(true) },
                                        modifier = Modifier.padding(bottom = 32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Button(
                                    onClick = onNextStep,
                                    shape = ShapeDefaults.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    Text(stringResource(Res.string.next))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }

                        2 -> {
                            // Paso 3: Método de división y selección
                            Column {
                                Text(
                                    text = stringResource(Res.string.split_method),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                AnimatedVisibility(
                                    visible = !methodDropdownExpanded
                                ) {
                                    CollapsedDropdownCard(
                                        itemContent = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = when (vm.splitMethod) {
                                                        SplitMethod.EQUALLY -> FontAwesomeIcons.Solid.Equals
                                                        SplitMethod.PERCENTAGES -> FontAwesomeIcons.Solid.Percent
                                                        SplitMethod.CUSTOM -> FontAwesomeIcons.Solid.DollarSign
                                                    },
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = stringResource(
                                                        vm.splitMethod.resId
                                                    ),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        },
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                        contentKey = "method_dropdown_step",
                                        onClick = { onMethodDropdownExpandedChange(true) },
                                        modifier = Modifier.padding(bottom = 20.dp)
                                    )
                                }
                                Text(
                                    text = when (vm.splitMethod) {
                                        SplitMethod.EQUALLY -> stringResource(Res.string.select_who_pays)
                                        SplitMethod.PERCENTAGES -> stringResource(Res.string.indicate_percentages)
                                        SplitMethod.CUSTOM -> stringResource(Res.string.indicate_quantities)
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
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
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.primary
                                                )
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
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.primary
                                                )
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
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.primary
                                                )
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
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.error
                                                )
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
                                        photoUrl = friend.photoUrl,
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
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
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
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Number
                                                        ),
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
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
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
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Number
                                                        ),
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
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    private fun ExistingExpenseLayout(
        vm: GroupExpensePropertiesViewModel,
        paidByDropdownExpanded: Boolean,
        onPaidByDropdownExpandedChange: (Boolean) -> Unit,
        methodDropdownExpanded: Boolean,
        onMethodDropdownExpandedChange: (Boolean) -> Unit,
        sortedMembers: List<UserInfo>,
        sharedTransitionScope: SharedTransitionScope
    ) {
        LazyColumn {
            item {

                DivideTextField(
                    label = stringResource(Res.string.title),
                    value = vm.title,
                    error = vm.titleError,
                    onValueChange = vm::updateTitle,
                    validate = vm::validateTitle,
                    modifier = Modifier.padding(bottom = 12.dp)
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
                    value = vm.amount,
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
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            item {

                Text(
                    text = stringResource(Res.string.paid_by),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AnimatedVisibility(
                    visible = !paidByDropdownExpanded
                ) {
                    CollapsedDropdownCard(
                        itemContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                NetworkImage(
                                    imageUrl = vm.payer.photoUrl,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape),
                                    type = NetworkImageType.PROFILE
                                )
                                Text(
                                    text = vm.payer.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this@AnimatedVisibility,
                        contentKey = "paid_by_dropdown_existing",
                        onClick = { onPaidByDropdownExpandedChange(true) },
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }
            item {

                AnimatedVisibility(
                    vm.amount.toDoubleOrNull() != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.split_method),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AnimatedVisibility(
                            visible = !methodDropdownExpanded
                        ) {
                            CollapsedDropdownCard(
                                itemContent = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = when ( vm.splitMethod) {
                                                SplitMethod.EQUALLY -> FontAwesomeIcons.Solid.Equals
                                                SplitMethod.PERCENTAGES -> FontAwesomeIcons.Solid.Percent
                                                SplitMethod.CUSTOM -> FontAwesomeIcons.Solid.DollarSign
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = stringResource(
                                                vm.splitMethod.resId
                                            ),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = this@AnimatedVisibility,
                                contentKey = "method_dropdown_existing",
                                onClick = { onMethodDropdownExpandedChange(true) },
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                        }
                        Text(
                            text = when (vm.splitMethod) {
                                SplitMethod.EQUALLY -> stringResource(Res.string.select_who_pays)
                                SplitMethod.PERCENTAGES -> stringResource(Res.string.indicate_percentages)
                                SplitMethod.CUSTOM -> stringResource(Res.string.indicate_quantities)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
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
                            var percentage by remember(friendQuantity) {
                                mutableStateOf(
                                    friendQuantity.toString()
                                )
                            }
                            var quantity = vm.quantities[friend.uuid]?.toString() ?: "0.0"
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
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
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
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
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
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AppBar(
        onBackClick: () -> Unit,
        currentStep: Int? = null,
        totalSteps: Int? = null
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    if (currentStep != null && totalSteps != null) {
                        when (currentStep) {
                            0 -> "Información básica"
                            1 -> "Cantidad y pago"
                            2 -> "División del gasto"
                            else -> stringResource(Res.string.add_expense)
                        }
                    } else {
                        stringResource(if (expenseId == null) Res.string.add_expense else Res.string.update_expense)
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        if (currentStep != null && currentStep > 0) Icons.Filled.ArrowBack else Icons.Filled.Close,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}