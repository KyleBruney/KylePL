package com.example.kylepl.data.rehab

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

private val Context.rehabDataStore: DataStore<Preferences> by preferencesDataStore(name = "rehab")

/**
 * Stores each body area's user-built rehab routine locally on-device (DataStore
 * Preferences), in the order the exercises should be performed. There's no
 * built-in routine; users add and order their own exercises per body area.
 */
object RehabRepository {

    private fun keyFor(group: RehabGroup) = stringPreferencesKey("exercises_${group.name}")

    fun observeExercises(context: Context, group: RehabGroup): Flow<List<RehabExercise>> =
        context.rehabDataStore.data.map { prefs -> decode(prefs[keyFor(group)].orEmpty()) }

    suspend fun addExercise(context: Context, group: RehabGroup, name: String, repRange: String, description: String) {
        context.rehabDataStore.edit { prefs ->
            val existing = decode(prefs[keyFor(group)].orEmpty())
            val exercise = RehabExercise(id = UUID.randomUUID().toString(), name = name, repRange = repRange, description = description)
            prefs[keyFor(group)] = encode(existing + exercise)
        }
    }

    suspend fun deleteExercise(context: Context, group: RehabGroup, id: String) {
        context.rehabDataStore.edit { prefs ->
            val updated = decode(prefs[keyFor(group)].orEmpty()).filterNot { it.id == id }
            prefs[keyFor(group)] = encode(updated)
        }
    }

    /** Swaps the exercise with [id] with its neighbor in [direction] (-1 to move up, +1 to move down). */
    suspend fun moveExercise(context: Context, group: RehabGroup, id: String, direction: Int) {
        context.rehabDataStore.edit { prefs ->
            val current = decode(prefs[keyFor(group)].orEmpty()).toMutableList()
            val index = current.indexOfFirst { it.id == id }
            val target = index + direction
            if (index < 0 || target < 0 || target >= current.size) return@edit
            val exercise = current.removeAt(index)
            current.add(target, exercise)
            prefs[keyFor(group)] = encode(current)
        }
    }

    // Each field is Base64-encoded so user text can never collide with the "|" / ";" delimiters below.
    private fun encode(exercises: List<RehabExercise>): String =
        exercises.joinToString(";") { exercise ->
            listOf(exercise.id, exercise.name, exercise.repRange, exercise.description)
                .joinToString("|") { field -> Base64.encodeToString(field.toByteArray(Charsets.UTF_8), Base64.NO_WRAP) }
        }

    private fun decode(raw: String): List<RehabExercise> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { record ->
            val parts = record.split("|")
            if (parts.size != 4) return@mapNotNull null
            val fields = parts.map { field -> String(Base64.decode(field, Base64.NO_WRAP), Charsets.UTF_8) }
            RehabExercise(id = fields[0], name = fields[1], repRange = fields[2], description = fields[3])
        }
    }
}
