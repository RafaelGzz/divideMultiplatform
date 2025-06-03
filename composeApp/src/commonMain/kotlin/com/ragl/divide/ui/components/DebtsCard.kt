package com.ragl.divide.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.ui.utils.WindowWidthSizeClass
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.getWindowWidthSizeClass
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowRight
import compose.icons.fontawesomeicons.solid.ChevronRight
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.EllipsisV


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CollapsedDebtsCard(
    debts: List<DebtInfo>,
    isGroup: Boolean = false,
    currentUserId: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit
) {
    val userDebts = debts.filter { it.fromUserId == currentUserId }
    val userCredits = debts.filter { it.toUserId == currentUserId }
    val totalDebt = userDebts.sumOf { it.amount }
    val totalCredit = userCredits.sumOf { it.amount }
    val balance = totalCredit - totalDebt

    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "debts_card"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(tween(100)),
                    exit = fadeOut(tween(300)),
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
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
                        text = if (isGroup) "Resumen de eventos" else "Resumen de deudas",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    if (!isGroup)
                        Text(
                            text = when {
                                balance > 0.01 -> "Te deben ${formatCurrency(balance, "es-MX")}"
                                balance < -0.01 -> "Debes ${formatCurrency(-balance, "es-MX")}"
                                else -> "Estás al día"
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
                    Text(
                        text = "${debts.size} deudas",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ChevronRight,
                        contentDescription = "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
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
    onPayDebt: (DebtInfo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val size = getWindowWidthSizeClass()
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isGroup) "Resumen de eventos" else "Resumen de deudas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val userDebts = debts.filter { it.isCurrentUserInvolved }
                        val otherDebts = debts.filter { !it.isCurrentUserInvolved }

                        if (userDebts.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Tus deudas",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }

                            itemsIndexed(userDebts) { index, debt ->
                                DebtListItem(
                                    debt = debt,
                                    users = users,
                                    showEvent = isGroup,
                                    currentUserId = currentUserId,
                                    isHighlighted = true,
                                    listIndex = index,
                                    isLast = index == userDebts.lastIndex,
                                    onPayDebt = onPayDebt
                                )
                            }

                            if (otherDebts.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Otras deudas",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }

                        itemsIndexed(otherDebts) { index, debt ->
                            DebtListItem(
                                debt = debt,
                                users = users,
                                showEvent = isGroup,
                                currentUserId = currentUserId,
                                isHighlighted = false,
                                listIndex = index,
                                isLast = index == otherDebts.lastIndex,
                                onPayDebt = onPayDebt
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtListItem(
    debt: DebtInfo,
    showEvent: Boolean = false,
    users: List<UserInfo>,
    currentUserId: String,
    isHighlighted: Boolean,
    listIndex: Int,
    isLast: Boolean,
    onPayDebt: (DebtInfo) -> Unit = {}
) {
    val fromUser = users.find { it.uuid == debt.fromUserId }
    val toUser = users.find { it.uuid == debt.toUserId }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape =
            if (listIndex == 0 && !isLast)
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = 2.dp,
                    bottomStart = 2.dp
                )
            else if (listIndex == 0 && isLast)
                RoundedCornerShape(16.dp)
            else if (listIndex != 0 && isLast)
                RoundedCornerShape(
                    topStart = 2.dp,
                    topEnd = 2.dp,
                    bottomEnd = 16.dp,
                    bottomStart = 16.dp
                )
            else
                RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (isHighlighted)
                MaterialTheme.colorScheme.onSecondaryContainer
            else
                MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Origen (avatar from)
            UserAvatarSmall(fromUser)
            
            // Información principal (centro)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (debt.fromUserId == currentUserId) "Tú" else fromUser?.name
                            ?: "?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (debt.fromUserId == currentUserId) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )

                    Text(
                        text = if (debt.toUserId == currentUserId) "Tú" else toUser?.name ?: "?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (debt.toUserId == currentUserId) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.End
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (showEvent)
                    Text(
                        text = debt.eventName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
            }
            
            // Destino (avatar to)
            UserAvatarSmall(toUser)
            
            // Monto
            Text(
                text = formatCurrency(debt.amount, "es-MX"),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = if (debt.fromUserId == currentUserId)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            // Menú de opciones
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.EllipsisV,
                        contentDescription = "Opciones",
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
                                    imageVector = FontAwesomeIcons.Solid.DollarSign,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Pagar deuda")
                            }
                        },
                        onClick = { 
                            onPayDebt(debt)
                            showMenu = false
                        }
                    )
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
