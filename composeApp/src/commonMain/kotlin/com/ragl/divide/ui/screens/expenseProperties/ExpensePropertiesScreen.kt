package com.ragl.divide.ui.screens.expenseProperties

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.ragl.divide.data.models.Category
import com.ragl.divide.ui.components.AdaptiveFAB
import com.ragl.divide.ui.components.DateTimePickerDialog
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.utils.DivideTextField
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add_expense
import dividemultiplatform.composeapp.generated.resources.amount
import dividemultiplatform.composeapp.generated.resources.category
import dividemultiplatform.composeapp.generated.resources.edit
import dividemultiplatform.composeapp.generated.resources.notes
import dividemultiplatform.composeapp.generated.resources.title
import dividemultiplatform.composeapp.generated.resources.update_expense
import org.jetbrains.compose.resources.stringResource

class ExpensePropertiesScreen(
    private val expenseId: String? = null
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = koinScreenModel<ExpensePropertiesViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        // Estado para el layout por pasos (solo para gastos nuevos)
        var currentStep by remember { mutableStateOf(0) }
        val totalSteps = 3

        // Función para validar el paso actual
        val validateCurrentStep: () -> Boolean = {
            when (currentStep) {
                0 -> vm.validateTitle() // Paso 1: validar título
                1 -> vm.validateAmount() // Paso 2: validar cantidad
                2 -> true // Paso 3: notas son opcionales
                else -> true
            }
        }

        // BackHandler para manejar navegación entre pasos
        if (expenseId == null) {
            BackHandler(enabled = currentStep > 0) {
                currentStep--
            }
        }

        LaunchedEffect(Unit) {
            if (expenseId != null) {
                vm.setViewModelExpense(userViewModel.getExpenseById(expenseId))
            }
        }

        var categoryMenuExpanded by remember { mutableStateOf(false) }
        var frequencyMenuExpanded by remember { mutableStateOf(false) }

        var selectDateDialogEnabled by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        var selectedDate = vm.startingDate

        Scaffold(
            topBar = {
                AppBar(
                    onBackClick = { 
                        if (expenseId == null && currentStep > 0) {
                            currentStep--
                        } else {
                            navigator.pop()
                        }
                    },
                    currentStep = if (expenseId == null) currentStep else null,
                    totalSteps = if (expenseId == null) totalSteps else null
                )
            },
            floatingActionButton = {
                if (expenseId != null || currentStep == totalSteps - 1) {
                    AdaptiveFAB(
                        onClick = {
                            if (vm.validateTitle().and(vm.validateAmount())) {
                                userViewModel.showLoading()
                                vm.saveExpense(
                                    onSuccess = {
                                        userViewModel.saveExpense(it)
                                        userViewModel.hideLoading()
                                        navigator.pop()
                                    },
                                    onError = {
                                        userViewModel.hideLoading()
                                        userViewModel.handleError(it)
                                    }
                                )
                            }
                        },
                        icon = Icons.Default.Check,
                        contentDescription = if (expenseId == null) stringResource(Res.string.add_expense) else stringResource(Res.string.edit),
                        text = if (expenseId == null) stringResource(Res.string.add_expense) else stringResource(Res.string.edit),
                    )
                }
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .imePadding()
                    .verticalScroll(state = scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            ) {
                if (selectDateDialogEnabled) {
                    DateTimePickerDialog(
                        initialTime = selectedDate,
                        onDismissRequest = { selectDateDialogEnabled = false },
                        onConfirmClick = {
                            vm.updateStartingDate(it)
                            selectedDate = it
                        }
                    )
                }

                // Layout por pasos para gastos nuevos
                if (expenseId == null) {
                    StepByStepLayout(
                        vm = vm,
                        currentStep = currentStep,
                        onNextStep = { 
                            if (validateCurrentStep() && currentStep < totalSteps - 1) {
                                currentStep++
                            }
                        },
                        categoryMenuExpanded = categoryMenuExpanded,
                        onCategoryMenuExpandedChange = { categoryMenuExpanded = it },
                        validateCurrentStep = validateCurrentStep
                    )
                } else {
                    // Layout original para gastos existentes
                    ExistingExpenseLayout(
                        vm = vm,
                        categoryMenuExpanded = categoryMenuExpanded,
                        onCategoryMenuExpandedChange = { categoryMenuExpanded = it },
                        frequencyMenuExpanded = frequencyMenuExpanded,
                        onFrequencyMenuExpandedChange = { frequencyMenuExpanded = it },
                        selectDateDialogEnabled = selectDateDialogEnabled,
                        onSelectDateDialogEnabledChange = { selectDateDialogEnabled = it },
                        selectedDate = selectedDate
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun StepByStepLayout(
        vm: ExpensePropertiesViewModel,
        currentStep: Int,
        onNextStep: () -> Unit,
        categoryMenuExpanded: Boolean,
        onCategoryMenuExpandedChange: (Boolean) -> Unit,
        validateCurrentStep: () -> Boolean
    ) {
        // Animación del progress indicator
        val animatedProgress by animateFloatAsState(
            targetValue = (currentStep + 1f) / 3f,
            animationSpec = tween(durationMillis = 300),
            label = "progress"
        )

        Column {
            Text(
                text = "Paso ${currentStep + 1} de 3",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            // Contenido animado con slide horizontal
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        // Avanzar: slide hacia la izquierda
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(300)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(300)
                        )
                    } else {
                        // Retroceder: slide hacia la derecha
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(300)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(300)
                        )
                    }
                },
                label = "step_content"
            ) { step ->
                when (step) {
                    0 -> {
                        // Paso 1: Título y Categoría
                        Column {
                            DivideTextField(
                                label = stringResource(Res.string.title),
                                input = vm.title,
                                error = vm.titleError,
                                onValueChange = vm::updateTitle,
                                validate = vm::validateTitle,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            Text(
                                text = stringResource(Res.string.category),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                modifier = Modifier
                                    .padding(bottom = 32.dp)
                                    .fillMaxWidth(),
                                expanded = categoryMenuExpanded,
                                onExpandedChange = onCategoryMenuExpandedChange
                            ) {
                                TextField(
                                    value = vm.category.name,
                                    onValueChange = {},
                                    singleLine = true,
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                                        .clip(ShapeDefaults.Medium)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryMenuExpanded,
                                    onDismissRequest = { onCategoryMenuExpandedChange(false) },
                                    modifier = Modifier.clip(CircleShape)
                                ) {
                                    Category.entries.forEach {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = it.name,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            onClick = {
                                                vm.updateCategory(it)
                                                onCategoryMenuExpandedChange(false)
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))
                            
                            Button(
                                onClick = onNextStep,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text("Siguiente")
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    1 -> {
                        // Paso 2: Cantidad
                        Column {
                            DivideTextField(
                                label = stringResource(Res.string.amount),
                                keyboardType = KeyboardType.Number,
                                prefix = { Text(text = "$", style = MaterialTheme.typography.bodyMedium) },
                                input = vm.amount,
                                error = vm.amountError,
                                enabled = vm.amountPaid == 0.0,
                                validate = vm::validateAmount,
                                onValueChange = { input ->
                                    if (input.isEmpty()) vm.updateAmount("") else {
                                        val formatted = input.replace(",", ".")
                                        val parsed = formatted.toDoubleOrNull()
                                        parsed?.let {
                                            val decimalPart = formatted.substringAfter(".", "")
                                            if (decimalPart.length <= 2 && parsed <= 999999.99) {
                                                vm.updateAmount(input)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(bottom = 32.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            
                            Button(
                                onClick = onNextStep,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text("Siguiente")
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    2 -> {
                        // Paso 3: Notas
                        Column {
                            DivideTextField(
                                label = stringResource(Res.string.notes),
                                input = vm.notes,
                                onValueChange = vm::updateNotes,
                                imeAction = ImeAction.Default,
                                singleLine = false,
                                modifier = Modifier
                                    .heightIn(max = 200.dp)
                                    .padding(bottom = 32.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ExistingExpenseLayout(
        vm: ExpensePropertiesViewModel,
        categoryMenuExpanded: Boolean,
        onCategoryMenuExpandedChange: (Boolean) -> Unit,
        frequencyMenuExpanded: Boolean,
        onFrequencyMenuExpandedChange: (Boolean) -> Unit,
        selectDateDialogEnabled: Boolean,
        onSelectDateDialogEnabledChange: (Boolean) -> Unit,
        selectedDate: Long
    ) {
        DivideTextField(
            label = stringResource(Res.string.title),
            input = vm.title,
            error = vm.titleError,
            onValueChange = vm::updateTitle,
            validate = vm::validateTitle,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = stringResource(Res.string.category),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            expanded = categoryMenuExpanded,
            onExpandedChange = onCategoryMenuExpandedChange
        ) {
            TextField(
                value = vm.category.name,
                onValueChange = {},
                singleLine = true,
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                    .clip(ShapeDefaults.Medium)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { onCategoryMenuExpandedChange(false) },
                modifier = Modifier.clip(CircleShape)
            ) {
                Category.entries.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            vm.updateCategory(it)
                            onCategoryMenuExpandedChange(false)
                        }
                    )
                }
            }
        }
        DivideTextField(
            label = stringResource(Res.string.amount),
            keyboardType = KeyboardType.Number,
            prefix = { Text(text = "$", style = MaterialTheme.typography.bodyMedium) },
            input = vm.amount,
            error = vm.amountError,
            enabled = vm.amountPaid == 0.0,
            validate = vm::validateAmount,
            onValueChange = { input ->
                if (input.isEmpty()) vm.updateAmount("") else {
                    val formatted = input.replace(",", ".")
                    val parsed = formatted.toDoubleOrNull()
                    parsed?.let {
                        val decimalPart = formatted.substringAfter(".", "")
                        if (decimalPart.length <= 2 && parsed <= 999999.99) {
                            vm.updateAmount(input)
                        }
                    }
                }
            },
            modifier = Modifier.padding(bottom = 12.dp)
        )
        DivideTextField(
            label = stringResource(Res.string.notes),
            input = vm.notes,
            onValueChange = vm::updateNotes,
            imeAction = ImeAction.Default,
            singleLine = false,
            modifier = Modifier
                .heightIn(max = 200.dp)
                .padding(bottom = 12.dp)
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AppBar(
        onBackClick: () -> Unit,
        currentStep: Int? = null,
        totalSteps: Int? = null
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    if (currentStep != null && totalSteps != null) {
                        when (currentStep) {
                            0 -> "Información básica"
                            1 -> "Cantidad"
                            2 -> "Detalles adicionales"
                            else -> stringResource(Res.string.add_expense)
                        }
                    } else {
                        stringResource(if (expenseId == null) Res.string.add_expense else Res.string.update_expense)
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        if (currentStep != null && currentStep > 0) Icons.Filled.ArrowBack else Icons.Filled.Close,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}