package com.ragl.divide.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.ui.utils.WindowWidthSizeClass
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.getWindowWidthSizeClass
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChevronRight
import compose.icons.fontawesomeicons.solid.EllipsisV
import compose.icons.fontawesomeicons.solid.PeopleArrows
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.debts_summary
import dividemultiplatform.composeapp.generated.resources.event_summary
import dividemultiplatform.composeapp.generated.resources.go_to_event
import dividemultiplatform.composeapp.generated.resources.no_debts
import dividemultiplatform.composeapp.generated.resources.options
import dividemultiplatform.composeapp.generated.resources.owes_to
import dividemultiplatform.composeapp.generated.resources.owes_you
import dividemultiplatform.composeapp.generated.resources.pay_debt
import dividemultiplatform.composeapp.generated.resources.up_to_date
import dividemultiplatform.composeapp.generated.resources.x_debts
import dividemultiplatform.composeapp.generated.resources.you_owe
import dividemultiplatform.composeapp.generated.resources.you_owe_to
import dividemultiplatform.composeapp.generated.resources.youre_owed
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CollapsedDebtsCard(
    debts: List<DebtInfo>,
    isGroup: Boolean = false,
    currentUserId: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userDebts = debts.filter { it.fromUserId == currentUserId }
    val userCredits = debts.filter { it.toUserId == currentUserId }
    val totalDebt = userDebts.sumOf { it.amount }
    val totalCredit = userCredits.sumOf { it.amount }
    val balance = totalCredit - totalDebt

    with(sharedTransitionScope) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "debts_card"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(tween(100)),
                    exit = fadeOut(tween(300)),
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
                .clip(CardDefaults.shape)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isGroup) stringResource(Res.string.event_summary) else stringResource(
                            Res.string.debts_summary
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    if (!isGroup)
                        Text(
                            text = when {
                                balance > 0.01 -> stringResource(
                                    Res.string.youre_owed,
                                    formatCurrency(balance, "es-MX")
                                )

                                balance < -0.01 -> stringResource(
                                    Res.string.you_owe,
                                    formatCurrency(-balance, "es-MX")
                                )

                                else -> stringResource(Res.string.up_to_date)
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = when {
                                    balance > 0.01 -> MaterialTheme.colorScheme.primary
                                    balance < -0.01 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ),
                        )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (debts.isNotEmpty()) {
                        Text(
                            text = stringResource(Res.string.x_debts, debts.size),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ChevronRight,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.no_debts),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExpandedDebtsCard(
    debts: List<DebtInfo>,
    users: List<UserInfo>,
    isGroup: Boolean = false,
    currentUserId: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onDismiss: () -> Unit,
    onPayDebtClicked: (DebtInfo) -> Unit = {},
    onGoToEventClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val size = getWindowWidthSizeClass()
    var showOtherDebts by remember { mutableStateOf(false) }

    // Separar deudas del usuario y otras deudas
    val userDebts = debts.filter { it.fromUserId == currentUserId || it.toUserId == currentUserId }
    val otherDebts = debts.filter { it.fromUserId != currentUserId && it.toUserId != currentUserId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onDismiss() }
            ),
        contentAlignment = Alignment.Center
    ) {
        with(sharedTransitionScope) {
            Card(
                modifier = modifier
                    .let {
                        if (size == WindowWidthSizeClass.Compact) {
                            it.fillMaxWidth()
                        } else {
                            it.width(500.dp)
                        }
                    }
                    .padding(16.dp)
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null,
                        onClick = {}
                    )
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "debts_card"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        enter = fadeIn(tween(100)),
                        exit = fadeOut(tween(300)),
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isGroup) stringResource(Res.string.event_summary) else stringResource(
                            Res.string.debts_summary
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .animateContentSize(
                                animationSpec = tween(300)
                            )
                    ) {
                        // Agrupar todas las deudas por evento
                        val allDebtsByEvent = debts.groupBy { it.eventName }

                        // Separar eventos con deudas del usuario y eventos solo con otras deudas
                        val eventsWithUserDebts = mutableMapOf<String, List<DebtInfo>>()
                        val eventsWithOnlyOtherDebts = mutableMapOf<String, List<DebtInfo>>()

                        allDebtsByEvent.forEach { (eventName, eventDebts) ->
                            val eventUserDebts =
                                eventDebts.filter { it.fromUserId == currentUserId || it.toUserId == currentUserId }
                            val eventOtherDebts =
                                eventDebts.filter { it.fromUserId != currentUserId && it.toUserId != currentUserId }

                            if (eventUserDebts.isNotEmpty()) {
                                eventsWithUserDebts[eventName] = eventDebts
                            } else if (eventOtherDebts.isNotEmpty()) {
                                eventsWithOnlyOtherDebts[eventName] = eventOtherDebts
                            }
                        }

                        if (eventsWithUserDebts.isNotEmpty() || eventsWithOnlyOtherDebts.isNotEmpty()) {
                            // Mostrar eventos con deudas del usuario
                            eventsWithUserDebts.forEach { (eventName, eventDebts) ->
                                val eventUserDebts =
                                    eventDebts.filter { it.fromUserId == currentUserId || it.toUserId == currentUserId }
                                val eventOtherDebts =
                                    eventDebts.filter { it.fromUserId != currentUserId && it.toUserId != currentUserId }

                                // Título del evento
                                item {
                                    Text(
                                        text = eventName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                        ),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                // Deudas del usuario para este evento
                                itemsIndexed(eventUserDebts) { index, debt ->
                                    EventDebtItem(
                                        debt = debt,
                                        users = users,
                                        currentUserId = currentUserId,
                                        showEvent = isGroup,
                                        onPayDebtClicked = onPayDebtClicked,
                                        onGoToEventClicked = onGoToEventClicked,
                                        isLast = index == eventUserDebts.lastIndex,
                                        isFirst = index == 0,
                                        isSingle = eventUserDebts.size == 1,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }

                                // Botón para ver otras deudas de este evento (solo si hay deudas del usuario)
                                if (eventOtherDebts.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    showOtherDebts = !showOtherDebts
                                                }
                                            ) {
                                                Text(
                                                    text = if (showOtherDebts)
                                                        "Ocultar"
                                                    else
                                                        "Ver más deudas (${eventOtherDebts.size})",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }

                                    // Otras deudas con animación
                                    itemsIndexed(eventOtherDebts) { index, debt ->
                                        AnimatedVisibility(
                                            visible = showOtherDebts,
                                        ) {
                                            EventDebtItem(
                                                debt = debt,
                                                users = users,
                                                currentUserId = currentUserId,
                                                showEvent = isGroup,
                                                onPayDebtClicked = onPayDebtClicked,
                                                onGoToEventClicked = onGoToEventClicked,
                                                isLast = index == eventOtherDebts.lastIndex,
                                                isFirst = index == 0,
                                                isSingle = eventOtherDebts.size == 1,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                                                                                        // Label "Estás al día" y botón para ver otros eventos (solo con deudas de otros usuarios)
                            if (eventsWithOnlyOtherDebts.isNotEmpty()) {
                                val totalOtherDebts = eventsWithOnlyOtherDebts.values.flatten().size
                                
                                // Mostrar "Estás al día" solo si no hay deudas del usuario
                                if (eventsWithUserDebts.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.up_to_date),
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = if (eventsWithUserDebts.isNotEmpty()) 16.dp else 0.dp)
                                            .clickable { 
                                                showOtherDebts = !showOtherDebts 
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = if (showOtherDebts) 
                                                    "Ocultar" 
                                                else 
                                                    "Ver otras deudas (${totalOtherDebts})",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }
                                    }
                                }

                                // Mostrar eventos con solo otras deudas
                                eventsWithOnlyOtherDebts.forEach { (eventName, eventDebts) ->
                                    item {
                                        AnimatedVisibility(
                                            visible = showOtherDebts,
                                        ) {
                                            Column {
                                                Text(
                                                    text = eventName,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    ),
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                                eventDebts.forEachIndexed { index, debt ->
                                                    EventDebtItem(
                                                        debt = debt,
                                                        users = users,
                                                        currentUserId = currentUserId,
                                                        showEvent = isGroup,
                                                        onPayDebtClicked = onPayDebtClicked,
                                                        onGoToEventClicked = onGoToEventClicked,
                                                        isLast = index == eventDebts.lastIndex,
                                                        isFirst = index == 0,
                                                        isSingle = eventDebts.size == 1,
                                                        modifier = Modifier.padding(bottom = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Solo mostrar "no debts" si no hay deudas
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(Res.string.no_debts),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDebtItem(
    debt: DebtInfo,
    users: List<UserInfo>,
    currentUserId: String,
    showEvent: Boolean,
    onPayDebtClicked: (DebtInfo) -> Unit,
    onGoToEventClicked: (String) -> Unit,
    isLast: Boolean,
    isFirst: Boolean,
    isSingle: Boolean,
    modifier: Modifier = Modifier
) {
    val fromUser = users.find { it.uuid == debt.fromUserId }
    val toUser = users.find { it.uuid == debt.toUserId }
    val isUserOwing = debt.fromUserId == currentUserId
    val isUserOwed = debt.toUserId == currentUserId
    var showMenu by remember { mutableStateOf(false) }

    val shape = when {
        isSingle -> RoundedCornerShape(16.dp)
        isFirst -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomEnd = 2.dp,
            bottomStart = 2.dp
        )

        isLast -> RoundedCornerShape(
            topStart = 2.dp,
            topEnd = 2.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp
        )

        else -> RoundedCornerShape(2.dp)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserAvatarSmall(if (isUserOwing) toUser else fromUser)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when {
                        isUserOwing -> stringResource(Res.string.you_owe_to, toUser?.name ?: "?")
                        isUserOwed -> stringResource(Res.string.owes_you, fromUser?.name ?: "?")
                        else -> stringResource(
                            Res.string.owes_to,
                            fromUser?.name ?: "?",
                            toUser?.name ?: "?"
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            Text(
                text = formatCurrency(debt.amount, "es-MX"),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = when {
                        isUserOwed -> MaterialTheme.colorScheme.primary
                        isUserOwing -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            )
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.EllipsisV,
                        contentDescription = stringResource(Res.string.options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.PeopleArrows,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(stringResource(Res.string.pay_debt))
                            }
                        },
                        onClick = {
                            onPayDebtClicked(debt)
                            showMenu = false
                        }
                    )
                    if (showEvent) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(stringResource(Res.string.go_to_event))
                                }
                            },
                            onClick = {
                                onGoToEventClicked(debt.eventId)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserAvatarSmall(
    user: UserInfo?,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceDim,
    letterColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (user?.photoUrl?.isNotEmpty() == true) {
            NetworkImage(
                imageUrl = user.photoUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        } else {
            Text(
                text = user?.name?.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = letterColor
                )
            )
        }
    }
}

data class DebtInfo(
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,
    val isCurrentUserInvolved: Boolean,
    val eventName: String,
    val eventId: String
)
