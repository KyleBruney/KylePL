package com.example.kylepl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kylepl.data.weighin.WeighIn
import com.example.kylepl.data.weighin.WeighInRepository
import com.example.kylepl.data.weighin.WeighInViewMode
import com.example.kylepl.data.weighin.datesInPeriod
import com.example.kylepl.data.weighin.isCurrentPeriod
import com.example.kylepl.data.weighin.periodLabel
import com.example.kylepl.data.weighin.shiftAnchor
import com.example.kylepl.ui.components.HeroCard
import com.example.kylepl.ui.components.SectionHeading
import com.example.kylepl.ui.components.WeighInLineChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun WeighInScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entriesFlow = remember(context) { WeighInRepository.observeEntries(context) }
    val entries by entriesFlow.collectAsState(initial = emptyList())

    var mode by remember { mutableStateOf(WeighInViewMode.WEEK) }
    var anchor by remember { mutableStateOf(LocalDate.now()) }
    var weightInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { WeighInRepository.ensureSeeded(context) }

    val periodDates = remember(anchor, mode) { datesInPeriod(anchor, mode) }
    val periodEntries = remember(entries, periodDates) {
        val dateSet = periodDates.toSet()
        entries.filter { it.date in dateSet }
    }
    val periodMean = periodEntries.takeIf { it.isNotEmpty() }
        ?.let { list -> list.sumOf { it.weightKg } / list.size }
        ?.let(::roundToOneDecimal)
    val todayEntry = entries.findLast { it.date == LocalDate.now() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SectionHeading("Weigh In") }

        item {
            LogWeightCard(
                weightInput = weightInput,
                onWeightInputChange = { weightInput = it },
                currentEntryKg = todayEntry?.weightKg,
                onSave = {
                    val weight = weightInput.toDoubleOrNull()
                    if (weight != null && weight > 0) {
                        scope.launch { WeighInRepository.logWeight(context, LocalDate.now(), weight) }
                        weightInput = ""
                    }
                },
            )
        }

        item {
            HeroCard(
                eyebrow = "Bodyweight",
                title = periodLabel(anchor, mode),
                value = periodMean?.let { "${formatKg(it)} kg" } ?: "No data",
                caption = "Mean for this ${if (mode == WeighInViewMode.WEEK) "week" else "month"} · ${periodEntries.size} entries logged",
            )
        }

        item { ModeToggle(mode = mode, onModeChange = { newMode -> mode = newMode }) }

        item {
            PeriodNavigator(
                label = periodLabel(anchor, mode),
                canGoNext = !isCurrentPeriod(anchor, mode),
                onPrevious = { anchor = shiftAnchor(anchor, mode, -1) },
                onNext = { anchor = shiftAnchor(anchor, mode, 1) },
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    WeighInLineChart(periodDates = periodDates, entries = periodEntries, meanWeightKg = periodMean)
                }
            }
        }

//        item {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//            ) {
//                StatTile("Latest", latestEntry?.weightKg?.let { "${formatKg(it)} kg" } ?: "—", Modifier.weight(1f))
//                StatTile("All-Time Mean", allTimeMean?.let { "${formatKg(it)} kg" } ?: "—", Modifier.weight(1f))
//                StatTile("Logged Days", "${entries.size}", Modifier.weight(1f))
//            }
//        }

        item { SectionHeading("Entries") }

        if (periodEntries.isEmpty()) {
            item { Text("No weigh-ins logged for this period.", style = MaterialTheme.typography.bodyMedium) }
        } else {
            items(periodEntries.sortedByDescending { it.date }) { entry -> WeighInRow(entry) }
        }
    }
}

@Composable
private fun LogWeightCard(
    weightInput: String,
    onWeightInputChange: (String) -> Unit,
    currentEntryKg: Double?,
    onSave: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Log Today's Weight", style = MaterialTheme.typography.titleMedium)
            if (currentEntryKg != null) {
                Text(
                    "Already logged today: ${formatKg(currentEntryKg)} kg — saving again will add another entry.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = onWeightInputChange,
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onSave, enabled = weightInput.toDoubleOrNull()?.let { it > 0 } == true) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun ModeToggle(mode: WeighInViewMode, onModeChange: (WeighInViewMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = mode == WeighInViewMode.WEEK,
            onClick = { onModeChange(WeighInViewMode.WEEK) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) { Text("Week") }
        SegmentedButton(
            selected = mode == WeighInViewMode.MONTH,
            onClick = { onModeChange(WeighInViewMode.MONTH) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) { Text("Month") }
    }
}

@Composable
private fun PeriodNavigator(label: String, canGoNext: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous period")
        }
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next period")
        }
    }
}

@Composable
private fun WeighInRow(entry: WeighIn) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                entry.date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "${formatKg(entry.weightKg)} kg",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
