package com.example.kylepl.data.warmup

/** Which lift's warmup routine an exercise belongs to. */
enum class WarmupGroup {
    SQUAT, BENCH, DEADLIFT
}

/** A single user-defined warmup exercise for one lift. */
data class WarmupExercise(
    val id: String,
    val name: String,
    val repRange: String,
    val description: String,
)
