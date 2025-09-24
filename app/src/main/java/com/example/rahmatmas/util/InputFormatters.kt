package com.example.rahmatmas.util

import java.math.BigInteger
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatNumberInput(rawValue: String): String {
    if (rawValue.isEmpty()) return ""

    val digitsOnly = rawValue.filter(Char::isDigit)
    if (digitsOnly.isEmpty()) return ""

    val formatter = DecimalFormat(
        "#,###",
        DecimalFormatSymbols(Locale("id", "ID"))
    )

    return formatter.format(BigInteger(digitsOnly))
}
