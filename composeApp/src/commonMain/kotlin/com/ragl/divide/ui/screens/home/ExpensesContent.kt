package com.ragl.divide.ui.screens.home

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aay.compose.barChart.BarChart
import com.aay.compose.barChart.model.BarParameters
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.utils.Header
import com.ragl.divide.ui.utils.WindowWidthSizeClass
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import com.ragl.divide.ui.utils.getWindowWidthSizeClass
import com.ragl.divide.ui.utils.toTwoDecimals
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.paid
import dividemultiplatform.composeapp.generated.resources.paid_expenses
import dividemultiplatform.composeapp.generated.resources.pending
import dividemultiplatform.composeapp.generated.resources.pending_expenses
import dividemultiplatform.composeapp.generated.resources.you_have_no_expenses
import dividemultiplatform.composeapp.generated.resources.your_expenses
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpensesContent(
    expenses: List<Expense>,
    onExpenseClick: (String) -> Unit
) {
    var showPaidExpenses by rememberSaveable { mutableStateOf(false) }
    val (paid, unpaid) = remember(expenses) { expenses.partition { it.paid } }

    // Estados para controlar las animaciones escalonadas
    var showHeader by remember { mutableStateOf(true) }
    var showChart by remember { mutableStateOf(true) }
    var showEmptyState by remember { mutableStateOf(true) }
    var showUnpaidSection by remember { mutableStateOf(true) }
    var showPaidButton by remember { mutableStateOf(true) }

    // Obtener tamaño de ventana y calcular número de meses en código común
    val windowSizeClass = getWindowWidthSizeClass()
    val monthsToShow = when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> 3
        WindowWidthSizeClass.Medium -> 5
        WindowWidthSizeClass.Expanded -> 6
    }

    // Generar los últimos N meses incluyendo el actual según el tamaño de pantalla
    val lastNMonths by derivedStateOf {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        (0 until monthsToShow).map { i ->
            val monthsBack = monthsToShow - 1 - i
            var month = now.monthNumber - monthsBack
            var year = now.year
            
            // Ajustar si el mes es <= 0
            while (month <= 0) {
                month += 12
                year -= 1
            }
            
            "${month.toString().padStart(2, '0')}/$year"
        }
    }

    val expensesByMonth by derivedStateOf {
        val expensesGrouped = expenses
            .sortedBy { it.createdAt }
            .groupBy { expense ->
                formatDate(expense.createdAt, "MM/yyyy")
            }
        
        // Crear mapa con los últimos N meses, rellenando con listas vacías los meses sin datos
        lastNMonths.associateWith { month ->
            expensesGrouped[month] ?: emptyList()
        }
    }

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

    val chartAlpha by animateFloatAsState(
        targetValue = if (showChart) 1f else 0f,
        animationSpec = tween(350),
        label = "chartAlpha"
    )
    val chartOffsetY by animateIntAsState(
        targetValue = if (showChart) 0 else -200,
        animationSpec = tween(350),
        label = "chartOffsetY"
    )

    val emptyStateAlpha by animateFloatAsState(
        targetValue = if (showEmptyState) 1f else 0f,
        animationSpec = tween(300),
        label = "emptyStateAlpha"
    )
    val emptyStateOffsetY by animateIntAsState(
        targetValue = if (showEmptyState) 0 else -50,
        animationSpec = tween(300),
        label = "emptyStateOffsetY"
    )

    val unpaidSectionAlpha by animateFloatAsState(
        targetValue = if (showUnpaidSection) 1f else 0f,
        animationSpec = tween(350),
        label = "unpaidSectionAlpha"
    )
    val unpaidSectionOffsetY by animateIntAsState(
        targetValue = if (showUnpaidSection) 0 else -100,
        animationSpec = tween(350),
        label = "unpaidSectionOffsetY"
    )

    val paidButtonAlpha by animateFloatAsState(
        targetValue = if (showPaidButton) 1f else 0f,
        animationSpec = tween(300),
        label = "paidButtonAlpha"
    )
    val paidButtonOffsetY by animateIntAsState(
        targetValue = if (showPaidButton) 0 else -50,
        animationSpec = tween(300),
        label = "paidButtonOffsetY"
    )

    // Efecto para iniciar las animaciones escalonadas
    LaunchedEffect(expenses) {
        showHeader = true
        delay(50)
        if (expensesByMonth.isNotEmpty()) {
            showChart = true
            delay(80)
        }
        if (expenses.isEmpty()) {
            showEmptyState = true
        } else {
            if (unpaid.isNotEmpty()) {
                showUnpaidSection = true
                delay(80)
            }
            if (paid.isNotEmpty()) {
                showPaidButton = true
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Header(
                title = stringResource(Res.string.your_expenses),
                modifier = Modifier
                    .alpha(headerAlpha)
                    .offset(y = headerOffsetY.dp)
            )
        }

        if (expensesByMonth.isNotEmpty()) {
            item {
                ExpenseChart(
                    expensesByMonth = expensesByMonth,
                    modifier = Modifier
                        .alpha(chartAlpha)
                        .offset(y = chartOffsetY.dp)
                )
            }
        }

        if (expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .alpha(emptyStateAlpha)
                        .offset(y = emptyStateOffsetY.dp),
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
                item {
                    Column(
                        modifier = Modifier
                            .alpha(unpaidSectionAlpha)
                            .offset(y = unpaidSectionOffsetY.dp)
                    ) {
                        Text(
                            stringResource(Res.string.pending_expenses),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 4.dp)
                        )

                        unpaid.forEachIndexed { i, expense ->
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
                    }
                }
            }

            if (paid.isNotEmpty()) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(paidButtonAlpha)
                            .offset(y = paidButtonOffsetY.dp)
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

                // Mantenemos AnimatedVisibility solo para paid expenses porque necesitan aparecer/desaparecer completamente
                item {
                    AnimatedVisibility(
                        visible = showPaidExpenses,
                        enter = slideInVertically(
                            animationSpec = tween(350),
                            initialOffsetY = { -it }
                        ) + fadeIn(tween(350)),
                        exit = slideOutVertically(
                            animationSpec = tween(250),
                            targetOffsetY = { -it }
                        ) + fadeOut(tween(100))
                    ) {
                        Column {
                            paid.forEachIndexed { i, expense ->
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
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
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
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (paid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    ),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = formatDate(expense.createdAt, "MMM dd"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (expense.amountPaid != 0.0 && !paid) {
                    Text(
                        text = formatCurrency(remainingBalance, "es-MX"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.tertiary,
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
                            color = if (paid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary,
                        )
                    )
            }
        }
    }
}

@Composable
private fun ExpenseChart(
    expensesByMonth: Map<String, List<Expense>>,
    modifier: Modifier = Modifier
) {
    // Usar el orden cronológico correcto de las claves del mapa (ya están ordenadas)
    val sortedMonths = expensesByMonth.keys.toList()
    
    val paid by derivedStateOf {
        sortedMonths.map { month ->
            expensesByMonth[month]?.filter { it.paid }?.sumOf { it.amount } ?: 0.0
        }
    }
    val pending by derivedStateOf {
        sortedMonths.map { month ->
            expensesByMonth[month]?.filter { !it.paid }?.sumOf { it.amount } ?: 0.0
        }
    }
    
    // Formatear las etiquetas del eje X para mostrar solo mes/año
    val xAxisLabels = sortedMonths.map { monthYear ->
        val parts = monthYear.split("/")
        "${parts[0]}/${parts[1].takeLast(2)}" // MM/YY format
    }
    
    Box(
        modifier = modifier
            .height(400.dp)
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            BarChart(
                chartParameters = listOf(
                    BarParameters(
                        dataName = stringResource(Res.string.pending),
                        data = pending,
                        barColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                    BarParameters(
                        dataName = stringResource(Res.string.paid),
                        data = paid,
                        barColor = MaterialTheme.colorScheme.inversePrimary,
                    ),
                ),
                gridColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                xAxisData = xAxisLabels,
                isShowGrid = true,
                backgroundLineWidth = 1f,
                showGridWithSpacer = true,
                animateChart = false,
                descriptionStyle = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                ),
                yAxisStyle = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                ),
                xAxisStyle = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                ),
                barCornerRadius = 8.dp,
            )
        }
    }
}
