package com.example.kylepl.data.remote

/** One competition entry from a lifter's OpenIPF history. */
data class CompetitionResult(
    val date: String,
    val meetName: String,
    val federation: String,
    val location: String,
    val division: String,
    val equipment: String,
    val weightClassKg: String,
    val bodyweightKg: Double?,
    val place: String,
    /** Each attempt in the order it was taken; positive kg = good lift, negative kg = no lift, null = not taken. */
    val squatAttemptsKg: List<Double?>,
    val benchAttemptsKg: List<Double?>,
    val deadliftAttemptsKg: List<Double?>,
    val best3SquatKg: Double?,
    val best3BenchKg: Double?,
    val best3DeadliftKg: Double?,
    val totalKg: Double?,
    /** IPF GL Points, the score OpenIPF itself labels "GLP" (from the CSV's Goodlift column). */
    val glp: Double?,
)

/** Best-ever single lift and total, each of which may come from a different meet. */
data class PersonalBests(
    val bestSquatKg: Double?,
    val bestBenchKg: Double?,
    val bestDeadliftKg: Double?,
    val bestTotalKg: Double?,
    val bestGlp: Double?,
)

fun List<CompetitionResult>.toPersonalBests(): PersonalBests = PersonalBests(
    bestSquatKg = mapNotNull { it.best3SquatKg }.maxOrNull(),
    bestBenchKg = mapNotNull { it.best3BenchKg }.maxOrNull(),
    bestDeadliftKg = mapNotNull { it.best3DeadliftKg }.maxOrNull(),
    bestTotalKg = mapNotNull { it.totalKg }.maxOrNull(),
    bestGlp = mapNotNull { it.glp }.maxOrNull(),
)
