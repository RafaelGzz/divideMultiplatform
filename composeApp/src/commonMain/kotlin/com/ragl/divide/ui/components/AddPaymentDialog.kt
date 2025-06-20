package com.ragl.divide.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ragl.divide.ui.utils.formatCurrency
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add
import dividemultiplatform.composeapp.generated.resources.amount
import dividemultiplatform.composeapp.generated.resources.amount_must_be_greater_than_0
import dividemultiplatform.composeapp.generated.resources.amount_must_be_less_than_remaining_balance
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.make_a_payment
import dividemultiplatform.composeapp.generated.resources.remaining_balance
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPaymentDialog(
    remainingAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }

    val amountLessThanRemainingBalance = stringResource(Res.string.amount_must_be_less_than_remaining_balance)
    val amountGreaterThanZero = stringResource(Res.string.amount_must_be_greater_than_0)

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
//        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .99f),
//        titleContentColor = MaterialTheme.colorScheme.primary,
//        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                stringResource(Res.string.make_a_payment),
                style = MaterialTheme.typography.titleLarge
            ) },
        text = {
            Column {
                DivideTextField(
                    label = stringResource(Res.string.amount),
                    value = amount,
                    error = amountError,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    prefix = { Text(text = "$", style = MaterialTheme.typography.bodyMedium) },
                    onValueChange = { input ->
                        if (input.isEmpty()) amount = "" else {
                            val formatted = input.replace(",", ".")
                            val parsed = formatted.toDoubleOrNull()
                            parsed?.let {
                                val decimalPart = formatted.substringAfter(".", "")
                                if (decimalPart.length <= 2 && parsed <= 999999.99) {
                                    amount = input
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(
                        Res.string.remaining_balance,
                        formatCurrency(remainingAmount, "es-MX")
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (amount.isNotEmpty() && amount.toDouble() > 0) {
                        if (amount.toDouble() <= remainingAmount) {
                            onConfirm(amount.toDouble())
                        } else {
                            amountError = amountLessThanRemainingBalance
                        }
                    } else {
                        amountError = amountGreaterThanZero
                    }
                }
            ) {
                Text(text = stringResource(Res.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.cancel))
            }
        }
    )
}