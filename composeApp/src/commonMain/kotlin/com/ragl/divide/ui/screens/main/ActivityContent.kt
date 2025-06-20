package com.ragl.divide.ui.screens.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.ragl.divide.data.models.Expense
import com.ragl.divide.ui.utils.WindowWidthSizeClass
import com.ragl.divide.ui.utils.formatDate
import com.ragl.divide.ui.utils.getWindowWidthSizeClass
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.bar_item_2_text
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ActivityContent(
    expenses: List<Expense>
) {
    // Estados para controlar las animaciones escalonadas
    var showHeader by remember { mutableStateOf(true) }
    var showChart by remember { mutableStateOf(true) }

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

    // Efecto para iniciar las animaciones escalonadas
    LaunchedEffect(expenses) {
        showHeader = true
        delay(50)
        if (expensesByMonth.isNotEmpty()) {
            showChart = true
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Header(
                title = stringResource(Res.string.bar_item_2_text),
                modifier = Modifier
                    .alpha(headerAlpha)
                    .offset(y = headerOffsetY.dp)
            )
        }

        if (expensesByMonth.isNotEmpty()) {
            item {
//                ExpenseChart(
//                    expensesByMonth = expensesByMonth,
//                    modifier = Modifier
//                        .alpha(chartAlpha)
//                        .offset(y = chartOffsetY.dp)
//                )
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
//            BarChart(
//                chartParameters = listOf(
//                    BarParameters(
//                        dataName = stringResource(Res.string.pending),
//                        data = pending,
//                        barColor = MaterialTheme.colorScheme.tertiaryContainer,
//                    ),
//                    BarParameters(
//                        dataName = stringResource(Res.string.paid),
//                        data = paid,
//                        barColor = MaterialTheme.colorScheme.inversePrimary,
//                    ),
//                ),
//                gridColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
//                xAxisData = xAxisLabels,
//                isShowGrid = true,
//                backgroundLineWidth = 1f,
//                showGridWithSpacer = true,
//                animateChart = false,
//                descriptionStyle = MaterialTheme.typography.labelLarge.copy(
//                    color = MaterialTheme.colorScheme.onPrimary,
//                ),
//                yAxisStyle = MaterialTheme.typography.labelSmall.copy(
//                    color = MaterialTheme.colorScheme.onPrimary,
//                ),
//                xAxisStyle = MaterialTheme.typography.labelSmall.copy(
//                    color = MaterialTheme.colorScheme.onPrimary,
//                ),
//                barCornerRadius = 8.dp,
//            )
        }
    }
} 