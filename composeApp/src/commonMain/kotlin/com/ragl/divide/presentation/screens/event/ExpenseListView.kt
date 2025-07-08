package com.ragl.divide.presentation.screens.event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.presentation.components.FriendItem
import com.ragl.divide.presentation.utils.formatCurrency
import com.ragl.divide.presentation.utils.formatDate
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.by
import dividemultiplatform.composeapp.generated.resources.paid_by
import dividemultiplatform.composeapp.generated.resources.x_paid_y
import org.jetbrains.compose.resources.stringResource

fun LazyListScope.expenseListView(
    expensesAndPayments: List<Any>,
    getPaidByNames: (List<String>) -> String,
    members: List<UserInfo>,
    onExpenseClick: (String) -> Unit,
    onPaymentClick: (String) -> Unit,
) {
    val expensesByMonth = expensesAndPayments.groupBy {
        when (it) {
            is EventExpense -> {
                formatDate(it.createdAt, "MMMM yyyy")
            }

            is EventPayment -> {
                formatDate(it.createdAt, "MMMM yyyy")
            }

            else -> ""
        }
    }
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

@Composable
private fun MonthSection(
    month: String,
    expensesAndPayments: List<Any>,
    getPaidByNames: (List<String>) -> String,
    members: List<UserInfo>,
    onExpenseClick: (String) -> Unit,
    onPaymentClick: (String) -> Unit
) {
    Column {
        Text(
            text = month,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            expensesAndPayments.sortedByDescending {
                when (it) {
                    is EventExpense -> it.createdAt
                    is EventPayment -> it.createdAt
                    else -> null
                }
            }.fastForEachIndexed { i, item ->
                val formattedDate = formatDate(
                    if (item is EventExpense) item.createdAt else (item as EventPayment).createdAt,
                    "MMM dd"
                )

                val payer = when (item) {
                    is EventExpense -> members.find { it.uuid == item.payers.keys.firstOrNull() }
                    is EventPayment -> members.find { it.uuid == item.from }
                    else -> null
                }

                FriendItem(
                    photoUrl = payer?.photoUrl ?: "",
                    headline = when (item) {
                        is EventExpense -> item.title
                        is EventPayment -> stringResource(
                            Res.string.x_paid_y,
                            members.find { it.uuid == item.from }?.name ?: "",
                            members.find { it.uuid == item.to }?.name ?: ""
                        )

                        else -> ""
                    },
                    trailingContent = {
                        Text(
                            text = formatCurrency(
                                when (item) {
                                    is EventExpense -> item.amount
                                    is EventPayment -> item.amount
                                    else -> 0.0
                                }, "es-MX"
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Normal
                            ),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    },
                    supporting = formattedDate + when (item) {
                        is EventExpense -> " · " + stringResource(Res.string.by) + " " + getPaidByNames(
                            item.payers.keys.toList()
                        )
                        is EventPayment -> ""
                        else -> ""
                    },
                    onClick = when (item) {
                        is EventExpense -> {
                            { onExpenseClick(item.id) }
                        }
                        is EventPayment -> {
                            { onPaymentClick(item.id) }
                        }
                        else -> null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            if (expensesAndPayments.size == 1)
                                RoundedCornerShape(16.dp)
                            else
                                if (i == 0) RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomEnd = 2.dp,
                                    bottomStart = 2.dp
                                ) else {
                                    if (i == expensesAndPayments.lastIndex)
                                        RoundedCornerShape(
                                            topStart = 2.dp,
                                            topEnd = 2.dp,
                                            bottomEnd = 16.dp,
                                            bottomStart = 16.dp
                                        )
                                    else RoundedCornerShape(2.dp)
                                }

                        )
                        .clickable {
                            when (item) {
                                is EventExpense -> onExpenseClick(item.id)
                                is EventPayment -> onPaymentClick(item.id)
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun GroupExpenseItem(
    item: Any,
    getPaidByNames: (List<String>) -> String,
    members: List<UserInfo>,
    modifier: Modifier = Modifier,
) {
    val formattedDate = formatDate(
        if (item is EventExpense) item.createdAt else (item as EventPayment).createdAt,
        "MMM\ndd"
    )

    Row(
        modifier = modifier.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            formattedDate, style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ), modifier = Modifier.padding(start = 16.dp)
        )

        Icon(
            if (item is EventExpense) getCategoryIcon(item.category) else FontAwesomeIcons.Solid.DollarSign,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null,
            modifier = Modifier.padding(start = 12.dp).size(24.dp)
        )

        Column(
            modifier = Modifier.fillMaxHeight().weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            when (item) {
                is EventExpense -> {
                    Text(
                        text = item.title,
                        maxLines = 2,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )

                    Text(
                        text = stringResource(Res.string.paid_by) + " " + getPaidByNames(
                            item.payers.keys.toList()
                        ),
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                    )
                }

                is EventPayment -> {
                    Text(
                        stringResource(
                            Res.string.x_paid_y,
                            members.find { it.uuid == item.from }?.name ?: "",
                            members.find { it.uuid == item.to }?.name ?: ""
                        ),
                        //"${members.find { it.uuid == expenseOrPayment.from }?.name} paid ${members.find { it.uuid == expenseOrPayment.to }?.name}",
                        softWrap = true,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
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
                    when (item) {
                        is EventExpense -> item.amount
                        is EventPayment -> item.amount
                        else -> 0.0
                    }, "es-MX"
                ),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal
                ),
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}