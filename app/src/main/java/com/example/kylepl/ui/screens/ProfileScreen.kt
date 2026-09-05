package com.example.kylepl.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kylepl.data.remote.CompetitionResult
import com.example.kylepl.data.remote.OpenIpfRepository
import com.example.kylepl.data.remote.ProfileDataCache
import com.example.kylepl.data.remote.toPersonalBests
import com.example.kylepl.data.video.LiftKey
import com.example.kylepl.data.video.PrVideoRepository
import com.example.kylepl.ui.components.HeroCard
import com.example.kylepl.ui.components.PlaceBadge
import com.example.kylepl.ui.components.PrVideoDialog
import com.example.kylepl.ui.components.SectionHeading
import com.example.kylepl.ui.components.StatTile
import kotlinx.coroutines.launch

private const val OPENIPF_USERNAME = "kylebruney"

private sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data class Success(val results: List<CompetitionResult>) : ProfileUiState
}

@Composable
fun ProfileScreen(onResultClick: (Int) -> Unit) {
    var uiState by remember { mutableStateOf<ProfileUiState>(ProfileUiState.Loading) }
    var retryToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(retryToken) {
        uiState = ProfileUiState.Loading
        uiState = try {
            val results = OpenIpfRepository.fetchCompetitionHistory(OPENIPF_USERNAME)
            ProfileDataCache.results = results
            ProfileUiState.Success(results)
        } catch (e: Exception) {
            ProfileUiState.Error(e.message ?: "Couldn't load data from OpenIPF")
        }
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> LoadingState()
        is ProfileUiState.Error -> ErrorState(state.message) { retryToken++ }
        is ProfileUiState.Success -> ProfileContent(state.results, onResultClick)
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Couldn't reach OpenIPF", style = MaterialTheme.typography.titleLarge)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun ProfileContent(results: List<CompetitionResult>, onResultClick: (Int) -> Unit) {
    val bests = results.toPersonalBests()
    val latest = results.firstOrNull()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var openVideoFor by remember { mutableStateOf<LiftKey?>(null) }

    val pickVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val lift = openVideoFor
        if (uri != null && lift != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            scope.launch { PrVideoRepository.setVideoUri(context, lift, uri.toString()) }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                eyebrow = "Powerlifting Profile",
                title = "Kyle Bruney",
                value = bests.bestTotalKg?.let { "${formatKg(it)} kg" } ?: "— kg",
                caption = listOfNotNull(
                    "Best Total",
                    latest?.federation?.takeIf { it.isNotBlank() },
                ).joinToString(" · ") + " · openipf.org/u/$OPENIPF_USERNAME",
                centered = true,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile(
                    "Squat",
                    bests.bestSquatKg?.let { "${formatKg(it)} kg" } ?: "—",
                    Modifier
                        .weight(1f)
                        .clickable { openVideoFor = LiftKey.SQUAT },
                )
                StatTile(
                    "Bench",
                    bests.bestBenchKg?.let { "${formatKg(it)} kg" } ?: "—",
                    Modifier
                        .weight(1f)
                        .clickable { openVideoFor = LiftKey.BENCH },
                )
                StatTile(
                    "Deadlift",
                    bests.bestDeadliftKg?.let { "${formatKg(it)} kg" } ?: "—",
                    Modifier
                        .weight(1f)
                        .clickable { openVideoFor = LiftKey.DEADLIFT },
                )
                StatTile("GLP", bests.bestGlp?.let { formatKg(it) } ?: "—", Modifier.weight(1f))
            }
        }

        item { SectionHeading("Competition Results") }

        if (results.isEmpty()) {
            item { Text("No competition results found on OpenIPF.", style = MaterialTheme.typography.bodyMedium) }
        } else {
            itemsIndexed(results) { index, result ->
                CompetitionRow(result, onClick = { onResultClick(index) })
            }
        }
    }

    val currentLift = openVideoFor
    if (currentLift != null) {
        val videoUriFlow = remember(currentLift) { PrVideoRepository.observeVideoUri(context, currentLift) }
        val videoUriString by videoUriFlow.collectAsState(initial = null)

        PrVideoDialog(
            liftLabel = currentLift.label,
            videoUri = videoUriString?.let { Uri.parse(it) },
            onDismiss = { openVideoFor = null },
            onPickVideo = { pickVideoLauncher.launch(arrayOf("video/*")) },
            onRemoveVideo = { scope.launch { PrVideoRepository.clearVideoUri(context, currentLift) } },
        )
    }
}

@Composable
private fun CompetitionRow(result: CompetitionResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceBadge(result.place)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    result.meetName.ifBlank { "Untitled meet" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOfNotNull(
                        result.date.takeIf { it.isNotBlank() },
                        result.federation.takeIf { it.isNotBlank() },
                        result.location.takeIf { it.isNotBlank() },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    result.totalKg?.let { "${formatKg(it)} kg" } ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    result.glp?.let { "GLP ${formatKg(it)}" } ?: "",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
