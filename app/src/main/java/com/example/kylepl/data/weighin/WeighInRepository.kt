package com.example.kylepl.data.weighin

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import kotlin.math.round
import kotlin.math.sin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.weighInDataStore: DataStore<Preferences> by preferencesDataStore(name = "weigh_in")

/**
 * Stores bodyweight log entries locally on-device (DataStore Preferences), one per day.
 * There's no server for this data, so it never leaves the phone.
 */
object WeighInRepository {

    private val entriesKey = stringPreferencesKey("entries")
    private val seededKey = booleanPreferencesKey("seeded")

    fun observeEntries(context: Context): Flow<List<WeighIn>> =
        context.weighInDataStore.data.map { prefs -> decode(prefs[entriesKey].orEmpty()) }

    suspend fun logWeight(context: Context, date: LocalDate, weightKg: Double) {
        context.weighInDataStore.edit { prefs ->
            val updated = decode(prefs[entriesKey].orEmpty()) + WeighIn(date, weightKg)
            prefs[entriesKey] = encode(updated.sortedBy { it.date })
        }
    }

    /** Seeds a few months of sample history on first launch only, so the graph isn't empty. */
    suspend fun ensureSeeded(context: Context) {
        context.weighInDataStore.edit { prefs ->
            if (prefs[seededKey] != true) {
                if (decode(prefs[entriesKey].orEmpty()).isEmpty()) {
                    prefs[entriesKey] = encode(generateSeedHistory())
                }
                prefs[seededKey] = true
            }
        }
    }

    private fun generateSeedHistory(): List<WeighIn> {
        val today = LocalDate.now()
        val baseWeightKg = 106.5
        return (84 downTo 0 step 2).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val trend = -0.018 * (84 - daysAgo)
            val wave = sin(daysAgo * 0.35) * 0.7
            val weight = round((baseWeightKg + trend + wave) * 10) / 10
            WeighIn(date, weight)
        }
    }

    private fun encode(entries: List<WeighIn>): String =
        entries.joinToString("|") { "${it.date}:${it.weightKg}" }

    private fun decode(raw: String): List<WeighIn> {
        if (raw.isBlank()) return emptyList()
        return raw.split("|").mapNotNull { chunk ->
            val parts = chunk.split(":")
            if (parts.size != 2) return@mapNotNull null
            val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: return@mapNotNull null
            val weight = parts[1].toDoubleOrNull() ?: return@mapNotNull null
            WeighIn(date, weight)
        }
    }
}
