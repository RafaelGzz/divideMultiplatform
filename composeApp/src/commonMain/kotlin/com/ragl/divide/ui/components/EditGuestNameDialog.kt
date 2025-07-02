package com.ragl.divide.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.guest_name
import dividemultiplatform.composeapp.generated.resources.update
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditGuestNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    errorMessage: String = ""
) {
    var guestName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.guest_name),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            DivideTextField(
                value = guestName,
                onValueChange = { guestName = it },
                label = stringResource(Res.string.guest_name),
                imeAction = ImeAction.Done,
                error = errorMessage,
                characterLimit = 20
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(guestName.trim()) }
            ) {
                Text(stringResource(Res.string.update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
} 