package com.ragl.divide.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.back
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.ok
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    initialTime: Long
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTime }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialTime
    )
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
    )

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
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
            DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(
                containerColor = Color.Transparent
            ))
        }
    } else {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis ?: initialTime
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onConfirmClick(calendar.timeInMillis)
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
            text = { TimePicker(state = timePickerState, colors = TimePickerDefaults.colors(
                containerColor = Color.Transparent
            )) },
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}