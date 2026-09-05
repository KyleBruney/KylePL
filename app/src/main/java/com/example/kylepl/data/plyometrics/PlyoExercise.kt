package com.example.kylepl.data.plyometrics

/** Which difficulty level a plyometric drill belongs to. */
enum class PlyoLevel {
    BEGINNER, INTERMEDIATE, ADVANCED
}

/** A single user-defined plyometric exercise for one difficulty level. */
data class PlyoExercise(
    val id: String,
    val name: String,
    val repRange: String,
    val description: String,
)
