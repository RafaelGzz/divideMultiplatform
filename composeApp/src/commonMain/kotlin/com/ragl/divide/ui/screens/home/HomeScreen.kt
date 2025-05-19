package com.ragl.divide.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.ragl.divide.ui.utils.toTwoDecimals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.UserPlus
import compose.icons.fontawesomeicons.solid.Users
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.add_friends
import dividemultiplatform.composeapp.generated.resources.app_name
import dividemultiplatform.composeapp.generated.resources.bar_item_friends_text
import dividemultiplatform.composeapp.generated.resources.bar_item_home_text
import dividemultiplatform.composeapp.generated.resources.bar_item_profile_text
import dividemultiplatform.composeapp.generated.resources.compose_multiplatform
import dividemultiplatform.composeapp.generated.resources.you_have_no_expenses
import dividemultiplatform.composeapp.generated.resources.you_have_no_groups
import dividemultiplatform.composeapp.generated.resources.your_expenses
import dividemultiplatform.composeapp.generated.resources.your_groups
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class HomeScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        val state by userViewModel.state.collectAsState()

        // Use remember with state.friends as key to prevent unnecessary recompositions
        val friends = remember(state.friends) {
            state.friends.values.toList().sortedBy { it.name.lowercase() }
        }
        val expenses = remember(state.user.expenses) {
            state.user.expenses.values.toList().sortedByDescending { it.createdAt }
        }
        val groups = remember(state.groups) {
            state.groups.values.toList().sortedBy { it.name.lowercase() }
        }

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
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    icon = {
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
                    },
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = selectedTabIndex == 1,
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = ExitTransition.None
                        ) {
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(Res.string.add_friends)) },
                                icon = {
                                    Icon(
                                        FontAwesomeIcons.Solid.UserPlus,
                                        contentDescription = stringResource(Res.string.add_friends),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                onClick = {
                                    navigator.push(AddFriendsScreen())
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                            )
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
                                            navigator.push(GroupPropertiesScreen())
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
                                    FriendsBody(friends = friends)
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
                                        onChangeDarkMode = userViewModel::changeDarkMode
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
                    labelStringResource = Res.string.your_expenses,
                    buttonStringResource = Res.string.add,
                    onAddClick = onAddExpenseClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 20.dp)
                )

                ExpensesRow(
                    expenses = expenses,
                    onExpenseClick = { onExpenseClick(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                TitleRow(
                    labelStringResource = Res.string.your_groups,
                    buttonStringResource = Res.string.add,
                    onAddClick = onAddGroupClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp)
                )
            }
            if (groups.isEmpty())
                item {
                    Text(
                        text = stringResource(Res.string.you_have_no_groups),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            else items(groups, key = { it.id }) { group ->
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .clip(ShapeDefaults.Medium)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onGroupClick(group.id) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (group.image.isNotBlank()) {
                        CoilImage(
                            imageModel = { group.image },
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop
                            ),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            },
                            failure = {
                                Image(
                                    painter = painterResource(resource = Res.drawable.compose_multiplatform),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.Companion
                                        .size(100.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                bottomStart = 16.dp
                                            )
                                        )
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            },
                            modifier = Modifier.Companion
                                .size(100.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                        )
                    } else {
                        Image(
                            painter = painterResource(resource = Res.drawable.compose_multiplatform),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.Companion
                                .size(100.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                        modifier = Modifier.Companion
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }

    }

    @Composable
    private fun ExpensesRow(
        modifier: Modifier = Modifier,
        expenses: List<Expense>,
        onExpenseClick: (String) -> Unit
    ) {
        val unpaidExpenses = expenses.filter { !it.paid }

        if (unpaidExpenses.isEmpty()) {
            EmptyStateMessage(message = stringResource(Res.string.you_have_no_expenses))
        } else {
            LazyRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(Modifier.width(8.dp))
                }
                items(unpaidExpenses, key = { it.id }) { expense ->
                    ExpenseCard(expense = expense, onClick = { onExpenseClick(expense.id) })
                }
                item {
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }

    @Composable
    private fun ExpenseCard(expense: Expense, onClick: () -> Unit) {

        val remainingBalance = remember(expense.amountPaid, expense.amount) {
            (expense.amount - expense.amountPaid).toTwoDecimals()
        }
        Row(
            modifier = Modifier
                .height(80.dp)
                .clip(ShapeDefaults.Medium)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onClick)
                .semantics { contentDescription = "Expense: ${expense.title}" },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                getCategoryIcon(expense.category),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = expense.category.toString(),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(24.dp)
            )
            Column(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .widthIn(max = 120.dp)
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Normal
                    ),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (expense.amountPaid != 0.0) {
                    Text(
                        text = formatCurrency(expense.amount, "es-MX"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = Color.Gray
                        ),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        text = formatCurrency(remainingBalance, "es-MX"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Normal
                        ),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = formatCurrency(expense.amount, "es-MX"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Normal
                        ),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
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