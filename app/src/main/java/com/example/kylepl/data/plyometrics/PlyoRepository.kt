package com.example.kylepl.data.plyometrics

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.plyoDataStore: DataStore<Preferences> by preferencesDataStore(name = "plyometrics")

/**
 * Stores each difficulty level's user-built plyometric routine locally on-device
 * (DataStore Preferences), in the order the exercises should be performed. There's
 * no built-in routine; users add and order their own exercises per level.
 */
object PlyoRepository {

    private fun keyFor(level: PlyoLevel) = stringPreferencesKey("exercises_${level.name}")

    fun observeExercises(context: Context, level: PlyoLevel): Flow<List<PlyoExercise>> =
        context.plyoDataStore.data.map { prefs -> decode(prefs[keyFor(level)].orEmpty()) }

    suspend fun addExercise(context: Context, level: PlyoLevel, name: String, repRange: String, description: String) {
        context.plyoDataStore.edit { prefs ->
            val existing = decode(prefs[keyFor(level)].orEmpty())
            val exercise = PlyoExercise(id = UUID.randomUUID().toString(), name = name, repRange = repRange, description = description)
            prefs[keyFor(level)] = encode(existing + exercise)
        }
    }

    suspend fun updateExercise(context: Context, level: PlyoLevel, id: String, name: String, repRange: String, description: String) {
        context.plyoDataStore.edit { prefs ->
            val updated = decode(prefs[keyFor(level)].orEmpty()).map { exercise ->
                if (exercise.id == id) exercise.copy(name = name, repRange = repRange, description = description) else exercise
            }
            prefs[keyFor(level)] = encode(updated)
        }
    }

    suspend fun deleteExercise(context: Context, level: PlyoLevel, id: String) {
        context.plyoDataStore.edit { prefs ->
            val updated = decode(prefs[keyFor(level)].orEmpty()).filterNot { it.id == id }
            prefs[keyFor(level)] = encode(updated)
        }
    }

    /** Swaps the exercise with [id] with its neighbor in [direction] (-1 to move up, +1 to move down). */
    suspend fun moveExercise(context: Context, level: PlyoLevel, id: String, direction: Int) {
        context.plyoDataStore.edit { prefs ->
            val current = decode(prefs[keyFor(level)].orEmpty()).toMutableList()
            val index = current.indexOfFirst { it.id == id }
            val target = index + direction
            if (index < 0 || target < 0 || target >= current.size) return@edit
            val exercise = current.removeAt(index)
            current.add(target, exercise)
            prefs[keyFor(level)] = encode(current)
        }
    }

    // Each field is Base64-encoded so user text can never collide with the "|" / ";" delimiters below.
    private fun encode(exercises: List<PlyoExercise>): String =
        exercises.joinToString(";") { exercise ->
            listOf(exercise.id, exercise.name, exercise.repRange, exercise.description)
                .joinToString("|") { field -> Base64.encodeToString(field.toByteArray(Charsets.UTF_8), Base64.NO_WRAP) }
        }

    private fun decode(raw: String): List<PlyoExercise> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { record ->
            val parts = record.split("|")
            if (parts.size != 4) return@mapNotNull null
            val fields = parts.map { field -> String(Base64.decode(field, Base64.NO_WRAP), Charsets.UTF_8) }
            PlyoExercise(id = fields[0], name = fields[1], repRange = fields[2], description = fields[3])
        }
    }
}
