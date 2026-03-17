package com.it10x.foodappgstav7_06.utils

import java.text.NumberFormat
import java.util.Locale

fun formatAmount2(value: Any?): String {
    val amount = when (value) {
        is Double -> value
        is Float -> value.toDouble()
        is Int -> value.toDouble()
        is Long -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2

    return formatter.format(amount)
}
