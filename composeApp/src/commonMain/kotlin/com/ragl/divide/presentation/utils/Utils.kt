package com.ragl.divide.presentation.utils

import kotlin.math.round

expect fun logMessage(tag: String, message: String)

expect fun formatCurrency(value: Double, local: String): String

expect fun formatDate(epochMilliseconds: Long, pattern: String = "dd/MM/yyyy hh:mm a"): String

fun Double.toTwoDecimals(): Double {
    return round(this * 100) / 100
}

fun validateQuantity(input: String, updateInput: (String) -> Unit) {
    if (input.isEmpty()) updateInput("") else if (!input.contains(',')) {
        val parsed = input.toDoubleOrNull()
        parsed?.let {
            val decimalPart = input.substringAfter(".", "")
            if (decimalPart.length <= 2 && parsed <= 999999.99) {
                updateInput(input)
            }
        }
    }
}