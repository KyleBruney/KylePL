package com.example.kylepl.data.weighin

import java.time.LocalDate

/** A single bodyweight log entry, one per day. */
data class WeighIn(
    val date: LocalDate,
    val weightKg: Double,
)
