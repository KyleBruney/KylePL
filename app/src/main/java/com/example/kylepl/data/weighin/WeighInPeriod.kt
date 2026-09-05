package com.example.kylepl.data.weighin

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

enum class WeighInViewMode { WEEK, MONTH }

/** Every calendar date in the period containing [anchor] (a Mon–Sun week, or a full month), ascending. */
fun datesInPeriod(anchor: LocalDate, mode: WeighInViewMode): List<LocalDate> = when (mode) {
    WeighInViewMode.WEEK -> {
        val start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        (0..6).map { start.plusDays(it.toLong()) }
    }
    WeighInViewMode.MONTH -> {
        val yearMonth = YearMonth.from(anchor)
        (1..yearMonth.lengthOfMonth()).map { yearMonth.atDay(it) }
    }
}

fun periodLabel(anchor: LocalDate, mode: WeighInViewMode): String = when (mode) {
    WeighInViewMode.WEEK -> {
        val dates = datesInPeriod(anchor, mode)
        val start = dates.first()
        val end = dates.last()
        val startFormatter = DateTimeFormatter.ofPattern(if (start.year == end.year) "MMM d" else "MMM d, yyyy")
        val endFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        "${start.format(startFormatter)} – ${end.format(endFormatter)}"
    }
    WeighInViewMode.MONTH -> YearMonth.from(anchor).format(DateTimeFormatter.ofPattern("MMMM yyyy"))
}

fun shiftAnchor(anchor: LocalDate, mode: WeighInViewMode, steps: Long): LocalDate = when (mode) {
    WeighInViewMode.WEEK -> anchor.plusWeeks(steps)
    WeighInViewMode.MONTH -> anchor.plusMonths(steps)
}

/** True when the period containing [anchor] is the one containing today, i.e. there's no later period to move to. */
fun isCurrentPeriod(anchor: LocalDate, mode: WeighInViewMode): Boolean {
    val today = LocalDate.now()
    return when (mode) {
        WeighInViewMode.WEEK -> datesInPeriod(anchor, mode).first() == datesInPeriod(today, mode).first()
        WeighInViewMode.MONTH -> YearMonth.from(anchor) == YearMonth.from(today)
    }
}
