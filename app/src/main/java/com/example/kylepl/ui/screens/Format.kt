package com.example.kylepl.ui.screens

import kotlin.math.round

/** Drops a trailing ".0" for whole-number kg values, keeps decimals like 232.5. */
internal fun formatKg(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

internal fun roundToOneDecimal(value: Double): Double = round(value * 10) / 10.0
