package com.ragl.divide.presentation.screens.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.ragl.divide.data.models.Payment
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.presentation.components.AdaptiveFAB
import com.ragl.divide.presentation.components.DeleteDialog
import com.ragl.divide.presentation.components.DivideTextField
import com.ragl.divide.presentation.screens.expenseProperties.ExpensePropertiesScreen
import com.ragl.divide.presentation.screens.expenseReminder.ExpenseReminderScreen
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.formatCurrency
import com.ragl.divide.presentation.utils.formatDate
import com.ragl.divide.presentation.utils.toTwoDecimals
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.added_on
import dividemultiplatform.composeapp.generated.resources.amount
import dividemultiplatform.composeapp.generated.resources.amount_must_be_greater_than_0
import dividemultiplatform.composeapp.generated.resources.amount_must_be_less_than_remaining_balance
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.delete_expense_confirm
import dividemultiplatform.composeapp.generated.resources.delete_payment_confirm
import dividemultiplatform.composeapp.generated.resources.make_a_payment
import dividemultiplatform.composeapp.generated.resources.movements
import dividemultiplatform.composeapp.generated.resources.no_movements_yet
import dividemultiplatform.composeapp.generated.resources.notes
import dividemultiplatform.composeapp.generated.resources.paid
import dividemultiplatform.composeapp.generated.resources.remaining_balance
import dividemultiplatform.composeapp.generated.resources.total
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class ExpenseScreen(private val expenseId: String) : Screen {

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
        InternalVoyagerApi::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val expenseViewModel = koinScreenModel<ExpenseViewModel>()
        val appStateService: AppStateService = koinInject()
        val strings: Strings = koinInject()

        LaunchedEffect(Unit) {
            expenseViewModel.setExpenseById(expenseId)
        }
        val expense by expenseViewModel.expense.collectAsState()

        var showDeleteExpenseDialog by remember { mutableStateOf(false) }
        var showAddPaymentDialog by remember { mutableStateOf(false) }
        var showDeletePaymentDialog by remember { mutableStateOf(false) }
        var selectedPayment by remember { mutableStateOf<Payment?>(null) }

        val remainingBalance = remember(expense.amountPaid, expense.amount) {
            (expense.amount - expense.amountPaid).toTwoDecimals()
        }

        val payments = remember(expense.payments) {
            expense.payments.entries.toList().sortedByDescending { it.value.createdAt }
        }

        BackHandler(showDeleteExpenseDialog || showDeletePaymentDialog || showAddPaymentDialog) {
            showDeleteExpenseDialog = false
            showDeletePaymentDialog = false
            showAddPaymentDialog = false
        }

        if (showDeleteExpenseDialog) {
            DeleteDialog(
                text = stringResource(Res.string.delete_expense_confirm),
                onDismiss = { showDeleteExpenseDialog = false },
                onConfirm = {
                    appStateService.showLoading()
                    expenseViewModel.deleteExpense(
                        onSuccess = {
                            showDeleteExpenseDialog = false
                            navigator.pop()
                            appStateService.hideLoading()
                        },
                        onFailure = { error ->
                            appStateService.hideLoading()
                            appStateService.handleError(error)
                        }
                    )
                }
            )
        }

        if (showDeletePaymentDialog && selectedPayment != null) {
            DeleteDialog(
                text = stringResource(Res.string.delete_payment_confirm),
                onDismiss = {
                    showDeletePaymentDialog = false
                    selectedPayment = null
                },
                onConfirm = {
                    appStateService.showLoading()
                    selectedPayment?.let { payment ->
                        expenseViewModel.deletePayment(
                            paymentId = payment.id,
                            amount = payment.amount,
                            onSuccess = {
                                showDeletePaymentDialog = false
                                selectedPayment = null
                                appStateService.hideLoading()
                            },
                            onFailure = { error ->
                                appStateService.hideLoading()
                                appStateService.handleError(error)
                            }
                        )
                    }
                }
            )
        }

        SharedTransitionLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    expense.title,
                                    softWrap = true,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                navigationIconContentColor = MaterialTheme.colorScheme.primary,
                                actionIconContentColor = MaterialTheme.colorScheme.primary
                            ),
                            navigationIcon = {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { navigator.push(ExpenseReminderScreen(expense.id)) }) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Reminders"
                                    )
                                }
                                IconButton(onClick = {
                                    navigator.push(
                                        ExpensePropertiesScreen(
                                            expense.id
                                        )
                                    )
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { showDeleteExpenseDialog = true }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = !showAddPaymentDialog,
                            enter = fadeIn(tween(100)),
                            exit = fadeOut(tween(300))
                        ) {
                            AdaptiveFAB(
                                onClick = {
                                    if (!expense.paid) showAddPaymentDialog = true
                                    else appStateService.handleError(strings.getExpenseAlreadyPaid())
                                },
                                icon = FontAwesomeIcons.Solid.DollarSign,
                                contentDescription = stringResource(Res.string.make_a_payment),
                                containerColor = if (!showAddPaymentDialog) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                text = stringResource(Res.string.make_a_payment),
                                modifier = Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(key = "add_payment_dialog"),
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                        enter = fadeIn(tween(100)),
                                        exit = fadeOut(tween(300)),
                                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                    )
                            )
                        }
                    }
                ) { paddingValues ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Column {
                                if (expense.paid) {
                                    Text(
                                        text = stringResource(Res.string.paid),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (remainingBalance != expense.amount && !expense.paid) {
                                    Text(
                                        text = formatCurrency(expense.amount, "es-MX"),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            textDecoration = TextDecoration.LineThrough,
                                            color = Color.Gray
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = formatCurrency(remainingBalance, "es-MX"),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Text(
                                        text = formatCurrency(expense.amount, "es-MX"),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (expense.notes != "") {
                                    Text(
                                        text = "${stringResource(Res.string.notes)}:",
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    )
                                    Text(
                                        text = expense.notes,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Text(
                                    text = stringResource(
                                        Res.string.added_on,
                                        formatDate(expense.createdAt, "dd MMM yyyy, hh:mm a")
                                    ),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )
                            }
                            Text(
                                text = stringResource(Res.string.movements),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            if (expense.payments.isEmpty()) {
                                Text(
                                    stringResource(Res.string.no_movements_yet),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp)
                                )
                            }
                        }
                        if (expense.payments.isNotEmpty())
                            item {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                    leadingContent = {
                                        Text(
                                            stringResource(Res.string.total),
                                            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                                        )
                                    },
                                    headlineContent = {
                                        Text(
                                            formatCurrency(expense.amountPaid, "es-MX"),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(vertical = 16.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .padding(vertical = 1.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                bottomStart = 2.dp,
                                                bottomEnd = 2.dp,
                                                topStart = 16.dp,
                                                topEnd = 16.dp
                                            )
                                        )
                                )
                            }
                        itemsIndexed(payments, key = { _, entry -> entry.key }) { i, payment ->
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                leadingContent = {
                                    Icon(
                                        FontAwesomeIcons.Solid.DollarSign,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        contentDescription = "Money icon",
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(12.dp)
                                            .size(24.dp)
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        formatCurrency(payment.value.amount, "es-MX"),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        formatDate(payment.value.createdAt, "MMM dd"),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            selectedPayment = payment.value
                                            showDeletePaymentDialog = true
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = stringResource(Res.string.delete)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(vertical = 1.dp)
                                    .clip(
                                        if (i == payments.lastIndex) RoundedCornerShape(
                                            topStart = 2.dp,
                                            topEnd = 2.dp,
                                            bottomEnd = 16.dp,
                                            bottomStart = 16.dp
                                        ) else {
                                            RoundedCornerShape(2.dp)
                                        }
                                    )
                            )
                        }
                        item{
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }

                }
            }

            AnimatedVisibility(
                visible = showAddPaymentDialog,
                enter = fadeIn(tween(100)),
                exit = fadeOut(tween(300))
            ) {
                AddPaymentCard(
                    remainingAmount = expense.amount - expense.amountPaid,
                    onDismiss = { showAddPaymentDialog = false },
                    onConfirm = { amount ->
                        appStateService.showLoading()
                        expenseViewModel.addPayment(
                            amount = amount,
                            onSuccess = {
                                showAddPaymentDialog = false
                                appStateService.hideLoading()
                            },
                            onFailure = { error ->
                                appStateService.hideLoading()
                                appStateService.handleError(error)
                            },
                            onPaidExpense = {
                                navigator.pop()
                                appStateService.handleSuccess(strings.getCongratulations(expense.title))
                            }
                        )
                    },
                    modifier = Modifier
                        .imePadding()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "add_payment_dialog"),
                            animatedVisibilityScope = this@AnimatedVisibility,
                            enter = fadeIn(tween(100)),
                            exit = fadeOut(tween(300)),
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                        )
                )
            }
        }
    }
}


@Composable
fun AddPaymentCard(
    remainingAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }

    val amountLessThanRemainingBalance =
        stringResource(Res.string.amount_must_be_less_than_remaining_balance)
    val amountGreaterThanZero = stringResource(Res.string.amount_must_be_greater_than_0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onDismiss() }
            )
    ) {
        Card(
            modifier = modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.make_a_payment),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                DivideTextField(
                    label = stringResource(Res.string.amount),
                    value = amount,
                    error = amountError,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    prefix = { Text(text = "$", style = MaterialTheme.typography.bodyMedium) },
                    onValueChange = { input ->
                        if (input.isEmpty()) amount = "" else {
                            val formatted = input.replace(",", ".")
                            val parsed = formatted.toDoubleOrNull()
                            parsed?.let {
                                val decimalPart = formatted.substringAfter(".", "")
                                if (decimalPart.length <= 2 && parsed <= 999999.99) {
                                    amount = formatted
                                }
                            }
                        }
                    }
                )

                Text(
                    text = stringResource(
                        Res.string.remaining_balance,
                        formatCurrency(remainingAmount, "es-MX")
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(text = stringResource(Res.string.cancel))
                    }

                    TextButton(
                        onClick = {
                            if (amount.isNotEmpty() && amount.toDouble() > 0) {
                                if (amount.toDouble() <= remainingAmount) {
                                    onConfirm(amount.toDouble())
                                } else {
                                    amountError = amountLessThanRemainingBalance
                                }
                            } else {
                                amountError = amountGreaterThanZero
                            }
                        },
                    ) {
                        Text(text = stringResource(Res.string.add))
                    }
                }
            }
        }
    }
}