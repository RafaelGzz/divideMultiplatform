package com.ragl.divide.ui.screens.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
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
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import com.ragl.divide.ui.utils.logMessage
import com.ragl.divide.ui.utils.toTwoDecimals
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.added_on
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
import kotlin.math.exp

class ExpenseScreen(private val expense: Expense) : Screen {

    @Composable
    override fun Content() {
        val expenseViewModel = koinScreenModel<ExpenseViewModel>()
        val userViewModel = koinScreenModel<UserViewModel>()
        // Set the expense in the ViewModel
        LaunchedEffect(Unit) {
            expenseViewModel.setExpense(expense)
            logMessage("ExpenseViewmodel", "$expense")
        }
        val expense by expenseViewModel.expense.collectAsState()
        val isLoading by expenseViewModel.isLoading.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        var showDeleteDialog by remember { mutableStateOf(false) }
        var showAddPaymentDialog by remember { mutableStateOf(false) }
        var showDeletePaymentDialog by remember { mutableStateOf(false) }
        var selectedPayment by remember { mutableStateOf<Payment?>(null) }

        if (showDeleteDialog) {
            DeleteDialog(
                text = stringResource(Res.string.delete_expense_confirm),
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    expenseViewModel.deleteExpense(
                        id = expense.id,
                        onSuccess = {
                            showDeleteDialog = false
                            userViewModel.removeExpense(expense.id)
                            navigator.pop()
                        },
                        onFailure = { error ->
                            userViewModel.handleError(Exception(error))
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
                    expenseViewModel.addPayment(
                        amount = amount,
                        onSuccess = { _ ->
                            showAddPaymentDialog = false
                        },
                        onFailure = { error ->
                            userViewModel.handleError(Exception(error))
                        },
                        onPaidExpense = {
                            userViewModel.paidExpense(expense.id)
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
                    selectedPayment?.let { payment ->
                        expenseViewModel.deletePayment(
                            paymentId = payment.id,
                            amount = payment.amount,
                            onSuccess = {
                                showDeletePaymentDialog = false
                                selectedPayment = null
                            },
                            onFailure = { error ->
                                userViewModel.handleError(Exception(error))
                            }
                        )
                    }
                }
            )
        }

        ExpenseScreenContent(
            expense = expense,
            isLoading = isLoading,
            onBackClick = { navigator.pop() },
            onEditClick = { navigator.push(ExpensePropertiesScreen(expense)) },
            onDeleteClick = { showDeleteDialog = true },
            onAddPaymentClick = { showAddPaymentDialog = true },
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
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onDeletePaymentClick: (Payment) -> Unit
) {

    val remainingBalance = remember(expense.amountPaid, expense.amount) {
        (expense.amount - expense.amountPaid).toTwoDecimals()
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
        },
//        floatingActionButton = {
//            if (!expense.paid) {
//                FloatingActionButton(onClick = onAddPaymentClick) {
//                    Icon(Icons.Default.Add, contentDescription = "Add Payment")
//                }
//            }
//        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                //verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = formatCurrency(expense.amount, "es-MX"),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
//                Text(
//                    text = stringResource(
//                        Res.string.s_paid,
//                        formatCurrency(expense.amountPaid)
//                    ),
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.labelMedium,
//                    modifier = Modifier.fillMaxWidth()
//                )
                Text(
                    text = stringResource(
                        Res.string.remaining_balance,
                        formatCurrency(remainingBalance, "es-MX")
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(
                        Res.string.added_on,
                        formatDate(expense.addedDate)
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                if (expense.notes != "") {
                    Text(
                        text = "${stringResource(Res.string.notes)}:",
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                    Text(
                        text = expense.notes,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                    items(
                        expense.payments.entries.toList().sortedBy { it.value.date },
                        key = { it.key }) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            leadingContent = {
                                Text(
                                    stringResource(Res.string.paid),
                                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                                )
                            },
                            headlineContent = {
                                Text(
                                    formatCurrency(it.value.amount, "es-MX")
                                )
                            },
                            supportingContent = {
                                Text(
                                    formatDate(it.value.date),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { onDeletePaymentClick(it.value) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete payment")
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(ShapeDefaults.Medium)
                        )
                    }
                    if (expense.payments.isNotEmpty())
                        item {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                leadingContent = {
                                    Text(
                                        stringResource(Res.string.total),
                                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        formatCurrency(expense.amountPaid, "es-MX"),
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .clip(ShapeDefaults.Medium)
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailsCard(expense: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Total Amount", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = formatCurrency(expense.amount, "es-MX"),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Paid Amount", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = formatCurrency(expense.amountPaid, "es-MX"),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Remaining", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = formatCurrency(expense.amount - expense.amountPaid, "es-MX"),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Category", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = expense.category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (expense.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Notes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = expense.notes, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun PaymentItem(payment: Payment, onDeleteClick: (Payment) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formatCurrency(payment.amount, "es-MX"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${payment.date}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = { onDeleteClick(payment) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Payment")
            }
        }
    }
}