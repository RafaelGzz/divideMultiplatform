package com.ragl.divide.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.ui.components.AddPaymentDialog
import com.ragl.divide.ui.components.DeleteDialog
import com.ragl.divide.ui.components.TitleRow
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.expenseProperties.ExpensePropertiesScreen
import com.ragl.divide.ui.utils.Strings
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import com.ragl.divide.ui.utils.toTwoDecimals
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.added_on
import dividemultiplatform.composeapp.generated.resources.delete_expense_confirm
import dividemultiplatform.composeapp.generated.resources.delete_payment_confirm
import dividemultiplatform.composeapp.generated.resources.make_a_payment
import dividemultiplatform.composeapp.generated.resources.movements
import dividemultiplatform.composeapp.generated.resources.no_movements_yet
import dividemultiplatform.composeapp.generated.resources.notes
import dividemultiplatform.composeapp.generated.resources.paid
import dividemultiplatform.composeapp.generated.resources.total
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class ExpenseScreen(private val expenseId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val expenseViewModel = koinScreenModel<ExpenseViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        val strings = koinInject<Strings>()

        // Set the expense in the ViewModel
        LaunchedEffect(Unit) {
            expenseViewModel.setExpense(userViewModel.getExpenseById(expenseId))
        }
        val expense by expenseViewModel.expense.collectAsState()

        var showDeleteDialog by remember { mutableStateOf(false) }
        var showAddPaymentDialog by remember { mutableStateOf(false) }
        var showDeletePaymentDialog by remember { mutableStateOf(false) }
        var selectedPayment by remember { mutableStateOf<Payment?>(null) }

        if (showDeleteDialog) {
            DeleteDialog(
                text = stringResource(Res.string.delete_expense_confirm),
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    userViewModel.showLoading()
                    expenseViewModel.deleteExpense(
                        id = expense.id,
                        onSuccess = {
                            userViewModel.removeExpense(expense.id)
                            userViewModel.hideLoading()
                            showDeleteDialog = false
                            navigator.pop()
                        },
                        onFailure = { error ->
                            userViewModel.hideLoading()
                            userViewModel.handleError(error)
                        }
                    )
                }
            )
        }

        if (showAddPaymentDialog) {
            AddPaymentDialog(
                remainingAmount = expense.amount - expense.amountPaid,
                onDismiss = { showAddPaymentDialog = false },
                onConfirm = { amount ->
                    userViewModel.showLoading()
                    expenseViewModel.addPayment(
                        amount = amount,
                        onSuccess = { savedPayment ->
                            userViewModel.savePayment(expense.id, savedPayment)
                            userViewModel.hideLoading()
                            showAddPaymentDialog = false
                        },
                        onFailure = { error ->
                            userViewModel.hideLoading()
                            userViewModel.handleError(error)
                        },
                        onPaidExpense = {
                            userViewModel.updatePaidExpense(expense.id, true)
                            navigator.pop()
                            userViewModel.handleSuccess("Felicidades! Terminaste de pagar ${expense.title}!")
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
                    userViewModel.showLoading()
                    selectedPayment?.let { payment ->
                        expenseViewModel.deletePayment(
                            paymentId = payment.id,
                            amount = payment.amount,
                            onSuccess = {
                                userViewModel.deletePayment(expense.id, payment.id)
                                userViewModel.hideLoading()
                                showDeletePaymentDialog = false
                                selectedPayment = null
                            },
                            onFailure = { error ->
                                userViewModel.hideLoading()
                                userViewModel.handleError(error)
                            }
                        )
                    }
                }
            )
        }

        ExpenseScreenContent(
            expense = expense,
            onBackClick = { navigator.pop() },
            onEditClick = { navigator.push(ExpensePropertiesScreen(expense.id)) },
            onDeleteClick = { showDeleteDialog = true },
            onAddPaymentClick = {
                if (!expense.paid) showAddPaymentDialog = true
                else userViewModel.handleError(strings.getExpenseAlreadyPaid())
            },
            onDeletePaymentClick = { payment ->
                selectedPayment = payment
                showDeletePaymentDialog = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreenContent(
    expense: Expense,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onDeletePaymentClick: (Payment) -> Unit
) {

    val remainingBalance = remember(expense.amountPaid, expense.amount) {
        (expense.amount - expense.amountPaid).toTwoDecimals()
    }

    val payments = remember(expense.payments) {
        expense.payments.entries.toList().sortedByDescending { it.value.createdAt }
    }

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
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            //verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
            TitleRow(
                labelStringResource = Res.string.movements,
                buttonStringResource = Res.string.make_a_payment,
                onAddClick = onAddPaymentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 10.dp)
            )
            LazyColumn {
                if (expense.payments.isEmpty()) {
                    item {
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
                itemsIndexed(
                    payments,
                    key = { _, entry -> entry.key }) { i, payment ->
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
                                onClick = { onDeletePaymentClick(payment.value) },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete payment"
                                )
                            }
                        },
                        modifier = Modifier
                            .padding(vertical = 1.dp)
                            .clip(
                                if (i == 0) RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomEnd = 2.dp,
                                    bottomStart = 2.dp
                                ) else {
                                    RoundedCornerShape(2.dp)
                                }
                            )
                    )
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
                                        topStart = 2.dp,
                                        topEnd = 2.dp,
                                        bottomEnd = 16.dp,
                                        bottomStart = 16.dp
                                    )
                                )
                        )
                    }
            }

        }
    }
}