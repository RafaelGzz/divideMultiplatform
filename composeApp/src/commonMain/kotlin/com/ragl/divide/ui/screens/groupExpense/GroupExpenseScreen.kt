package com.ragl.divide.ui.screens.groupExpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.SplitMethod
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.groupExpenseProperties.GroupExpensePropertiesScreen
import com.ragl.divide.ui.utils.FriendItem
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.added_on
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.delete_expense_confirm
import dividemultiplatform.composeapp.generated.resources.notes
import dividemultiplatform.composeapp.generated.resources.owes_x
import dividemultiplatform.composeapp.generated.resources.paid_x
import org.jetbrains.compose.resources.stringResource

class GroupExpenseScreen(
    private val groupId: String,
    private val expenseId: String,
    private val members: List<User>
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<GroupExpenseViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        LaunchedEffect(Unit) {
            val groupExpense = userViewModel.getGroupExpenseById(groupId, expenseId)
            viewModel.setGroupExpense(groupExpense, members)
        }

        val groupExpenseState by viewModel.groupExpense.collectAsState()

        val sortedDebtors = viewModel.sortedDebtors
        val sortedPaidBy = viewModel.sortedPaidBy

        var isDeleteDialogEnabled by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = groupExpenseState.title) },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        scrolledContainerColor = Color.Transparent,
                        containerColor = Color.Transparent,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = {navigator.pop()}
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navigator.push(GroupExpensePropertiesScreen(groupId, members, groupExpenseState.id)) }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(
                            onClick = { isDeleteDialogEnabled = true }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            }
        ) { pv ->
            Column(
                modifier = Modifier
                    .padding(pv)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (isDeleteDialogEnabled) {
                    AlertDialog(
                        onDismissRequest = { isDeleteDialogEnabled = false },
                        title = { Text(stringResource(Res.string.delete), style = MaterialTheme.typography.titleMedium) },
                        text = { Text(stringResource(Res.string.delete_expense_confirm), style = MaterialTheme.typography.bodyMedium) },
                        confirmButton = {
                            TextButton(onClick = {
                                isDeleteDialogEnabled = false
                                viewModel.deleteExpense(groupId, {
                                    userViewModel.removeGroupExpense(groupId, it)
                                    navigator.pop()
                                }) {
                                    userViewModel.handleError(Exception(it))
                                }
                            }) {
                                Text(stringResource(Res.string.delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { isDeleteDialogEnabled = false }) {
                                Text(stringResource(Res.string.cancel))
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        textContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                ExpenseDetails(groupExpenseState)

                Spacer(modifier = Modifier.height(16.dp))

                sortedPaidBy.forEach { (member, debt) ->
                    val realDebt = when(groupExpenseState.splitMethod) {
                        SplitMethod.EQUALLY,SplitMethod.CUSTOM -> debt
                        SplitMethod.PERCENTAGES -> debt / 100 * groupExpenseState.amount
                    }
                    FriendItem(
                        headline = member.name,
                        photoUrl = member.photoUrl,
                        trailingContent = {
                            Text(
                                text = stringResource(
                                    Res.string.paid_x,
                                    formatCurrency(realDebt, "es-MX")
                                ),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                when (groupExpenseState.splitMethod) {
                    SplitMethod.EQUALLY -> {
                        sortedDebtors.map { (member, debt) ->
                            FriendItem(
                                headline = member.name,
                                hasLeadingContent = false,
                                trailingContent = {
                                    Text(
                                        text = stringResource(
                                            Res.string.owes_x,
                                            formatCurrency(debt, "es-MX")
                                        ),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    SplitMethod.PERCENTAGES -> {
                        sortedDebtors.map { (member, percentage) ->
                            val debt = groupExpenseState.amount * (percentage / 100)
                            FriendItem(
                                headline = member.name,
                                hasLeadingContent = false,
                                trailingContent = {
                                    Text(
                                        text = stringResource(
                                            Res.string.owes_x,
                                            formatCurrency(debt, "es-MX")
                                        ),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    SplitMethod.CUSTOM -> {
                        sortedDebtors.map { (member, debt) ->
                            FriendItem(
                                headline = member.name,
                                hasLeadingContent = false,
                                trailingContent = {
                                    Text(
                                        text = stringResource(
                                            Res.string.owes_x,
                                            formatCurrency(debt, "es-MX")
                                        ),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ExpenseDetails(expense: GroupExpense) {
        Text(
            text = formatCurrency(expense.amount, "es-MX"),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
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

    //            Text(
    //                stringResource(Res.string.split_method) + ": " + stringResource(groupExpense.splitMethod.resId),
    //                textAlign = TextAlign.Center,
    //                style = MaterialTheme.typography.bodyLarge,
    //                modifier = Modifier.fillMaxWidth()
    //            )
    //
    //            Spacer(modifier = Modifier.height(16.dp))
    }
}
