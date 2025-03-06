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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.components.TitleRow
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.expense.ExpenseScreen
import com.ragl.divide.ui.screens.expenseProperties.ExpensePropertiesScreen
import com.ragl.divide.ui.screens.signIn.SignInScreen
import com.ragl.divide.ui.utils.FriendItem
import com.ragl.divide.ui.utils.ProfileImage
import com.ragl.divide.ui.utils.formatCurrency
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.Moon
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.UserPlus
import compose.icons.fontawesomeicons.solid.Users
import dividemultiplatform.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class HomeScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userViewModel = koinScreenModel<UserViewModel>()
        val state by userViewModel.state.collectAsState()

        // Use remember with state.friends as key to prevent unnecessary recompositions
        val friends = remember(state.friends) {
            state.friends.values.toList().sortedBy { it.name.lowercase() }
        }
        val expenses = remember(state.user.expenses) {
            state.user.expenses.values.toList().sortedByDescending { it.addedDate }
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
                        BottomBar(
                            tabs = tabs,
                            selectedTabIndex = selectedTabIndex,
                            onItemClick = {
                                selectedTabIndex = it
                            }
                        )
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
                                    //navigator.push(AddFriendScreen(userViewModel))
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
                                            //navigator.push(AddGroupScreen(userViewModel))
                                        },
                                        onExpenseClick = {
                                            navigator.push(ExpenseScreen(state.user.expenses[it] ?: Expense()))
                                        },
                                        onGroupClick = {
                                            //navigator.push(GroupDetailScreen(userViewModel, it))
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
                                        isDarkMode = userViewModel.isDarkMode.value,
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
    private fun ProfileBody(
        modifier: Modifier = Modifier,
        user: User,
        onSignOut: () -> Unit,
        isDarkMode: String?,
        onChangeDarkMode: (Boolean?) -> Unit
    ) {
        val allowNotifications = remember { mutableStateOf(true) }
        var isSignOutDialogVisible by remember { mutableStateOf(false) }
        Column {
            if (isSignOutDialogVisible) {
                AlertDialog(
                    onDismissRequest = { isSignOutDialogVisible = false },
                    confirmButton = {
                        TextButton(onClick = onSignOut) {
                            Text(stringResource(Res.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isSignOutDialogVisible = false }) {
                            Text(stringResource(Res.string.no))
                        }
                    },
                    title = {
                        Text(
                            stringResource(Res.string.sign_out),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            stringResource(Res.string.sign_out_confirmation),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    textContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            TopBar(
                title = stringResource(Res.string.bar_item_profile_text)
            )
            Box(modifier = modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (user.photoUrl.isNotBlank()) {
                        ProfileImage(
                            photoUrl = user.photoUrl,
                            modifier = Modifier.size(52.dp)
                        )
                    } else {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .padding(12.dp)
                                .clip(CircleShape)
                        )
                    }
                    Column {
                        Text(
                            user.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            user.email,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { isSignOutDialogVisible = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            NotificationSetting(allowNotifications)
            Spacer(modifier = Modifier.height(16.dp))
            DarkModeSetting(isDarkMode, onChangeDarkMode)
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    private fun NotificationSetting(allowNotifications: MutableState<Boolean>) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(8.dp),
            onClick = { allowNotifications.value = !allowNotifications.value }
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.allow_notifications),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = allowNotifications.value,
                    onCheckedChange = { allowNotifications.value = it })
            }
        }
    }

    @Composable
    private fun DarkModeSetting(
        isDarkMode: String?,
        onChangeDarkMode: (Boolean?) -> Unit
    ) {
        val isExpanded = remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(8.dp),
            onClick = { isExpanded.value = !isExpanded.value },
        ) {
            Row(
                Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = if (isExpanded.value) 0.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    FontAwesomeIcons.Solid.Moon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.dark_mode),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                    Icon(
                        if (isExpanded.value) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded.value) {
                Column {
                    Row(
                        Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(36.dp))
                        Text(
                            stringResource(Res.string.activated),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = isDarkMode == "true",
                            onClick = { onChangeDarkMode(true) })
                    }
                    Row(
                        Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(36.dp))
                        Text(
                            stringResource(Res.string.deactivated),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = isDarkMode == "false",
                            onClick = { onChangeDarkMode(false) })
                    }
                    Row(
                        Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(36.dp))
                        Text(
                            stringResource(Res.string.system_default),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = isDarkMode == null,
                            onClick = { onChangeDarkMode(null) })
                    }
                }
            }
        }
    }

    @Composable
    fun FriendsBody(
        friends: List<User>
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    TopBar(
                        title = stringResource(Res.string.bar_item_friends_text)
                    )
                }
                if (friends.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.you_have_no_friends),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else items(friends, key = { it.uuid }) { friend ->
                    FriendItem(
                        headline = friend.name,
                        supporting = friend.email,
                        photoUrl = friend.photoUrl,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
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
                TopBar(
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
                                    painter = painterResource(resource = Res.drawable.ic_launcher_foreground),
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
                            painter = painterResource(resource = Res.drawable.ic_launcher_foreground),
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
                Text(
                    text = formatCurrency(expense.amount, "es-MX"),
                    style = MaterialTheme.typography.bodySmall.copy(
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

    @Composable
    private fun TopBar(
        title: String
    ) {
        Box(
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .align(Alignment.Center)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

    }

    @Composable
    private fun BottomBar(
        tabs: List<Pair<StringResource, ImageVector>>,
        selectedTabIndex: Int,
        onItemClick: (Int) -> Unit
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(80.dp)
            ) {
                tabs.forEachIndexed { index, pair ->
                    BottomBarItem(
                        labelStringResource = pair.first,
                        icon = pair.second,
                        selected = selectedTabIndex == index,
                        onItemClick = { onItemClick(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    @Composable
    private fun BottomBarItem(
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        labelStringResource: StringResource,
        icon: ImageVector,
        onItemClick: () -> Unit
    ) {
        Box(modifier = modifier.clickable { onItemClick() }) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.6f
                    ),
                    modifier = Modifier
                        .clip(ShapeDefaults.Medium)
                        .size(24.dp)
                    //.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Text(
                    text = stringResource(labelStringResource),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.6f
                        )
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}