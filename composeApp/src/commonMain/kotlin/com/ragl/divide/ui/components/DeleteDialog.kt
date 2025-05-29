package com.ragl.divide.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.delete
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteDialog(
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(Res.string.delete), style = MaterialTheme.typography.titleLarge)
        },
        text = { Text(text, style = MaterialTheme.typography.bodySmall) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
//        containerColor = MaterialTheme.colorScheme.primaryContainer,
//        titleContentColor = MaterialTheme.colorScheme.primary,
//        textContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}