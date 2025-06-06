package com.ragl.divide.ui.screens.event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.ui.components.CollapsedDebtsCard
import com.ragl.divide.ui.components.DebtInfo
import com.ragl.divide.ui.components.EventFABGroup
import com.ragl.divide.ui.components.ExpandedDebtsCard
import com.ragl.divide.ui.components.expenseListView
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.eventProperties.EventPropertiesScreen
import com.ragl.divide.ui.screens.groupExpense.GroupExpenseScreen
import com.ragl.divide.ui.screens.groupExpenseProperties.GroupExpensePropertiesScreen
import com.ragl.divide.ui.screens.groupPayment.GroupPaymentScreen
import com.ragl.divide.ui.screens.groupPaymentProperties.GroupPaymentPropertiesScreen
import com.ragl.divide.ui.utils.formatDate
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.active
import dividemultiplatform.composeapp.generated.resources.activity
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.created_on
import dividemultiplatform.composeapp.generated.resources.discard
import dividemultiplatform.composeapp.generated.resources.edit
import dividemultiplatform.composeapp.generated.resources.no_activity
import dividemultiplatform.composeapp.generated.resources.reopen
import dividemultiplatform.composeapp.generated.resources.reopen_banner_message
import dividemultiplatform.composeapp.generated.resources.reopen_banner_title
import dividemultiplatform.composeapp.generated.resources.reopen_event
import dividemultiplatform.composeapp.generated.resources.reopen_event_confirm
import dividemultiplatform.composeapp.generated.resources.settle
import dividemultiplatform.composeapp.generated.resources.settle_banner_message
import dividemultiplatform.composeapp.generated.resources.settle_banner_title
import dividemultiplatform.composeapp.generated.resources.settle_event
import dividemultiplatform.composeapp.generated.resources.settle_event_confirm
import dividemultiplatform.composeapp.generated.resources.settled
import org.jetbrains.compose.resources.stringResource

