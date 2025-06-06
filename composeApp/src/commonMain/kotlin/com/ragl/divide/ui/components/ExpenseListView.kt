package com.ragl.divide.ui.components

import androidx.compose.foundation.background
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
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import dividemultiplatform.composeapp.generated.resources.Res
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
            is GroupExpense -> {
                formatDate(it.createdAt, "MMMM yyyy")
            }

            is Payment -> {
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
                    is GroupExpense -> it.createdAt
                    is Payment -> it.createdAt
                    else -> null
                }
            }.fastForEachIndexed { i, item ->

                GroupExpenseItem(
                    item = item,
                    getPaidByNames = getPaidByNames,
                    members = members,
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
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable {
                            when (item) {
                                is GroupExpense -> onExpenseClick(item.id)
                                is Payment -> onPaymentClick(item.id)
                            }
                        },
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
        if (item is GroupExpense) item.createdAt else (item as Payment).createdAt,
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
            if (item is GroupExpense) getCategoryIcon(item.category) else FontAwesomeIcons.Solid.DollarSign,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null,
            modifier = Modifier.padding(start = 12.dp).size(24.dp)
        )

        Column(
            modifier = Modifier.fillMaxHeight().weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            when (item) {
                is GroupExpense -> {
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

                is Payment -> {
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
                        is GroupExpense -> item.amount
                        is Payment -> item.amount
                        else -> 0.0
                    }, "es-MX"
                ), style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal
                ), softWrap = true, overflow = TextOverflow.Ellipsis, maxLines = 1
            )

        }
    }
}