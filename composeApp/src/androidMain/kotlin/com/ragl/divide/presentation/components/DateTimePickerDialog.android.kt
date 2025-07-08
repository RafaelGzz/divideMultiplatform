package com.ragl.divide.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.ragl.divide.presentation.utils.logMessage
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.ok
import org.jetbrains.compose.resources.stringResource
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    initialTime: Long
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val initialCalendar = remember {
        Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = initialTime
        }
    }
    LaunchedEffect(Unit) {
        logMessage(
            "DateTimePickerDialog",
            "Inicializado con tiempo: $initialTime (${initialCalendar.time})"
        )
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialCalendar.timeInMillis
    )
    val timePickerState = rememberTimePickerState(
        initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCalendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = { showTimePicker = true }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState, colors = DatePickerDefaults.colors(
                    containerColor = Color.Transparent
                )
            )
        }
    } else {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis ?: initialTime
                    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = selectedDateMillis
                    }

                    val localCalendar = Calendar.getInstance(TimeZone.getDefault()).apply {
                        set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                        set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val resultTimeMillis = localCalendar.timeInMillis
                    logMessage(
                        "DateTimePickerDialog",
                        "Fecha seleccionada: $resultTimeMillis (${localCalendar.time})"
                    )

                    onConfirmClick(resultTimeMillis)
                    onDismissRequest()
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(Res.string.back))
                }
            },
            text = {
                TimePicker(
                    state = timePickerState, colors = TimePickerDefaults.colors(
                        containerColor = Color.Transparent
                    )
                )
            }
        )
    }
}