package com.ragl.divide.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.components.NetworkImage
import com.ragl.divide.ui.components.NetworkImageType
import com.ragl.divide.ui.components.TitleRow
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.addFriends.AddFriendsScreen
import com.ragl.divide.ui.screens.expense.ExpenseScreen
import com.ragl.divide.ui.screens.expenseProperties.ExpensePropertiesScreen
import com.ragl.divide.ui.screens.group.GroupScreen
import com.ragl.divide.ui.screens.groupProperties.GroupPropertiesScreen
import com.ragl.divide.ui.screens.signIn.SignInScreen
import com.ragl.divide.ui.utils.Header
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import com.ragl.divide.ui.utils.toTwoDecimals
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.Users
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add_expense
import dividemultiplatform.composeapp.generated.resources.add_group
import dividemultiplatform.composeapp.generated.resources.app_name
import dividemultiplatform.composeapp.generated.resources.bar_item_friends_text
import dividemultiplatform.composeapp.generated.resources.bar_item_home_text
import dividemultiplatform.composeapp.generated.resources.bar_item_profile_text
import dividemultiplatform.composeapp.generated.resources.paid_expenses
import dividemultiplatform.composeapp.generated.resources.pending_expenses
import dividemultiplatform.composeapp.generated.resources.you_have_no_expenses
import dividemultiplatform.composeapp.generated.resources.you_have_no_groups
import dividemultiplatform.composeapp.generated.resources.your_expenses
import dividemultiplatform.composeapp.generated.resources.your_groups
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class HomeScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        val state by userViewModel.state.collectAsState()

        val friends = remember(state.friends) {
            state.friends.values.toList().sortedBy { it.name.lowercase() }
        }
        val expenses = remember(state.user.expenses) {
            state.user.expenses.values.toList().sortedByDescending { it.createdAt }
        }
        val groups = remember(state.groups) {
            state.groups.values.toList().sortedBy { it.name.lowercase() }
        }
        val friendRequests = state.friendRequestsReceived.values.toList()

        val tabs: List<Pair<StringResource, ImageVector>> = listOf(
            Pair(Res.string.bar_item_home_text, FontAwesomeIcons.Solid.DollarSign),
            Pair(Res.string.bar_item_friends_text, FontAwesomeIcons.Solid.Users),
            Pair(Res.string.bar_item_profile_text, FontAwesomeIcons.Solid.User)
        )
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        val pullToRefreshState = rememberPullToRefreshState()
        var pullLoading by remember { mutableStateOf(state.isLoading) }
        LaunchedEffect(state.isLoading) {
            pullLoading = state.isLoading
        }

        val isDarkMode by userViewModel.isDarkMode.collectAsState()

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            }
            AnimatedVisibility(
                visible = !state.isLoading,
                enter = fadeIn(animationSpec = tween(500)),
                exit = ExitTransition.None
            ) {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            tabs.forEachIndexed { index, pair ->
                                NavigationBarItem(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    icon = {
                                        if (pair.first == Res.string.bar_item_friends_text && friendRequests.isNotEmpty()) {
                                            BadgedBox(
                                                badge = {
                                                    Badge {
                                                        Text(
                                                            "${friendRequests.size}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White,
                                                            modifier = Modifier.semantics {
                                                                contentDescription =
                                                                    "${friendRequests.size} friend requests"
                                                            })
                                                    }
                                                },
                                                modifier = Modifier.semantics {
                                                    contentDescription = "Friend requests"
                                                }
                                            ) {
                                                Icon(
                                                    pair.second,
                                                    contentDescription = stringResource(pair.first),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else
                                            Icon(
                                                pair.second,
                                                contentDescription = stringResource(pair.first),
                                                modifier = Modifier.size(24.dp)
                                            )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(pair.first),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                    ) {
                        PullToRefreshBox(
                            isRefreshing = pullLoading,
                            state = pullToRefreshState,
                            onRefresh =
                                userViewModel::getUserData,
                            indicator = {
                                PullToRefreshDefaults.Indicator(
                                    modifier = Modifier.align(Alignment.TopCenter),
                                    state = pullToRefreshState,
                                    isRefreshing = pullLoading,
                                    color = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                AnimatedVisibility(
                                    visible = selectedTabIndex == 0,
                                    enter = fadeIn(animationSpec = tween(300)),
                                    exit = ExitTransition.None
                                ) {
                                    HomeBody(
                                        expenses = expenses,
                                        groups = groups,
                                        onAddExpenseClick = {
                                            navigator.push(ExpensePropertiesScreen())
                                        },
                                        onAddGroupClick = {
                                            if (state.friends.isNotEmpty())
                                                navigator.push(GroupPropertiesScreen())
                                            else userViewModel.handleError("Add friends to create a group.")
                                        },
                                        onExpenseClick = {
                                            navigator.push(ExpenseScreen(it))
                                        },
                                        onGroupClick = {
                                            navigator.push(GroupScreen(it))
                                        }
                                    )
                                }
                                AnimatedVisibility(
                                    visible = selectedTabIndex == 1,
                                    enter = fadeIn(animationSpec = tween(500)),
                                    exit = ExitTransition.None
                                ) {
                                    FriendsBody(
                                        friends = friends,
                                        friendRequestsReceived = friendRequests,
                                        friendRequestsSent = state.friendRequestsSent.values.toList(),
                                        onAddFriendClick = {
                                            navigator.push(AddFriendsScreen())
                                        },
                                        onAcceptFriendRequest = userViewModel::acceptFriendRequest,
                                        onRejectFriendRequest = userViewModel::rejectFriendRequest,
                                        onCancelFriendRequest = userViewModel::cancelFriendRequest
                                    )
                                }
                                AnimatedVisibility(
                                    visible = selectedTabIndex == 2,
                                    enter = fadeIn(animationSpec = tween(500)),
                                    exit = ExitTransition.None
                                ) {
                                    ProfileBody(
                                        user = state.user,
                                        onSignOut = {
                                            userViewModel.signOut {
                                                navigator.replaceAll(SignInScreen())
                                            }
                                        },
                                        isDarkMode = isDarkMode,
                                        onChangeDarkMode = userViewModel::changeDarkMode,
                                        userViewModel = userViewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun HomeBody(
        expenses: List<Expense>,
        groups: List<Group>,
        onAddExpenseClick: () -> Unit,
        onAddGroupClick: () -> Unit,
        onExpenseClick: (String) -> Unit,
        onGroupClick: (String) -> Unit
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Header(
                    title = stringResource(Res.string.app_name)
                )
            }
            item {
                TitleRow(
                    Res.string.add_group,
                    Res.string.your_groups,
                    onAddClick = onAddGroupClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (groups.isEmpty()) {
                item {
                    EmptyStateMessage(message = stringResource(Res.string.you_have_no_groups))
                }
            } else
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        items(groups) { group ->
                            GroupCard(
                                group = group,
                                modifier = Modifier.size(140.dp).clip(ShapeDefaults.Medium)
                            ) { onGroupClick(group.id) }
                        }
                        item {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

            item {
                TitleRow(
                    Res.string.add_expense,
                    Res.string.your_expenses,
                    onAddClick = onAddExpenseClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (expenses.isEmpty()) {
                item {
                    EmptyStateMessage(message = stringResource(Res.string.you_have_no_expenses))
                }
            } else {
                val (paid, unpaid) = expenses.partition { it.paid }
                if (unpaid.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(Res.string.pending_expenses),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    itemsIndexed(unpaid, key = { _, e -> e.id }) { i, expense ->
                        ExpenseCard(
                            expense = expense,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 1.dp)
                                .clip(
                                    if (i == 0) RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 2.dp,
                                        bottomStart = 2.dp
                                    ) else {
                                        if (i == unpaid.lastIndex)
                                            RoundedCornerShape(
                                                topStart = 2.dp,
                                                topEnd = 2.dp,
                                                bottomEnd = 16.dp,
                                                bottomStart = 16.dp
                                            )
                                        else RoundedCornerShape(2.dp)
                                    }
                                )
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    onExpenseClick(expense.id)
                                }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (paid.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(Res.string.paid_expenses),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    itemsIndexed(paid, key = { _, e -> e.id }) { i, expense ->
                        ExpenseCard(
                            expense = expense,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 1.dp)
                                .clip(
                                    if (i == 0) RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 2.dp,
                                        bottomStart = 2.dp
                                    ) else {
                                        if (i == paid.lastIndex)
                                            RoundedCornerShape(
                                                topStart = 2.dp,
                                                topEnd = 2.dp,
                                                bottomEnd = 16.dp,
                                                bottomStart = 16.dp
                                            )
                                        else RoundedCornerShape(2.dp)
                                    }
                                )
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    onExpenseClick(expense.id)
                                }
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    @Composable
    private fun GroupCard(
        group: Group,
        modifier: Modifier = Modifier,
        onGroupClick: () -> Unit
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { onGroupClick() }
        ) {
            // Imagen de fondo
            NetworkImage(
                imageUrl = group.image,
                modifier = Modifier.fillMaxSize(),
                type = NetworkImageType.GROUP
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f)
                            ),
                            startY = 80f,
                        )
                    )
            )

            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            )
        }
    }

    @Composable
    private fun ExpenseCard(
        expense: Expense,
        modifier: Modifier = Modifier
    ) {
        val remainingBalance = remember(expense.amountPaid, expense.amount) {
            (expense.amount - expense.amountPaid).toTwoDecimals()
        }

//        val percentagePaid = remember(expense.amountPaid, expense.amount) {
//            if (expense.amount > 0) {
//                (expense.amountPaid / expense.amount * 100).toInt()
//            } else {
//                0
//            }
//        }

        val paid = expense.paid

        Column(
            modifier = modifier
                .semantics { contentDescription = "Expense: ${expense.title}" }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getCategoryIcon(expense.category),
                    tint = if (paid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onPrimary,
                    contentDescription = expense.category.toString(),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (paid) Color.Gray else MaterialTheme.colorScheme.primary)
                        .padding(12.dp)
                        .size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (paid) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = formatDate(expense.createdAt, "MMM dd"),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
//                        Text(
//                            text = stringResource(Res.string.s_paid, "$percentagePaid%"),
//                            style = MaterialTheme.typography.bodySmall.copy(
//                                color = when {
//                                    paid -> Color.Gray
//                                    percentagePaid == 100 -> MaterialTheme.colorScheme.primary
//                                    percentagePaid >= 50 -> Color(0xFF22BB33) // Verde
//                                    percentagePaid >= 25 -> Color(0xFFFFAA00) // Ámbar
//                                    else -> Color(0xFFFF6666) // Rojo claro
//                                }
//                            ),
//                        )

                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (expense.amountPaid != 0.0 && !paid) {
                        Text(
                            text = formatCurrency(remainingBalance, "es-MX"),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(expense.amount, "es-MX"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = Color.Gray
                            )
                        )
                    } else
                        Text(
                            text = formatCurrency(expense.amount, "es-MX"),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (paid) Color.Gray else MaterialTheme.colorScheme.primary,
                            )
                        )
                }
            }
        }
    }

    @Composable
    private fun EmptyStateMessage(message: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
