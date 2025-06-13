package com.ragl.divide.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.ui.components.AdaptiveFAB
import com.ragl.divide.ui.screens.AppState
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.addFriends.AddFriendsScreen
import com.ragl.divide.ui.screens.expense.ExpenseScreen
import com.ragl.divide.ui.screens.expenseProperties.ExpensePropertiesScreen
import com.ragl.divide.ui.screens.friends.FriendsBody
import com.ragl.divide.ui.screens.friends.FriendsScreen
import com.ragl.divide.ui.screens.group.GroupScreen
import com.ragl.divide.ui.screens.groupProperties.GroupPropertiesScreen
import com.ragl.divide.ui.screens.signIn.SignInScreen
import com.ragl.divide.ui.utils.WindowWidthSizeClass
import com.ragl.divide.ui.utils.getWindowWidthSizeClass
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.Home
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.UserFriends
import compose.icons.fontawesomeicons.solid.UserPlus
import compose.icons.fontawesomeicons.solid.Users
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add_friends
import dividemultiplatform.composeapp.generated.resources.bar_item_1_text
import dividemultiplatform.composeapp.generated.resources.bar_item_2_text
import dividemultiplatform.composeapp.generated.resources.bar_item_3_text
import dividemultiplatform.composeapp.generated.resources.new_expense
import dividemultiplatform.composeapp.generated.resources.new_group
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class MainScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        val state by userViewModel.state.collectAsState()
        val expenses = remember(state.user.expenses) {
            state.user.expenses.values.toList().sortedByDescending { it.createdAt }
        }
        val groups = remember(state.groups) {
            state.groups.values.toList().sortedBy { it.name.lowercase() }
        }
        val friendRequests = state.friendRequestsReceived.values.toList()

        val tabs: List<Pair<StringResource, ImageVector>> = listOf(
            Pair(Res.string.bar_item_1_text, FontAwesomeIcons.Solid.Home),
            Pair(Res.string.bar_item_2_text, FontAwesomeIcons.Solid.UserFriends),
            Pair(Res.string.bar_item_3_text, FontAwesomeIcons.Solid.User)
        )
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        val pullToRefreshState = rememberPullToRefreshState()
        var pullLoading by remember { mutableStateOf(state.isLoading) }
        LaunchedEffect(state.isLoading) {
            pullLoading = state.isLoading
        }

        BackHandler(selectedTabIndex != 0) {
            selectedTabIndex = 0
        }

        val isDarkMode by userViewModel.isDarkMode.collectAsState()
        val windowSizeClass = getWindowWidthSizeClass()

        val friends = remember(state.friends) {
            state.friends.values.toList().sortedBy { it.name.lowercase() }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = !state.isLoading,
                enter = fadeIn(animationSpec = tween(500)),
                exit = ExitTransition.None
            ) {
                when (windowSizeClass) {
                    WindowWidthSizeClass.Compact -> {
                        CompactLayout(
                            tabs = tabs,
                            selectedTabIndex = selectedTabIndex,
                            onTabSelected = { selectedTabIndex = it },
                            pullToRefreshState = pullToRefreshState,
                            pullLoading = pullLoading,
                            onRefresh = userViewModel::getUserData,
                            expenses = expenses,
                            groups = groups,
                            friendRequests = friendRequests,
                            state = state,
                            userViewModel = userViewModel,
                            navigator = navigator,
                            isDarkMode = isDarkMode,
                            friends = friends
                        )
                    }

                    WindowWidthSizeClass.Medium,
                    WindowWidthSizeClass.Expanded -> {
                        ExpandedLayout(
                            tabs = tabs,
                            selectedTabIndex = selectedTabIndex,
                            onTabSelected = { selectedTabIndex = it },
                            pullToRefreshState = pullToRefreshState,
                            pullLoading = pullLoading,
                            onRefresh = userViewModel::getUserData,
                            expenses = expenses,
                            groups = groups,
                            friendRequests = friendRequests,
                            state = state,
                            userViewModel = userViewModel,
                            navigator = navigator,
                            isDarkMode = isDarkMode,
                            friends = friends
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactLayout(
    tabs: List<Pair<StringResource, ImageVector>>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    pullToRefreshState: PullToRefreshState,
    pullLoading: Boolean,
    onRefresh: () -> Unit,
    expenses: List<Expense>,
    groups: List<Group>,
    friendRequests: List<UserInfo>,
    state: AppState,
    userViewModel: UserViewModel,
    navigator: Navigator,
    isDarkMode: String?,
    friends: List<UserInfo>
) {
    Scaffold(
        floatingActionButton = {
            when (selectedTabIndex) {
                0 -> HomeFABGroup(
                    onAddExpenseClick = {
                        navigator.push(ExpensePropertiesScreen())
                    },
                    onAddGroupClick = {
                        navigator.push(GroupPropertiesScreen())
                    }
                )

                1 -> AdaptiveFAB(
                    onClick = {
                        navigator.push(AddFriendsScreen())
                    },
                    icon = FontAwesomeIcons.Solid.UserPlus,
                    contentDescription = stringResource(Res.string.add_friends),
                    text = stringResource(Res.string.add_friends)
                )
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        icon = {
                            if (index == 1) {
                                if (friendRequests.isNotEmpty()) {
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
                                            tab.second,
                                            contentDescription = stringResource(tab.first),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else Icon(
                                    tab.second,
                                    contentDescription = stringResource(tab.first),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else Icon(
                                tab.second,
                                contentDescription = stringResource(tab.first),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(tab.first),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        MainContent(
            modifier = Modifier.padding(paddingValues),
            selectedTabIndex = selectedTabIndex,
            pullToRefreshState = pullToRefreshState,
            pullLoading = pullLoading,
            onRefresh = onRefresh,
            expenses = expenses,
            groups = groups,
            friendRequests = friendRequests,
            state = state,
            userViewModel = userViewModel,
            navigator = navigator,
            isDarkMode = isDarkMode,
            friends = friends
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedLayout(
    tabs: List<Pair<StringResource, ImageVector>>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    pullToRefreshState: PullToRefreshState,
    pullLoading: Boolean,
    onRefresh: () -> Unit,
    expenses: List<Expense>,
    groups: List<Group>,
    friendRequests: List<UserInfo>,
    state: AppState,
    userViewModel: UserViewModel,
    navigator: Navigator,
    isDarkMode: String?,
    friends: List<UserInfo>
) {
    Scaffold(
        floatingActionButton = {
            when (selectedTabIndex) {
                0 -> HomeFABGroup(
                    onAddExpenseClick = {
                        navigator.push(ExpensePropertiesScreen())
                    },
                    onAddGroupClick = {
                        navigator.push(GroupPropertiesScreen())
                    }
                )

                1 -> AdaptiveFAB(
                    onClick = {
                        navigator.push(AddFriendsScreen())
                    },
                    icon = FontAwesomeIcons.Solid.UserPlus,
                    contentDescription = stringResource(Res.string.add_friends),
                    text = stringResource(Res.string.add_friends)
                )
            }
        },
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavigationRail {
                tabs.forEachIndexed { index, tab ->
                    NavigationRailItem(
                        selected = selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        icon = {
                            if (index == 1) {
                                if (friendRequests.isNotEmpty()) {
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
                                            tab.second,
                                            contentDescription = stringResource(tab.first),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else Icon(
                                    tab.second,
                                    contentDescription = stringResource(tab.first),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else Icon(
                                tab.second,
                                contentDescription = stringResource(tab.first),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(tab.first),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            MainContent(
                modifier = Modifier.weight(1f),
                selectedTabIndex = selectedTabIndex,
                pullToRefreshState = pullToRefreshState,
                pullLoading = pullLoading,
                onRefresh = onRefresh,
                expenses = expenses,
                groups = groups,
                friendRequests = friendRequests,
                state = state,
                userViewModel = userViewModel,
                navigator = navigator,
                isDarkMode = isDarkMode,
                friends = friends
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    pullToRefreshState: PullToRefreshState,
    pullLoading: Boolean,
    onRefresh: () -> Unit,
    expenses: List<Expense>,
    groups: List<Group>,
    friendRequests: List<UserInfo>,
    state: AppState,
    userViewModel: UserViewModel,
    navigator: Navigator,
    isDarkMode: String?,
    friends: List<UserInfo>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        PullToRefreshBox(
            isRefreshing = pullLoading,
            state = pullToRefreshState,
            onRefresh = onRefresh,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = pullToRefreshState,
                    isRefreshing = pullLoading,
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = selectedTabIndex == 0,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = ExitTransition.None
                ) {
                    HomeContent(
                        expenses = expenses,
                        groups = groups,
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
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = ExitTransition.None
                ) {
                    FriendsBody(
                        friends = friends,
                        friendRequestsReceived = friendRequests,
                        friendRequestsSent = state.friendRequestsSent.values.toList(),
                        onAcceptFriendRequest = userViewModel::acceptFriendRequest,
                        onRejectFriendRequest = userViewModel::rejectFriendRequest,
                        onCancelFriendRequest = userViewModel::cancelFriendRequest,
                        onRemoveFriend = userViewModel::removeFriend
                    )
                }
                AnimatedVisibility(
                    visible = selectedTabIndex == 2,
                    enter = fadeIn(animationSpec = tween(500)),
                    exit = ExitTransition.None
                ) {
                    ProfileContent(
                        user = state.user,
                        onSignOut = {
                            userViewModel.signOut {
                                navigator.replaceAll(SignInScreen())
                            }
                        },
                        isDarkMode = isDarkMode,
                        onChangeDarkMode = userViewModel::changeDarkMode,
                        friendRequests = friendRequests,
                        onFriendsButtonClick = {
                            navigator.push(FriendsScreen())
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeFABGroup(
    fabIcon: ImageVector = Icons.Default.Add,
    onAddExpenseClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    expandedContainerColor: Color = MaterialTheme.colorScheme.secondary,
    expandedContentColor: Color = MaterialTheme.colorScheme.onSecondary,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(dampingRatio = 1f),
        label = "scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 315f else 0f,
        animationSpec = spring(dampingRatio = 3f),
        label = "rotation"
    )

    Column {
        Column(
            modifier = Modifier.offset(
                x = animateDpAsState(
                    targetValue = if (isExpanded) 0.dp else 60.dp,
                    animationSpec = spring(dampingRatio = 1f),
                    label = "x"
                ).value, y = animateDpAsState(
                    targetValue = if (isExpanded) 0.dp else 100.dp,
                    animationSpec = spring(dampingRatio = 1f),
                    label = "y"
                ).value
            ).scale(scale)
        ) {
            Button(
                onClick = { onAddGroupClick() },
                shape = ShapeDefaults.Medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(60.dp).align(Alignment.End)
            ) {
                Icon(
                    FontAwesomeIcons.Solid.Users,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.new_group),
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onAddExpenseClick() },
                shape = ShapeDefaults.Medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(60.dp).align(Alignment.End)
            ) {
                Icon(
                    FontAwesomeIcons.Solid.DollarSign,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.new_expense),
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FloatingActionButton(
            onClick = {
                isExpanded = !isExpanded
            },
            shape = ShapeDefaults.Medium,
            containerColor = if (isExpanded) expandedContainerColor else containerColor,
            contentColor = if (isExpanded) expandedContentColor else contentColor,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = fabIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp).rotate(rotation)
            )
        }
    }
}
