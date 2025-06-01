package com.ragl.divide.ui.screens.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.components.EventFABGroup
import com.ragl.divide.ui.components.ExpenseListView
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.eventProperties.EventPropertiesScreen
import com.ragl.divide.ui.screens.groupExpense.GroupExpenseScreen
import com.ragl.divide.ui.screens.groupExpenseProperties.GroupExpensePropertiesScreen
import com.ragl.divide.ui.screens.groupPayment.GroupPaymentScreen
import com.ragl.divide.ui.screens.groupPaymentProperties.GroupPaymentPropertiesScreen
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign

class EventScreen(
    private val groupId: String,
    private val eventId: String
) : Screen {
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

        Scaffold(
            topBar = {
                EventDetailsAppBar(
                    event = eventState,
                    onBackClick = { navigator.pop() },
                    onEditClick = { navigator.push(EventPropertiesScreen(groupId, eventId)) },
                )
            },
            floatingActionButton = {
                EventFABGroup(
                    onAddExpenseClick = {
                        navigator.push(GroupExpensePropertiesScreen(groupId, eventId = eventId))
                    },
                    onAddPaymentClick = {
                        navigator.push(GroupPaymentPropertiesScreen(groupId, eventId = eventId))
                    }
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
                // Información del evento
                EventInfoHeader(event = eventState)

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasExpensesOrPayments) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no hay gastos ni pagos en este evento",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else
                    ExpenseListView(
                        expensesAndPayments = viewModel.expensesAndPayments,
                        getPaidByNames = viewModel::getPaidByNames,
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
                        },
                        members = viewModel.members,
                        currentDebts = eventState.currentDebts,
                        currentUserId = uuid,
                    )
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Settings, contentDescription = "Editar evento")
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
        // Mostrar fecha de creación
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
                text = "Creado: ${formatDate(event.createdAt, "dd MMM yyyy")}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Descripción del evento
        if (event.description.isNotEmpty()) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Estado del evento (liquidado o no)
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
                    text = if (event.settled) "Liquidado" else "Activo",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (event.settled)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondary
                )
            }
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun ExpenseAndPaymentsList(
    expenses: List<GroupExpense>,
    payments: List<Payment>,
    onExpenseClick: (String) -> Unit,
    onPaymentClick: (String) -> Unit,
    members: List<UserInfo>,
    getPaidByNames: (List<String>) -> String
) {
    // Combinar gastos y pagos y ordenar por fecha de creación descendente
    val items = (expenses + payments).sortedByDescending {
        when (it) {
            is GroupExpense -> it.createdAt
            is Payment -> it.createdAt
            else -> 0L
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            when (item) {
                is GroupExpense -> {
                    ExpenseItem(
                        expense = item,
                        onClick = { onExpenseClick(item.id) },
                        members = members,
                        getPaidByNames = getPaidByNames
                    )
                }

                is Payment -> {
                    PaymentItem(
                        payment = item,
                        onClick = { onPaymentClick(item.id) },
                        members = members
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Espacio para el FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: GroupExpense,
    onClick: () -> Unit,
    members: List<UserInfo>,
    getPaidByNames: (List<String>) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de la categoría
        Icon(
            imageVector = getCategoryIcon(expense.category),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    CircleShape
                )
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Pagado por: ${getPaidByNames(expense.payers.keys.toList())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Text(
                text = formatDate(expense.createdAt, "dd MMM yyyy"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            )
        }

        Text(
            text = formatCurrency(expense.amount, "es-MX"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        if (expense.settled) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {}
        }
    }
}

@Composable
private fun PaymentItem(
    payment: Payment,
    onClick: () -> Unit,
    members: List<UserInfo>
) {
    val fromUser = members.find { it.uuid == payment.from }?.name ?: "Usuario"
    val toUser = members.find { it.uuid == payment.to }?.name ?: "Usuario"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de pago
        Icon(
            imageVector = FontAwesomeIcons.Solid.DollarSign,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    CircleShape
                )
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "$fromUser → $toUser",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = formatDate(payment.createdAt, "dd MMM yyyy"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
            )
        }

        Text(
            text = formatCurrency(payment.amount, "es-MX"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        if (payment.settled) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {}
        }
    }
}

@Composable
fun EventFloatingActionButton(
    onAddExpenseClick: () -> Unit,
    onAddPaymentClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End
    ) {
        if (isExpanded) {
            // Botón para Agregar Gasto
            Button(
                onClick = {
                    onAddExpenseClick()
                    isExpanded = false
                },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Gasto")
            }

            // Botón para Realizar Pago
            Button(
                onClick = {
                    onAddPaymentClick()
                    isExpanded = false
                },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    FontAwesomeIcons.Solid.DollarSign,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Realizar Pago")
            }
        }

        // Botón principal
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 