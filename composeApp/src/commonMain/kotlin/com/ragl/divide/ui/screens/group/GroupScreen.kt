package com.ragl.divide.ui.screens.group

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.groupExpense.GroupExpenseScreen
import com.ragl.divide.ui.screens.groupExpenseProperties.GroupExpensePropertiesScreen
import com.ragl.divide.ui.screens.groupPayment.GroupPaymentScreen
import com.ragl.divide.ui.screens.groupPaymentProperties.GroupPaymentPropertiesScreen
import com.ragl.divide.ui.screens.groupProperties.GroupPropertiesScreen
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add_expense
import dividemultiplatform.composeapp.generated.resources.compose_multiplatform
import dividemultiplatform.composeapp.generated.resources.group_no_expenses
import dividemultiplatform.composeapp.generated.resources.make_a_payment
import dividemultiplatform.composeapp.generated.resources.paid_by
import dividemultiplatform.composeapp.generated.resources.pending
import dividemultiplatform.composeapp.generated.resources.settled
import dividemultiplatform.composeapp.generated.resources.x_paid_y
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class GroupScreen(private val groupId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = navigator.koinNavigatorScreenModel<GroupViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        LaunchedEffect(Unit) {
            val group = userViewModel.getGroupById(groupId)
            val uuid = userViewModel.getUUID()
            viewModel.setGroup(group, uuid)
        }

        val groupState by viewModel.group.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        val hasExpenses = remember(groupState.expenses) { groupState.expenses.isNotEmpty() }

        val addExpenseClick = {
            navigator.push(GroupExpensePropertiesScreen(groupId, viewModel.members))
        }

        val addPaymentClick = {
            navigator.push(GroupPaymentPropertiesScreen(groupId, viewModel.members))
        }

        Scaffold(
            topBar = {
                GroupDetailsAppBar(
                    group = groupState,
                    onBackClick = { navigator.pop() },
                    onEditClick = { navigator.push(GroupPropertiesScreen(groupId)) }
                )
            },
            floatingActionButton = {
                if (hasExpenses)
                    CustomFloatingActionButton(
                        fabIcon = Icons.Filled.Add,
                        onAddExpenseClick = addExpenseClick,
                        onAddPaymentClick = addPaymentClick,
                    )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {

                Spacer(modifier = Modifier.height(8.dp))
                if (!hasExpenses) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(Res.string.group_no_expenses),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = addExpenseClick,
                                shape = ShapeDefaults.Medium,
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .height(60.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.add_expense),
                                    maxLines = 1,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                } else {
                    if (!isLoading) {
                        // Mostrar las deudas actuales antes de la lista de gastos
                        CurrentDebtsView(
                            currentDebts = groupState.currentDebts,
                            currentUserId = viewModel.currentUserId,
                            members = viewModel.members
                        )

                        ExpenseListView(
                            expensesAndPayments = viewModel.expensesAndPayments,
                            modifier = Modifier.weight(1f),
                            getPaidByNames = viewModel::getPaidByNames,
                            onExpenseClick = {
                                navigator.push(GroupExpenseScreen(groupId, it, viewModel.members))
                            },
                            onPaymentClick = { paymentId ->
                                navigator.push(
                                    GroupPaymentScreen(
                                        groupId,
                                        paymentId,
                                        viewModel.members
                                    )
                                )
                            },
                            members = viewModel.members
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
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
            navigationIconContentColor = MaterialTheme.colorScheme.primary
        ),
        title = { GroupImageAndTitleRow(group) },
        navigationIcon = {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Edit")
            }
        }
    )
}

@Composable
private fun GroupImageAndTitleRow(group: Group, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        if (group.image.isNotEmpty()) {
            CoilImage(
                imageModel = {
                    group.image
                },
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
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(resource = Res.drawable.compose_multiplatform),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            group.name,
            style = MaterialTheme.typography.titleLarge,
            softWrap = true,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun CustomFloatingActionButton(
    onFabClick: () -> Unit = {},
    fabIcon: ImageVector,
    onAddExpenseClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(dampingRatio = 2f), label = "scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 315f else 0f,
        animationSpec = spring(dampingRatio = 3f), label = "rotation"
    )

    Column {
        // ExpandedBox over the FAB
        Column(
            modifier = Modifier
                .offset(
                    x = animateDpAsState(
                        targetValue = if (isExpanded) 0.dp else 60.dp,
                        animationSpec = spring(dampingRatio = 2f), label = "x"
                    ).value,
                    y = animateDpAsState(
                        targetValue = if (isExpanded) 0.dp else 100.dp,
                        animationSpec = spring(dampingRatio = 2f), label = "y"
                    ).value
                )
                .scale(scale)
        ) {
            // Customize the content of the expanded box as needed
            Button(
                onClick = { onAddExpenseClick() },
                shape = ShapeDefaults.Medium,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier
                    .height(60.dp)
                    .align(Alignment.End)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.add_expense),
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onAddPaymentClick() },
                shape = ShapeDefaults.Medium,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier
                    .height(60.dp)
                    .align(Alignment.End)
            ) {
                Icon(
                    FontAwesomeIcons.Solid.DollarSign,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.make_a_payment),
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
                onFabClick()
                isExpanded = !isExpanded
            },
            shape = ShapeDefaults.Medium,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.End)
        ) {
            Icon(
                imageVector = fabIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
private fun ExpenseListView(
    modifier: Modifier = Modifier,
    expensesAndPayments: List<Any>,
    getPaidByNames: (List<String>) -> String,
    members: List<User>,
    onExpenseClick: (String) -> Unit,
    onPaymentClick: (String) -> Unit
) {
    val expensesByMonth = expensesAndPayments.groupBy {
        when (it) {
            is GroupExpense -> {
                formatDate(it.createdAt, "MMMM yyyy")
            }

            is Payment -> {
                formatDate(it.date, "MMMM yyyy")
            }

            else -> ""
        }
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(expensesByMonth.keys.toList().sorted()) { month ->
            MonthSection(
                month = month,
                expensesAndPayments = expensesByMonth[month] ?: emptyList(),
                getPaidByNames = getPaidByNames,
                members = members,
                onExpenseClick = onExpenseClick,
                onPaymentClick = onPaymentClick
            )
        }
        item {
            Spacer(Modifier.height(70.dp))
        }
    }
}

@Composable
private fun MonthSection(
    month: String,
    expensesAndPayments: List<Any>,
    getPaidByNames: (List<String>) -> String,
    members: List<User>,
    onExpenseClick: (String) -> Unit,
    onPaymentClick: (String) -> Unit
) {
    Column {
        Text(
            text = month,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (expense in expensesAndPayments.sortedByDescending {
                when (it) {
                    is GroupExpense -> it.createdAt
                    is Payment -> it.date
                    else -> null
                }
            }) {
                GroupExpenseItem(
                    expenseOrPayment = expense,
                    getPaidByNames = getPaidByNames,
                    members = members,
                    onExpenseClick = onExpenseClick,
                    onPaymentClick = onPaymentClick
                )
            }
        }
    }
}

@Composable
private fun GroupExpenseItem(
    expenseOrPayment: Any,
    getPaidByNames: (List<String>) -> String,
    members: List<User>,
    onExpenseClick: (String) -> Unit,
    onPaymentClick: (String) -> Unit
) {
    val formattedDate = formatDate(
        if (expenseOrPayment is GroupExpense) expenseOrPayment.createdAt else (expenseOrPayment as Payment).date,
        "MMM\ndd"
    )

    val isSettled = when (expenseOrPayment) {
        is GroupExpense -> expenseOrPayment.settled ?: false
        is Payment -> expenseOrPayment.settled ?: false
        else -> false
    }

    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .clip(ShapeDefaults.Medium)
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .clickable {
                when (expenseOrPayment) {
                    is GroupExpense -> onExpenseClick(expenseOrPayment.id)
                    is Payment -> onPaymentClick(expenseOrPayment.id)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            formattedDate,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(start = 16.dp)
        )

        Icon(
            if (expenseOrPayment is GroupExpense) getCategoryIcon(expenseOrPayment.category) else FontAwesomeIcons.Solid.DollarSign,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            when (expenseOrPayment) {
                is GroupExpense -> {
                    Text(
                        text = expenseOrPayment.title,
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Normal
                        ),
                    )

                    Text(
                        text = stringResource(Res.string.paid_by) + " " + getPaidByNames(
                            expenseOrPayment.payers.keys.toList()
                        ),
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Normal
                        ),
                    )
                }

                is Payment -> {
                    Text(
                        stringResource(Res.string.x_paid_y, members.find { it.uuid == expenseOrPayment.from }?.name!!,  members.find { it.uuid == expenseOrPayment.to }?.name!!),
                        //"${members.find { it.uuid == expenseOrPayment.from }?.name} paid ${members.find { it.uuid == expenseOrPayment.to }?.name}",
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Normal
                        ),
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Text(
                text = formatCurrency(
                    when (expenseOrPayment) {
                        is GroupExpense -> expenseOrPayment.amount
                        is Payment -> expenseOrPayment.amount
                        else -> 0.0
                    },
                    "es-MX"
                ),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Normal
                ),
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            if (expenseOrPayment is GroupExpense) {
                if (isSettled) {
                    Text(
                        text = stringResource(Res.string.settled),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = ShapeDefaults.Small
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.pending),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                shape = ShapeDefaults.Small
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentDebtsView(
    currentDebts: Map<String, Map<String, Double>>,
    currentUserId: String,
    members: List<User>,
    modifier: Modifier = Modifier
) {
    if (currentDebts.isEmpty()) return

    // Filtrar deudas del usuario actual
    val userDebts = currentDebts[currentUserId] ?: emptyMap()
    val userOwed = currentDebts.filter { it.key != currentUserId }
        .mapValues { it.value[currentUserId] ?: 0.0 }
        .filter { it.value > 0.01 }

    if (userDebts.isEmpty() && userOwed.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Si el usuario debe dinero a alguien
        userDebts.forEach { (toUserId, amount) ->
            if (amount > 0.01) {
                val toUser = members.find { it.uuid == toUserId }
                Text(
                    buildAnnotatedString {
                        append("Debes ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(formatCurrency(amount, "es-MX"))
                        }
                        append(" a ${toUser?.name ?: "Desconocido"}")
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Si alguien debe dinero al usuario
        userOwed.forEach { (fromUserId, amount) ->
            val fromUser = members.find { it.uuid == fromUserId }
            Text(
                buildAnnotatedString {
                    append("${fromUser?.name ?: "Desconocido"} te debe ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(formatCurrency(amount, "es-MX"))
                    }
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
