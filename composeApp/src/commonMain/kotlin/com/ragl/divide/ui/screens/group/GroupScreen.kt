package com.ragl.divide.ui.screens.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.components.AdaptiveFAB
import com.ragl.divide.ui.components.CollapsedDebtsCard
import com.ragl.divide.ui.components.DebtInfo
import com.ragl.divide.ui.components.ExpandedDebtsCard
import com.ragl.divide.ui.components.NetworkImage
import com.ragl.divide.ui.components.NetworkImageType
import com.ragl.divide.ui.components.UserAvatarSmall
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.event.EventScreen
import com.ragl.divide.ui.screens.eventProperties.EventPropertiesScreen
import com.ragl.divide.ui.screens.groupPaymentProperties.GroupPaymentPropertiesScreen
import com.ragl.divide.ui.screens.groupProperties.GroupPropertiesScreen
import com.ragl.divide.ui.utils.formatDate
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.CalendarPlus
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.active_events
import dividemultiplatform.composeapp.generated.resources.add_event
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.edit
import dividemultiplatform.composeapp.generated.resources.events
import dividemultiplatform.composeapp.generated.resources.group_members
import dividemultiplatform.composeapp.generated.resources.no_events
import dividemultiplatform.composeapp.generated.resources.settled_events
import org.jetbrains.compose.resources.stringResource

