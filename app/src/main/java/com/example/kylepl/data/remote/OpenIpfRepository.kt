package com.example.kylepl.data.remote

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fetches a lifter's competition history from OpenIPF (openipf.org), the same
 * source used by the openpowerlifting-fetch project. OpenIPF doesn't publish a
 * JSON API, but the lifter page's "Download as CSV" button hits a stable CSV
 * endpoint, which is what this reads.
 */
object OpenIpfRepository {

    private const val LIFTER_CSV_URL = "https://www.openipf.org/api/liftercsv/"

    suspend fun fetchCompetitionHistory(username: String): List<CompetitionResult> =
        withContext(Dispatchers.IO) {
            val connection = URL(LIFTER_CSV_URL + username).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("User-Agent", "PowerLift-Android-App")

            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("OpenIPF returned HTTP ${connection.responseCode}")
                }
                val csv = connection.inputStream.bufferedReader().use { it.readText() }
                parseCsv(csv)
            } finally {
                connection.disconnect()
            }
        }

    private fun parseCsv(csv: String): List<CompetitionResult> {
        val lines = csv.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.size < 2) return emptyList()

        val header = splitCsvLine(lines[0])
        val columnIndex = header.withIndex().associate { (index, name) -> name to index }

        fun List<String>.field(name: String): String =
            columnIndex[name]?.let { index -> getOrNull(index) }?.trim().orEmpty()

        fun String.toKgOrNull(): Double? = toDoubleOrNull()?.takeIf { it > 0 }

        return lines.drop(1).map { line ->
            val row = splitCsvLine(line)
            val town = row.field("MeetTown")
            val state = row.field("MeetState")
            val country = row.field("MeetCountry")
            val location = listOf(town, state, country).filter { it.isNotBlank() }.joinToString(", ")

            CompetitionResult(
                date = row.field("Date"),
                meetName = row.field("MeetName"),
                federation = row.field("Federation"),
                location = location,
                division = row.field("Division"),
                equipment = row.field("Equipment"),
                weightClassKg = row.field("WeightClassKg"),
                bodyweightKg = row.field("BodyweightKg").toDoubleOrNull(),
                place = row.field("Place"),
                squatAttemptsKg = listOf("Squat1Kg", "Squat2Kg", "Squat3Kg", "Squat4Kg").map { row.field(it).toDoubleOrNull() },
                benchAttemptsKg = listOf("Bench1Kg", "Bench2Kg", "Bench3Kg", "Bench4Kg").map { row.field(it).toDoubleOrNull() },
                deadliftAttemptsKg = listOf("Deadlift1Kg", "Deadlift2Kg", "Deadlift3Kg", "Deadlift4Kg").map { row.field(it).toDoubleOrNull() },
                best3SquatKg = row.field("Best3SquatKg").toKgOrNull(),
                best3BenchKg = row.field("Best3BenchKg").toKgOrNull(),
                best3DeadliftKg = row.field("Best3DeadliftKg").toKgOrNull(),
                totalKg = row.field("TotalKg").toKgOrNull(),
                glp = row.field("Goodlift").toDoubleOrNull(),
            )
        }.sortedByDescending { it.date }
    }

    /** Minimal CSV line splitter that respects double-quoted fields (which may contain commas). */
    private fun splitCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}
