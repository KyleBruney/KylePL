package com.example.kylepl.data.remote

/**
 * Holds the most recently fetched competition history so the detail screen can
 * look up the tapped result by index, without re-fetching or passing the whole
 * object through a navigation argument.
 */
object ProfileDataCache {
    var results: List<CompetitionResult> = emptyList()
}
