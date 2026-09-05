package com.example.kylepl.data.video

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prVideoDataStore: DataStore<Preferences> by preferencesDataStore(name = "pr_videos")

/**
 * Remembers which local video (picked from the device via the system document
 * picker) is attached to each best-lift PR. Stores the content URI's persisted
 * read permission was already taken by the caller, so it stays playable across
 * app restarts.
 */
object PrVideoRepository {

    private fun keyFor(lift: LiftKey) = stringPreferencesKey("video_${lift.name}")

    fun observeVideoUri(context: Context, lift: LiftKey): Flow<String?> =
        context.prVideoDataStore.data.map { prefs -> prefs[keyFor(lift)] }

    suspend fun setVideoUri(context: Context, lift: LiftKey, uri: String) {
        context.prVideoDataStore.edit { prefs -> prefs[keyFor(lift)] = uri }
    }

    suspend fun clearVideoUri(context: Context, lift: LiftKey) {
        context.prVideoDataStore.edit { prefs -> prefs.remove(keyFor(lift)) }
    }
}
