package com.ragl.divide.presentation.screens.eventExpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.SplitMethod
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.presentation.components.FriendItem
import com.ragl.divide.presentation.screens.eventExpenseProperties.EventExpensePropertiesScreen
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.formatCurrency
import com.ragl.divide.presentation.utils.formatDate
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.added_on
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.currency_es_mx
import dividemultiplatform.composeapp.generated.resources.debtors
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.delete_expense_confirm
import dividemultiplatform.composeapp.generated.resources.edit
import dividemultiplatform.composeapp.generated.resources.notes
import dividemultiplatform.composeapp.generated.resources.owes_x
import dividemultiplatform.composeapp.generated.resources.paid_by_text
import dividemultiplatform.composeapp.generated.resources.paid_x
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class EventExpenseScreen(
    private val groupId: String,
    private val expenseId: String,
    private val eventId: String,
    private val isEventSettled: Boolean = false
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EventExpenseViewModel>()
        val strings: Strings = koinInject()
        val appStateService: AppStateService = koinInject()

        LaunchedEffect(Unit) {
            viewModel.setGroupExpense(groupId, expenseId, eventId)
        }

        val groupExpenseState by viewModel.groupExpense.collectAsState()

        val sortedDebtors by viewModel.sortedDebtors.collectAsState()
        val sortedPaidBy by viewModel.sortedPaidBy.collectAsState()

        var isDeleteDialogEnabled by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = groupExpenseState.title) },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        scrolledContainerColor = Color.Transparent,
                        containerColor = Color.Transparent,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (!isEventSettled)
                                    navigator.push(
                                        EventExpensePropertiesScreen(
                                            groupId,
                                            groupExpenseState.id,
                                            eventId
                                        )
                                    )
                                else
                                    appStateService.handleError(strings.getCannotModifyWhileEventSettled())
                            }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(Res.string.edit)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (!isEventSettled)
                                    isDeleteDialogEnabled = true
                                else
                                    appStateService.handleError(strings.getCannotModifyWhileEventSettled())
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.delete)
                            )
                        }
                    }
                )
            }
        ) { pv ->
            if (isDeleteDialogEnabled) {
                AlertDialog(
                    onDismissRequest = { isDeleteDialogEnabled = false },
                    title = {
                        Text(
                            stringResource(Res.string.delete),
                        )
                    },
                    text = {
                        Text(
                            stringResource(Res.string.delete_expense_confirm),
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            isDeleteDialogEnabled = false
                            appStateService.showLoading()
                            viewModel.deleteExpense(
                                groupId = groupId,
                                onSuccess = {
                                    appStateService.hideLoading()
                                    navigator.pop()
                                },
                                onError = {
                                    appStateService.handleError(it)
                                    appStateService.hideLoading()
                                }
                            )
                        }) {
                            Text(stringResource(Res.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isDeleteDialogEnabled = false }) {
                            Text(stringResource(Res.string.cancel))
                        }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .padding(pv)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    ExpenseDetails(groupExpenseState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.paid_by_text),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(sortedPaidBy.toList()) { (member, debt) ->
                    val realDebt = when (groupExpenseState.splitMethod) {
                        SplitMethod.EQUALLY, SplitMethod.CUSTOM -> debt
                        SplitMethod.PERCENTAGES -> debt / 100 * groupExpenseState.amount
                    }
                    FriendItem(
                        headline = member.name,
                        photoUrl = member.photoUrl,
                        trailingContent = {
                            Text(
                                text = stringResource(
                                    Res.string.paid_x,
                                    formatCurrency(
                                        (if (realDebt == 0.0) viewModel.groupExpense.value.amount else realDebt),
                                        stringResource(Res.string.currency_es_mx)
                                    )
                                ),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        modifier = Modifier.padding(bottom = 8.dp).clip(ShapeDefaults.Medium)
                    )
                }
                item {
                    Text(
                        text = stringResource(Res.string.debtors),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                itemsIndexed(sortedDebtors.toList()) { i, (member, debt) ->
                    val realDebt = when (groupExpenseState.splitMethod) {
                        SplitMethod.EQUALLY, SplitMethod.CUSTOM -> debt
                        SplitMethod.PERCENTAGES -> debt / 100 * groupExpenseState.amount
                    }
                    FriendItem(
                        headline = member.name,
                        photoUrl = member.photoUrl,
                        trailingContent = {
                            Text(
                                text = stringResource(
                                    Res.string.owes_x,
                                    formatCurrency(
                                        realDebt,
                                        stringResource(Res.string.currency_es_mx)
                                    )
                                ),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        modifier = Modifier
                            .clip(
                                if (sortedDebtors.size == 1) ShapeDefaults.Medium
                                else if (i == 0) RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 2.dp,
                                    bottomEnd = 2.dp
                                ) else if (i == sortedDebtors.size - 1) RoundedCornerShape(
                                    topStart = 2.dp,
                                    topEnd = 2.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                ) else RoundedCornerShape(2.dp)
                            )
                            .padding(bottom = 2.dp)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ExpenseDetails(expense: EventExpense) {
    Column {
        Text(
            text = formatCurrency(expense.amount, stringResource(Res.string.currency_es_mx)),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(
                Res.string.added_on,
                formatDate(expense.createdAt)
            ),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        if (expense.notes != "") {
            Text(
                text = stringResource(Res.string.notes) + ":",
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
    }
}
