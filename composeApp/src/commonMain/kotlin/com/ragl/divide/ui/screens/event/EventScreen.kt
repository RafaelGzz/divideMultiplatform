package com.ragl.divide.ui.screens.event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.ragl.divide.ui.components.ExpandedDebtsCard
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.eventProperties.EventPropertiesScreen
import com.ragl.divide.ui.screens.groupExpense.GroupExpenseScreen
import com.ragl.divide.ui.screens.groupExpenseProperties.GroupExpensePropertiesScreen
import com.ragl.divide.ui.screens.groupPayment.GroupPaymentScreen
import com.ragl.divide.ui.screens.groupPaymentProperties.GroupPaymentPropertiesScreen
import com.ragl.divide.ui.utils.formatCurrency
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.activity
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.discard
import dividemultiplatform.composeapp.generated.resources.edit
import dividemultiplatform.composeapp.generated.resources.event
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
import dividemultiplatform.composeapp.generated.resources.total_spent
import org.jetbrains.compose.resources.stringResource

class EventScreen(
    private val groupId: String,
    private val eventId: String
) : Screen {
    @OptIn(
        ExperimentalSharedTransitionApi::class,
        InternalVoyagerApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = navigator.koinNavigatorScreenModel<EventViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        LaunchedEffect(eventId) {
            val event = userViewModel.getEventById(groupId, eventId)
            val members = userViewModel.getGroupMembers(groupId)
            viewModel.setEvent(event, members)
            viewModel.setGroupId(groupId)
        }

        val eventState by viewModel.event.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val pullToRefreshState = rememberPullToRefreshState()

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
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            viewModel.refreshEvent(
                                onUpdateEventInState = userViewModel::updateEventInState,
                                onHandleError = userViewModel::handleError,
                                onSuccess = {
                                    // Actualizar los datos locales después del refresh
                                    val refreshedEvent = viewModel.event.value
                                    val members = userViewModel.getGroupMembers(groupId)
                                    viewModel.setEvent(refreshedEvent, members)
                                }
                            )
                        },
                        state = pullToRefreshState,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
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
                                if (!(canReopenEvent && showReopenBanner) && !(canSettleEvent && showSettleBanner))
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
                                            },
                                        )
                                    }
                            }
                            item {
                                Text(
                                    text = stringResource(Res.string.activity),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 4.dp, top = 12.dp)
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
                                                groupId, it, eventId, eventState.settled
                                            )
                                        )
                                    },
                                    onPaymentClick = { paymentId ->
                                        navigator.push(
                                            GroupPaymentScreen(
                                                groupId, paymentId, eventId, eventState.settled
                                            )
                                        )
                                    }
                                )
                            }
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

                // Indicador de progreso circular cuando está refrescando
                if (isRefreshing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDetailsAppBar(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(
                text = stringResource(Res.string.event),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back)
                )
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
    val totalSpent = remember(event.expenses) {
        event.expenses.values.sumOf { it.amount }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(
                        topStart = 12.dp,
                        bottomStart = 12.dp,
                        topEnd = 2.dp,
                        bottomEnd = 2.dp
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Icon(
//                    getCategoryIcon(event.category),
//                    tint = MaterialTheme.colorScheme.primary,
//                    contentDescription = null,
//                    modifier = Modifier.size(20.dp)
//                )
//                Text(
//                    text = event.category.getCategoryName(),
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }
            Text(
                event.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Icon(
//                    FontAwesomeIcons.Solid.Calendar,
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
//                    contentDescription = null,
//                    modifier = Modifier.size(20.dp)
//                )
//                Text(
//                    text = formatDate(event.createdAt, "dd MMM yyyy"),
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(
                        topStart = 2.dp,
                        bottomStart = 2.dp,
                        topEnd = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(Res.string.total_spent),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary.copy(
                        alpha = 0.7f
                    )
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                formatCurrency(totalSpent, "es-MX"),
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary)
            )
        }
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