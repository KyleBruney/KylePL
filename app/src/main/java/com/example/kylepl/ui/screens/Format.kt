package com.example.kylepl.ui.screens

import kotlin.math.round

/** Drops a trailing ".0" for whole-number kg values, keeps decimals like 232.5. */
internal fun formatKg(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

internal fun roundToOneDecimal(value: Double): Double = round(value * 10) / 10.0

/** Uppercases the first letter of every word, preserving existing spacing. */
internal fun capitalizeWords(text: String): String {
    val chars = text.toCharArray()
    var capitalizeNext = true
    for (i in chars.indices) {
        val c = chars[i]
        if (c.isWhitespace()) {
            capitalizeNext = true
        } else {
            if (capitalizeNext) chars[i] = c.uppercaseChar()
            capitalizeNext = false
        }
    }
    return String(chars)
}

/** Uppercases just the first letter of the text. */
internal fun capitalizeFirst(text: String): String = text.replaceFirstChar { it.uppercase() }
