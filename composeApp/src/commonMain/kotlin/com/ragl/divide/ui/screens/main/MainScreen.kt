package com.ragl.divide.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
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
import com.ragl.divide.data.services.AppStateService
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.addFriends.AddFriendsScreen
import com.ragl.divide.ui.screens.expense.ExpenseScreen
import com.ragl.divide.ui.screens.expenseProperties.ExpensePropertiesScreen
import com.ragl.divide.ui.screens.friends.FriendsBody
import com.ragl.divide.ui.screens.group.GroupScreen
import com.ragl.divide.ui.screens.groupProperties.CreateGroupScreen
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
import org.koin.compose.koinInject

class MainScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        val appStateService: AppStateService = koinInject()
        val state by userViewModel.state.collectAsState()
        val isLoading by appStateService.isLoading.collectAsState()
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
        var pullLoading by remember { mutableStateOf(isLoading) }
        LaunchedEffect(isLoading) {
            pullLoading = isLoading
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
                visible = !isLoading,
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
                        navigator.push(CreateGroupScreen())
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
                        navigator.push(CreateGroupScreen())
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
                    enter = fadeIn(animationSpec = tween(300)),
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
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeFABGroup(
    onAddExpenseClick: () -> Unit,
    onAddGroupClick: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        modifier = Modifier.offset(16.dp, 16.dp),
        expanded = isExpanded,
        button = {
            ToggleFloatingActionButton(
                modifier =
                    Modifier.semantics {
                        traversalIndex = -1f
                        stateDescription = if (isExpanded) "Expanded" else "Collapsed"
                        contentDescription = "Toggle menu"
                    },
                checked = isExpanded,
                onCheckedChange = { isExpanded = !isExpanded },
                containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                ),
                containerCornerRadius = ToggleFloatingActionButtonDefaults.containerCornerRadius(
                    12.dp, 56.dp
                ),
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = null,
                    modifier = Modifier.animateIcon(
                        { checkedProgress },
                        ToggleFloatingActionButtonDefaults.iconColor(
                            MaterialTheme.colorScheme.onPrimary,
                            MaterialTheme.colorScheme.onSecondary
                        )
                    ),
                )
            }
        }) {
        FloatingActionButtonMenuItem(
            onClick = {
                onAddGroupClick()
                isExpanded = false
            },
            icon = {
                Icon(
                    FontAwesomeIcons.Solid.Users,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.new_group),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            containerColor = MaterialTheme.colorScheme.primary,
        )
        FloatingActionButtonMenuItem(
            onClick = {
                onAddExpenseClick()
                isExpanded = false
            },
            icon = {
                Icon(
                    FontAwesomeIcons.Solid.DollarSign,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.new_expense),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            containerColor = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)

    )
}
