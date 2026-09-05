package com.example.kylepl.data.stretch

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

private val Context.stretchDataStore: DataStore<Preferences> by preferencesDataStore(name = "stretches")

/**
 * Stores the user-built stretch routine locally on-device (DataStore Preferences),
 * in the order the exercises should be performed. There's no built-in routine;
 * users add and order their own stretches.
 */
object StretchRepository {

    private val exercisesKey = stringPreferencesKey("exercises")

    fun observeExercises(context: Context): Flow<List<StretchExercise>> =
        context.stretchDataStore.data.map { prefs -> decode(prefs[exercisesKey].orEmpty()) }

    suspend fun addExercise(context: Context, name: String, repRange: String, description: String) {
        context.stretchDataStore.edit { prefs ->
            val existing = decode(prefs[exercisesKey].orEmpty())
            val exercise = StretchExercise(id = UUID.randomUUID().toString(), name = name, repRange = repRange, description = description)
            prefs[exercisesKey] = encode(existing + exercise)
        }
    }

    suspend fun deleteExercise(context: Context, id: String) {
        context.stretchDataStore.edit { prefs ->
            val updated = decode(prefs[exercisesKey].orEmpty()).filterNot { it.id == id }
            prefs[exercisesKey] = encode(updated)
        }
    }

    /** Swaps the exercise with [id] with its neighbor in [direction] (-1 to move up, +1 to move down). */
    suspend fun moveExercise(context: Context, id: String, direction: Int) {
        context.stretchDataStore.edit { prefs ->
            val current = decode(prefs[exercisesKey].orEmpty()).toMutableList()
            val index = current.indexOfFirst { it.id == id }
            val target = index + direction
            if (index < 0 || target < 0 || target >= current.size) return@edit
            val exercise = current.removeAt(index)
            current.add(target, exercise)
            prefs[exercisesKey] = encode(current)
        }
    }

    // Each field is Base64-encoded so user text can never collide with the "|" / ";" delimiters below.
    private fun encode(exercises: List<StretchExercise>): String =
        exercises.joinToString(";") { exercise ->
            listOf(exercise.id, exercise.name, exercise.repRange, exercise.description)
                .joinToString("|") { field -> Base64.encodeToString(field.toByteArray(Charsets.UTF_8), Base64.NO_WRAP) }
        }

    private fun decode(raw: String): List<StretchExercise> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { record ->
            val parts = record.split("|")
            if (parts.size != 4) return@mapNotNull null
            val fields = parts.map { field -> String(Base64.decode(field, Base64.NO_WRAP), Charsets.UTF_8) }
            StretchExercise(id = fields[0], name = fields[1], repRange = fields[2], description = fields[3])
        }
    }
}
