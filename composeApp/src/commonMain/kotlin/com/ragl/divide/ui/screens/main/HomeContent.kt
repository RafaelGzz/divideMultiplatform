package com.ragl.divide.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.components.NetworkImage
import com.ragl.divide.ui.components.NetworkImageType
import com.ragl.divide.ui.utils.WindowWidthSizeClass
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import com.ragl.divide.ui.utils.getWindowWidthSizeClass
import com.ragl.divide.ui.utils.toTwoDecimals
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.bar_item_1_text
import dividemultiplatform.composeapp.generated.resources.paid_expenses
import dividemultiplatform.composeapp.generated.resources.you_have_no_expenses
import dividemultiplatform.composeapp.generated.resources.you_have_no_groups
import dividemultiplatform.composeapp.generated.resources.your_expenses
import dividemultiplatform.composeapp.generated.resources.your_groups
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeContent(
    expenses: List<Expense>,
    groups: List<Group>,
    onExpenseClick: (String) -> Unit,
    onGroupClick: (String) -> Unit,
) {
    val windowSizeClass = getWindowWidthSizeClass()
    var showPaidExpenses by rememberSaveable { mutableStateOf(false) }
    val (paid, unpaid) = remember(expenses) { expenses.partition { it.paid } }

    // Estados para controlar las animaciones escalonadas
    var showHeader by remember { mutableStateOf(true) }
    var showGroupsRow by remember { mutableStateOf(true) }
    var showExpensesColumn by remember { mutableStateOf(true) }

    // Animaciones con Modifier para mejor rendimiento
    val headerAlpha by animateFloatAsState(
        targetValue = if (showHeader) 1f else 0f,
        animationSpec = tween(300),
        label = "headerAlpha"
    )
    val headerOffsetY by animateIntAsState(
        targetValue = if (showHeader) 0 else -50,
        animationSpec = tween(300),
        label = "headerOffsetY"
    )

    val groupsRowAlpha by animateFloatAsState(
        targetValue = if (showGroupsRow) 1f else 0f,
        animationSpec = tween(350),
        label = "groupsRowAlpha"
    )
    val groupsRowOffsetY by animateIntAsState(
        targetValue = if (showGroupsRow) 0 else -100,
        animationSpec = tween(350),
        label = "groupsRowOffsetY"
    )

    val expensesColumnAlpha by animateFloatAsState(
        targetValue = if (showExpensesColumn) 1f else 0f,
        animationSpec = tween(300),
        label = "expensesColumnAlpha"
    )
    val expensesColumnOffsetY by animateIntAsState(
        targetValue = if (showExpensesColumn) 0 else -50,
        animationSpec = tween(300),
        label = "expensesColumnOffsetY"
    )

    // Efecto para iniciar las animaciones escalonadas
    LaunchedEffect(expenses) {
        showHeader = true
        delay(50)
        showGroupsRow = true
        delay(80)
        showExpensesColumn = true
    }

    Column {
        Header(
            title = stringResource(Res.string.bar_item_1_text),
            modifier = Modifier
                .alpha(headerAlpha)
                .offset(y = headerOffsetY.dp)
        )
        when (windowSizeClass) {
            WindowWidthSizeClass.Compact, WindowWidthSizeClass.Medium -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = stringResource(Res.string.your_groups),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .alpha(groupsRowAlpha)
                                .offset(y = groupsRowOffsetY.dp)
                        )
                    }
                    item {
                        Box(
                            Modifier
                                .alpha(groupsRowAlpha)
                                .offset(y = groupsRowOffsetY.dp),
                        ) {
                            if (groups.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(Res.string.you_have_no_groups),
                                        style = MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            } else {
                                GroupsRow(
                                    groups = groups,
                                    onGroupClick = onGroupClick,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            text = stringResource(Res.string.your_expenses),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                .padding(top = 12.dp, bottom = 4.dp)
                                .alpha(expensesColumnAlpha)
                                .offset(y = expensesColumnOffsetY.dp)
                        )
                    }
                    if (expenses.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                                    .alpha(expensesColumnAlpha)
                                    .offset(y = expensesColumnOffsetY.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.you_have_no_expenses),
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        if (unpaid.isNotEmpty()) {
                            itemsIndexed(unpaid) { i, expense ->
                                ExpenseCard(
                                    expense = expense,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 1.dp)
                                        .clip(
                                            if (unpaid.size == 1) {
                                                RoundedCornerShape(16.dp)
                                            } else {
                                                if (i == 0) RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomEnd = 2.dp,
                                                    bottomStart = 2.dp
                                                ) else {
                                                    if (i == unpaid.lastIndex)
                                                        RoundedCornerShape(
                                                            topStart = 2.dp,
                                                            topEnd = 2.dp,
                                                            bottomEnd = 16.dp,
                                                            bottomStart = 16.dp
                                                        )
                                                    else RoundedCornerShape(2.dp)
                                                }
                                            }
                                        )
                                        .clickable {
                                            onExpenseClick(expense.id)
                                        }
                                        .alpha(expensesColumnAlpha)
                                        .offset(y = expensesColumnOffsetY.dp),
                                )
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp)
                                        .alpha(expensesColumnAlpha)
                                        .offset(y = expensesColumnOffsetY.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(Res.string.you_have_no_expenses),
                                        style = MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        if (paid.isNotEmpty()) {
                            item {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(expensesColumnAlpha)
                                        .offset(y = expensesColumnOffsetY.dp)
                                ) {
                                    TextButton(
                                        onClick = { showPaidExpenses = !showPaidExpenses },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.wrapContentWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            Icon(
                                                imageVector = if (showPaidExpenses) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(Res.string.paid_expenses) + " (${paid.size})",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        itemsIndexed(paid) { i, expense ->
                            AnimatedVisibility(
                                visible = showPaidExpenses,
                                enter = slideInVertically(
                                    animationSpec = tween(350),
                                    initialOffsetY = { -it + (it / 8) * 5 }
                                ) + fadeIn(tween(350)),
                                exit = slideOutVertically(
                                    animationSpec = tween(250),
                                    targetOffsetY = { -it - (it / 8) }
                                ) + fadeOut(tween(100))
                            ) {
                                ExpenseCard(
                                    expense = expense,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 1.dp)
                                        .clip(
                                            if (paid.size == 1) {
                                                RoundedCornerShape(16.dp)
                                            } else
                                                if (i == 0) RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomEnd = 2.dp,
                                                    bottomStart = 2.dp
                                                ) else {
                                                    if (i == paid.lastIndex)
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
                                            onExpenseClick(expense.id)
                                        }
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

            WindowWidthSizeClass.Expanded -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .alpha(groupsRowAlpha)
                            .offset(y = groupsRowOffsetY.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(Res.string.your_expenses),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 4.dp)
                            )
                        }
                        if (expenses.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(Res.string.you_have_no_expenses),
                                        style = MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            if (unpaid.isNotEmpty()) {
                                itemsIndexed(unpaid) { i, expense ->
                                    ExpenseCard(
                                        expense = expense,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 1.dp)
                                            .clip(
                                                if (unpaid.size == 1) {
                                                    RoundedCornerShape(16.dp)
                                                } else {
                                                    if (i == 0) RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomEnd = 2.dp,
                                                        bottomStart = 2.dp
                                                    ) else {
                                                        if (i == unpaid.lastIndex)
                                                            RoundedCornerShape(
                                                                topStart = 2.dp,
                                                                topEnd = 2.dp,
                                                                bottomEnd = 16.dp,
                                                                bottomStart = 16.dp
                                                            )
                                                        else RoundedCornerShape(2.dp)
                                                    }
                                                }
                                            )
                                            .clickable {
                                                onExpenseClick(expense.id)
                                            }
                                    )
                                }
                            } else {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.you_have_no_expenses),
                                            style = MaterialTheme.typography.labelMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                    }
                                }
                            }
                            if (paid.isNotEmpty()) {
                                item {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        TextButton(
                                            onClick = { showPaidExpenses = !showPaidExpenses },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.wrapContentWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                            ) {
                                                Icon(
                                                    imageVector = if (showPaidExpenses) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = stringResource(Res.string.paid_expenses) + " (${paid.size})",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                                itemsIndexed(paid) { i, expense ->
                                    AnimatedVisibility(
                                        visible = showPaidExpenses,
                                        enter = slideInVertically(
                                            animationSpec = tween(350),
                                            initialOffsetY = { -it + (it / 8) * 5 }
                                        ) + fadeIn(tween(350)),
                                        exit = slideOutVertically(
                                            animationSpec = tween(250),
                                            targetOffsetY = { -it - (it / 8) }
                                        ) + fadeOut(tween(100))
                                    ) {
                                        ExpenseCard(
                                            expense = expense,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = 16.dp,
                                                    vertical = 1.dp
                                                )
                                                .clip(
                                                    if (paid.size == 1) {
                                                        RoundedCornerShape(16.dp)
                                                    } else
                                                        if (i == 0) RoundedCornerShape(
                                                            topStart = 16.dp,
                                                            topEnd = 16.dp,
                                                            bottomEnd = 2.dp,
                                                            bottomStart = 2.dp
                                                        ) else {
                                                            if (i == paid.lastIndex)
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
                                                    onExpenseClick(expense.id)
                                                }
                                        )
                                    }
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                            .alpha(groupsRowAlpha)
                            .offset(y = groupsRowOffsetY.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(Res.string.your_groups),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (groups.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(Res.string.you_have_no_groups),
                                        style = MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            items(groups) { group ->
                                GroupCard(
                                    image = group.image,
                                    name = group.name,
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(ShapeDefaults.Medium)
                                ) { onGroupClick(group.id) }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    modifier: Modifier = Modifier
) {
    val remainingBalance = remember(expense.amountPaid, expense.amount) {
        (expense.amount - expense.amountPaid).toTwoDecimals()
    }

    val paid = expense.paid

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .semantics { contentDescription = "Expense: ${expense.title}" }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                getCategoryIcon(expense.category),
                tint = if (paid) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.onPrimary,
                contentDescription = expense.category.toString(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (paid) MaterialTheme.colorScheme.surfaceDim else MaterialTheme.colorScheme.primary)
                    .padding(12.dp)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (paid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    ),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth()
                )
                Text(
                    text = formatDate(expense.createdAt, "MMM dd"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.wrapContentWidth()
            ) {
                if (expense.amountPaid != 0.0 && !paid) {
                    Text(
                        text = formatCurrency(remainingBalance, "es-MX"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(expense.amount, "es-MX"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                } else
                    Text(
                        text = formatCurrency(expense.amount, "es-MX"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (paid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        )
                    )
            }
        }
    }
}

@Composable
internal fun GroupsRow(
    groups: List<Group>,
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(groups) { group ->
            GroupCard(
                image = group.image,
                name = group.name,
                modifier = Modifier
                    .size(150.dp)
                    .clip(ShapeDefaults.Medium)
            ) { onGroupClick(group.id) }
        }
    }
}

@Composable
private fun GroupCard(
    image: String,
    name: String,
    modifier: Modifier = Modifier,
    onGroupClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clickable { onGroupClick() }
    ) {
        // Imagen de fondo
        NetworkImage(
            imageUrl = image,
            modifier = Modifier.fillMaxSize(),
            type = NetworkImageType.GROUP
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        ),
                        startY = 250f,
                    )
                )
        )

        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        )
    }
}