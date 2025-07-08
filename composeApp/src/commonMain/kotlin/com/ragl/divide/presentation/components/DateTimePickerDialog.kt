package com.ragl.divide.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    initialTime: Long
)