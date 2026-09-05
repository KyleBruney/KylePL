package com.example.kylepl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kylepl.data.remote.CompetitionResult
import com.example.kylepl.ui.components.PlaceBadge
import com.example.kylepl.ui.components.StatTile

@Composable
fun CompetitionDetailScreen(result: CompetitionResult?, onBack: () -> Unit) {
    if (result == null) {
        MissingResultState(onBack)
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DetailHeader(result, onBack) }
        item { MeetInfoCard(result) }
        item { AttemptsCard("Squat", result.squatAttemptsKg, result.best3SquatKg) }
        item { AttemptsCard("Bench Press", result.benchAttemptsKg, result.best3BenchKg) }
        item { AttemptsCard("Deadlift", result.deadliftAttemptsKg, result.best3DeadliftKg) }
        item { TotalsCard(result) }
    }
}

@Composable
private fun MissingResultState(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Result no longer available", style = MaterialTheme.typography.titleLarge)
            Text(
                "Go back and reopen it from the list.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    }
}

@Composable
private fun DetailHeader(result: CompetitionResult, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        ) {
            Text(result.meetName.ifBlank { "Untitled meet" }, style = MaterialTheme.typography.headlineMedium)
            Text(
                listOfNotNull(
                    result.date.takeIf { it.isNotBlank() },
                    result.federation.takeIf { it.isNotBlank() },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        PlaceBadge(result.place)
    }
}

@Composable
private fun MeetInfoCard(result: CompetitionResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow("Location", result.location.ifBlank { "—" })
            InfoRow("Division", result.division.ifBlank { "—" })
            InfoRow("Equipment", result.equipment.ifBlank { "—" })
            InfoRow("Weight Class", result.weightClassKg.takeIf { it.isNotBlank() }?.let { "${it}kg" } ?: "—")
            InfoRow("Bodyweight", result.bodyweightKg?.let { "${formatKg(it)} kg" } ?: "—")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AttemptsCard(liftName: String, attemptsKg: List<Double?>, best3Kg: Double?) {
    val taken = attemptsKg.withIndex().filter { (_, kg) -> kg != null }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(liftName, style = MaterialTheme.typography.titleMedium)
                Text(
                    best3Kg?.let { "Best ${formatKg(it)}kg" } ?: "No good lift",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (taken.isEmpty()) {
                Text(
                    "No attempts recorded",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    taken.forEach { (index, kg) -> AttemptChip(index + 1, kg!!, Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun AttemptChip(attemptNumber: Int, kg: Double, modifier: Modifier = Modifier) {
    val isGoodLift = kg > 0
    val color = if (isGoodLift) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "${if (isGoodLift) "" else "−"}${formatKg(kotlin.math.abs(kg))}kg",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                "Attempt $attemptNumber" + if (isGoodLift) "" else " · No lift",
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
        }
    }
}

@Composable
private fun TotalsCard(result: CompetitionResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile("Total", result.totalKg?.let { "${formatKg(it)} kg" } ?: "—", Modifier.weight(1f))
        StatTile("GLP", result.glp?.let { formatKg(it) } ?: "—", Modifier.weight(1f))
    }
}