class EventScreen(
    private val groupId: String,
    private val eventId: String
) : Screen {
    @OptIn(ExperimentalSharedTransitionApi::class, InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = navigator.koinNavigatorScreenModel<EventViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        LaunchedEffect(eventId) {
            val event = userViewModel.getEventById(groupId, eventId)
            val members = userViewModel.getGroupMembers(groupId)
            viewModel.setEvent(event, members)
        }

        val eventState by viewModel.event.collectAsState()
        val hasExpensesOrPayments = remember(eventState) {
            eventState.expenses.isNotEmpty() || eventState.payments.isNotEmpty()
        }
        val uuid = remember(groupId) { userViewModel.getUUID() }

        val allDebts = remember(eventState.currentDebts) {
            buildList {
                eventState.currentDebts.forEach { (fromUserId, debts) ->
                    debts.forEach { (toUserId, amount) ->
                        if (amount > 0.01) {
                            add(
                                DebtInfo(
                                    fromUserId = fromUserId,
                                    toUserId = toUserId,
                                    amount = amount,
                                    isCurrentUserInvolved = fromUserId == uuid || toUserId == uuid,
                                    eventName = eventState.title,
                                    eventId = eventId
                                )
                            )
                        }
                    }
                }
            }
        }
        val usersWithDebts = remember(allDebts) {
            (allDebts.map { it.fromUserId } + allDebts.map { it.toUserId }).distinct()
        }
        var isDebtsExpanded by remember { mutableStateOf(false) }
        var showSettleDialog by remember { mutableStateOf(false) }
        var showReopenDialog by remember { mutableStateOf(false) }
        var showSettleBanner by remember { mutableStateOf(true) }
        var showReopenBanner by remember { mutableStateOf(true) }

        val canSettleEvent = remember(allDebts, eventState.settled, hasExpensesOrPayments) {
            allDebts.isEmpty() && !eventState.settled && hasExpensesOrPayments
        }

        val canReopenEvent = remember(eventState.settled, hasExpensesOrPayments) {
            eventState.settled && hasExpensesOrPayments
        }

        // Resetear banners cuando cambie el estado del evento
        LaunchedEffect(eventState.settled) {
            showSettleBanner = true
            showReopenBanner = true
        }

        BackHandler(enabled = isDebtsExpanded) {
            isDebtsExpanded = false
        }

        SharedTransitionLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Scaffold(
                    topBar = {
                        EventDetailsAppBar(
                            event = eventState,
                            onBackClick = { navigator.pop() },
                            onEditClick = {
                                navigator.push(
                                    EventPropertiesScreen(
                                        groupId,
                                        eventId
                                    )
                                )
                            },
                        )
                    },
                    floatingActionButton = {
                        if (!eventState.settled) {
                            EventFABGroup(
                                onAddExpenseClick = {
                                    navigator.push(
                                        GroupExpensePropertiesScreen(
                                            groupId,
                                            eventId = eventId
                                        )
                                    )
                                },
                                onAddPaymentClick = {
                                    navigator.push(
                                        GroupPaymentPropertiesScreen(
                                            groupId,
                                            eventId = eventId
                                        )
                                    )
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                            .fillMaxSize()
                    ) {
                        item {
                            Column {
                                EventInfoHeader(event = eventState)
                                if (canSettleEvent && showSettleBanner) {
                                    SettleBanner(
                                        onSettleClick = { showSettleDialog = true },
                                        onDismiss = { showSettleBanner = false }
                                    )
                                }
                                if (canReopenEvent && showReopenBanner) {
                                    ReopenBanner(
                                        onReopenClick = { showReopenDialog = true },
                                        onDismiss = { showReopenBanner = false }
                                    )
                                }
                            }
                        }
                        item {
                            AnimatedVisibility(
                                !isDebtsExpanded
                            ) {
                                CollapsedDebtsCard(
                                    debts = allDebts,
                                    currentUserId = uuid,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@AnimatedVisibility,
                                    onClick = {
                                        if (allDebts.isNotEmpty())
                                            isDebtsExpanded = true
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(Res.string.activity),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        if (!hasExpensesOrPayments) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(Res.string.no_activity),
                                        style = MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            expenseListView(
                                expensesAndPayments = viewModel.expensesAndPayments,
                                getPaidByNames = viewModel::getPaidByNames,
                                members = viewModel.members,
                                onExpenseClick = {
                                    navigator.push(
                                        GroupExpenseScreen(
                                            groupId, it, eventId
                                        )
                                    )
                                },
                                onPaymentClick = { paymentId ->
                                    navigator.push(
                                        GroupPaymentScreen(
                                            groupId, paymentId, eventId
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    isDebtsExpanded,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    ExpandedDebtsCard(
                        debts = allDebts,
                        users = usersWithDebts.mapNotNull { userId ->
                            viewModel.members.find { it.uuid == userId }
                        },
                        currentUserId = uuid,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedVisibility,
                        onDismiss = { isDebtsExpanded = false },
                        onPayDebtClicked = { debt ->
                            navigator.push(
                                GroupPaymentPropertiesScreen(
                                    groupId = groupId, eventId = eventId, currentDebtInfo = debt
                                )
                            )
                            isDebtsExpanded = false
                        }
                    )
                }

                // Diálogo de confirmación para liquidar evento
                if (showSettleDialog) {
                    AlertDialog(
                        onDismissRequest = { showSettleDialog = false },
                        title = {
                            Text(stringResource(Res.string.settle_event))
                        },
                        text = {
                            Text(stringResource(Res.string.settle_event_confirm))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.settleEvent(
                                        groupId = groupId,
                                        onSuccess = { userViewModel.settleEvent(groupId, eventId) },
                                        onError = { userViewModel.handleError(it) }
                                    )
                                    showSettleDialog = false
                                }
                            ) {
                                Text(stringResource(Res.string.settle))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showSettleDialog = false }
                            ) {
                                Text(stringResource(Res.string.cancel))
                            }
                        }
                    )
                }

                // Diálogo de confirmación para reabrir evento
                if (showReopenDialog) {
                    AlertDialog(
                        onDismissRequest = { showReopenDialog = false },
                        title = {
                            Text(stringResource(Res.string.reopen_event))
                        },
                        text = {
                            Text(stringResource(Res.string.reopen_event_confirm))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.reopenEvent(
                                        groupId = groupId,
                                        onSuccess = { userViewModel.reopenEvent(groupId, eventId) },
                                        onError = { userViewModel.handleError(it) }
                                    )
                                    showReopenDialog = false
                                }
                            ) {
                                Text(stringResource(Res.string.reopen))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showReopenDialog = false }
                            ) {
                                Text(stringResource(Res.string.cancel))
                            }
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDetailsAppBar(
    event: GroupEvent,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.edit))
            }
        }
    )
}

@Composable
private fun EventInfoHeader(event: GroupEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Badge(
                containerColor = if (event.settled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            ) {
                Text(
                    text = if (event.settled) stringResource(Res.string.settled) else stringResource(Res.string.active),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (event.settled)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.created_on, formatDate(event.createdAt, "dd MMM yyyy")),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (event.description.isNotEmpty()) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        HorizontalDivider(
            Modifier.padding(vertical = 8.dp),
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun SettleBanner(
    onSettleClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .clickable { onSettleClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(Res.string.settle_banner_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = stringResource(Res.string.settle_banner_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(Res.string.discard),
                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ReopenBanner(
    onReopenClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onReopenClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(Res.string.reopen_banner_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = stringResource(Res.string.reopen_banner_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(Res.string.discard),
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}