class GroupScreen(
    private val groupId: String
) : Screen {
    @OptIn(ExperimentalSharedTransitionApi::class, InternalVoyagerApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = navigator.koinNavigatorScreenModel<GroupViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        LaunchedEffect(Unit) {
            val group = userViewModel.getGroupById(groupId)
            val members = userViewModel.getGroupMembers(groupId)
            viewModel.setGroup(group, members)
        }

        val uuid = userViewModel.getUUID()
        val groupState by viewModel.group.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val pullToRefreshState = rememberPullToRefreshState()

        val eventDebtsMap = remember(groupState.events) {
            groupState.events.mapValues { (_, event) -> event.currentDebts }
        }

        val allDebts = remember(eventDebtsMap) {
            buildList {
                eventDebtsMap.forEach { (eventId, eventDebts) ->
                    val eventName = groupState.events[eventId]?.title ?: ""

                    eventDebts.forEach { (fromUserId, debts) ->
                        debts.forEach { (toUserId, amount) ->
                            if (amount > 0.01) {
                                add(
                                    DebtInfo(
                                        fromUserId = fromUserId,
                                        toUserId = toUserId,
                                        amount = amount,
                                        isCurrentUserInvolved = fromUserId == uuid || toUserId == uuid,
                                        eventName = eventName,
                                        eventId = eventId
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        val usersWithDebts = remember(allDebts) {
            (allDebts.map { it.fromUserId } + allDebts.map { it.toUserId }).distinct()
        }
        var isDebtsExpanded by remember { mutableStateOf(false) }

        val activeEvents = remember(viewModel.events) {
            viewModel.events.filter { !it.settled }
                .sortedByDescending { it.createdAt }
        }
        val settledEvents = remember(viewModel.events) {
            viewModel.events.filter { it.settled }
                .sortedByDescending { it.createdAt }
        }
        var showSettledEvents by rememberSaveable { mutableStateOf(false) }

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
                        GroupDetailsAppBar(
                            group = groupState,
                            onBackClick = { navigator.pop() },
                            onEditClick = { navigator.push(GroupPropertiesScreen(groupId)) }
                        )
                    },
                    floatingActionButton = {
                        AdaptiveFAB(
                            onClick = { navigator.push(EventPropertiesScreen(groupId)) },
                            icon = FontAwesomeIcons.Regular.CalendarPlus,
                            contentDescription = stringResource(Res.string.add_event),
                            text = stringResource(Res.string.add_event)
                        )
                    }
                ) { paddingValues ->
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            viewModel.refreshGroup(
                                onUpdateGroupInState = userViewModel::updateGroupInState,
                                onHandleError = userViewModel::handleError,
                                onSuccess = {
                                    // Actualizar los datos locales después del refresh
                                    val refreshedGroup = viewModel.group.value
                                    val members = userViewModel.getGroupMembers(groupId)
                                    viewModel.setGroup(refreshedGroup, members)
                                }
                            )
                        },
                        state = pullToRefreshState,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        LazyVerticalStaggeredGrid(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            columns = StaggeredGridCells.Adaptive(150.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp
                        ) {
//                        GroupInfoHeader(
//                            group = groupState,
//                            members = viewModel.members
//                        )
                            item(span = StaggeredGridItemSpan.FullLine ) {
                                AnimatedVisibility(
                                    visible = !isDebtsExpanded
                                ) {
                                    CollapsedDebtsCard(
                                        debts = allDebts,
                                        isGroup = true,
                                        currentUserId = uuid,
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                        onClick = {
                                            if (allDebts.isNotEmpty())
                                                isDebtsExpanded = true
                                        },
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }
                            }

                            item(span = StaggeredGridItemSpan.FullLine ) {
                                Text(
                                    text = stringResource(Res.string.events),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            if (activeEvents.isEmpty() && settledEvents.isEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.no_events),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                items(activeEvents) { event ->
                                    val debt = event.currentDebts[uuid]?.values?.sum() ?: 0.0
                                    val credit = event.currentDebts.values.sumOf { userDebts ->
                                        userDebts[uuid] ?: 0.0
                                    }
                                    EventItem(
                                        event = event,
                                        debt = debt,
                                        credit = credit,
                                        settled = event.settled,
                                        onClick = {
                                            navigator.push(
                                                EventScreen(
                                                    groupId,
                                                    event.id
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (settledEvents.isNotEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    showSettledEvents = !showSettledEvents
                                                },
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                modifier = Modifier.wrapContentWidth()
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center,
                                                ) {
                                                    Icon(
                                                        imageVector = if (showSettledEvents) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = stringResource(Res.string.settled_events, settledEvents.size),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                items(settledEvents) { event ->
                                    val debt = event.currentDebts[uuid]?.values?.sum() ?: 0.0
                                    val credit = event.currentDebts.values.sumOf { userDebts ->
                                        userDebts[uuid] ?: 0.0
                                    }
                                    AnimatedVisibility(
                                        visible = showSettledEvents,
                                        enter = slideInVertically(
                                            animationSpec = tween(350),
                                            initialOffsetY = { -it + (it / 8) * 5 }
                                        ) + fadeIn(tween(350)),
                                        exit = slideOutVertically(
                                            animationSpec = tween(250),
                                            targetOffsetY = { -it - (it / 8) }
                                        ) + fadeOut(tween(100))
                                    ) {
                                        EventItem(
                                            event = event,
                                            debt = debt,
                                            credit = credit,
                                            settled = event.settled,
                                            onClick = {
                                                navigator.push(
                                                    EventScreen(
                                                        groupId,
                                                        event.id
                                                    )
                                                )
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                item(span = StaggeredGridItemSpan.FullLine ) {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }

                            if (allDebts.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = isDebtsExpanded,
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(300))
                    ) {
                        ExpandedDebtsCard(
                            debts = allDebts,
                            isGroup = true,
                            users = usersWithDebts.mapNotNull { userId ->
                                viewModel.members.find { it.uuid == userId }
                            },
                            currentUserId = uuid,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@AnimatedVisibility,
                            onDismiss = { isDebtsExpanded = false },
                            onPayDebtClicked = {
                                navigator.push(EventScreen(groupId, it.eventId))
                                navigator.push(
                                    GroupPaymentPropertiesScreen(
                                        groupId = groupId,
                                        eventId = it.eventId,
                                        currentDebtInfo = it
                                    )
                                )
                            },
                            onGoToEventClicked = {
                                navigator.push(EventScreen(groupId, it))
                            }
                        )
                    }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailsAppBar(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    group: Group
) {
    TopAppBar(
        colors = TopAppBarDefaults.largeTopAppBarColors(
            scrolledContainerColor = Color.Transparent,
            containerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NetworkImage(
                    imageUrl = group.image,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    type = NetworkImageType.GROUP
                )
                Text(
                    group.name,
                    style = MaterialTheme.typography.titleLarge,
                    softWrap = true,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(Res.string.edit))
            }
        }
    )
}

@Composable
private fun EventItem(
    event: GroupEvent,
    settled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    debt: Double = 0.0,
    credit: Double = 0.0,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    settledContainerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    settledContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (settled) settledContainerColor else containerColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = getCategoryIcon(event.category),
                contentDescription = null,
                tint = if (settled) settledContainerColor else containerColor,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (settled) settledContentColor else contentColor,
                        CircleShape
                    )
                    .padding(8.dp)
            )
            if (!settled)
                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    if (credit > 0)
                        Text(
                            text = "+ $$credit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    if (debt > 0)
                        Text(
                            text = "- $$debt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.errorContainer,
                        )
                }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = event.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
            color = if (settled) settledContentColor else contentColor
        )
        Text(
            text = formatDate(event.createdAt, "dd MMM yyyy"),
            style = MaterialTheme.typography.labelSmall,
            color = (if (settled) settledContentColor else contentColor).copy(alpha = 0.5f)
        )

//        if (event.settled) {
//            Text(
//                text = "Liquidado",
//                style = MaterialTheme.typography.bodySmall,
//                color = containerColor,
//                modifier = Modifier
//                    .background(
//                        contentColor.copy(alpha = 0.1f),
//                        RoundedCornerShape(4.dp)
//                    )
//                    .padding(horizontal = 8.dp, vertical = 4.dp)
//            )
//        }
    }
}

@Composable
private fun GroupInfoHeader(
    group: Group,
    members: List<UserInfo>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val activeEventsCount = group.events.values.count { !it.settled }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.active_events, activeEventsCount),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (members.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.group_members) + ":",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    members.take(5).forEach { member ->
                        UserAvatarSmall(
                            user = member,
                            modifier = Modifier.size(32.dp).shadow(2.dp, CircleShape)
                        )
                    }

                    if (members.size > 5) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .shadow(2.dp, CircleShape)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceDim,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${members.size - 5}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            Modifier.padding(vertical = 8.dp),
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
    }
}
