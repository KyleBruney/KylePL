package com.example.kylepl.data.rehab

/** Which body area a rehab exercise belongs to. */
enum class RehabGroup {
    SHOULDERS, THORAX, POSTCHAIN, HIPS, KNEES
}

/** A single user-defined rehab exercise for one body area. */
data class RehabExercise(
    val id: String,
    val name: String,
    val repRange: String,
    val description: String,
)
