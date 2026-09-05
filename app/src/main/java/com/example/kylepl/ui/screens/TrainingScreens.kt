package com.example.kylepl.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kylepl.data.plyometrics.PlyoExercise
import com.example.kylepl.data.plyometrics.PlyoLevel
import com.example.kylepl.data.plyometrics.PlyoRepository
import com.example.kylepl.data.rehab.RehabExercise
import com.example.kylepl.data.rehab.RehabGroup
import com.example.kylepl.data.rehab.RehabRepository
import com.example.kylepl.data.stretch.StretchExercise
import com.example.kylepl.data.stretch.StretchRepository
import com.example.kylepl.data.warmup.WarmupExercise
import com.example.kylepl.data.warmup.WarmupGroup
import com.example.kylepl.data.warmup.WarmupRepository
import com.example.kylepl.ui.components.HeroCard
import com.example.kylepl.ui.components.SectionHeading
import kotlinx.coroutines.launch

@Composable
fun BeginnerPlyometricsScreen() = PlyoScreen(
    level = PlyoLevel.BEGINNER,
    title = "Beginner Plyometrics",
    caption = "Foundational drills to build reactive strength and landing mechanics.",
)

@Composable
fun IntermediatePlyometricsScreen() = PlyoScreen(
    level = PlyoLevel.INTERMEDIATE,
    title = "Intermediate Plyometrics",
    caption = "Higher-intensity drills for lifters with solid landing mechanics.",
)

@Composable
fun AdvancedPlyometricsScreen() = PlyoScreen(
    level = PlyoLevel.ADVANCED,
    title = "Advanced Plyometrics",
    caption = "Maximal-intensity plyometric work for experienced athletes.",
)

@Composable
private fun PlyoScreen(level: PlyoLevel, title: String, caption: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exercisesFlow = remember(context, level) { PlyoRepository.observeExercises(context, level) }
    val exercises by exercisesFlow.collectAsState(initial = emptyList())
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                eyebrow = "Accessory Training",
                title = title,
                value = "${exercises.size} Exercises",
                caption = caption,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeading("Plyometric Exercises")
                TextButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Add Exercise")
                }
            }
        }
        if (exercises.isEmpty()) {
            item {
                Text(
                    "No exercises yet. Tap \"Add Exercise\" to build your routine.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        itemsIndexed(exercises, key = { _, exercise -> exercise.id }) { index, exercise ->
            PlyoExerciseCard(
                exercise = exercise,
                selected = exercise.id == selectedId,
                canMoveUp = index > 0,
                canMoveDown = index < exercises.lastIndex,
                onClick = { selectedId = if (selectedId == exercise.id) null else exercise.id },
                onMoveUp = { scope.launch { PlyoRepository.moveExercise(context, level, exercise.id, -1) } },
                onMoveDown = { scope.launch { PlyoRepository.moveExercise(context, level, exercise.id, 1) } },
                onDelete = {
                    scope.launch { PlyoRepository.deleteExercise(context, level, exercise.id) }
                    selectedId = null
                },
            )
        }
    }

    if (showAddDialog) {
        AddPlyoDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, repRange, description ->
                scope.launch { PlyoRepository.addExercise(context, level, name, repRange, description) }
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun PlyoExerciseCard(
    exercise: PlyoExercise,
    selected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (exercise.repRange.isNotBlank()) {
                    Text(
                        exercise.repRange,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (exercise.description.isNotBlank()) {
                Text(
                    exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            AnimatedVisibility(visible = selected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPlyoDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, repRange: String, description: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var repRange by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Plyometric Exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = repRange,
                    onValueChange = { repRange = it },
                    label = { Text("Rep Range") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), repRange.trim(), description.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun SquatWarmupScreen() = WarmupScreen(
    group = WarmupGroup.SQUAT,
    title = "Squat Warmup",
    caption = "Prime the hips and knees before loading the squat.",
)

@Composable
fun BenchWarmupScreen() = WarmupScreen(
    group = WarmupGroup.BENCH,
    title = "Bench Warmup",
    caption = "Open up the shoulders and chest before benching.",
)

@Composable
fun DeadliftWarmupScreen() = WarmupScreen(
    group = WarmupGroup.DEADLIFT,
    title = "Deadlift Warmup",
    caption = "Prime the posterior chain before pulling.",
)

@Composable
private fun WarmupScreen(group: WarmupGroup, title: String, caption: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exercisesFlow = remember(context, group) { WarmupRepository.observeExercises(context, group) }
    val exercises by exercisesFlow.collectAsState(initial = emptyList())
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                eyebrow = "Accessory Training",
                title = title,
                value = "${exercises.size} Exercises",
                caption = caption,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeading("Warmup Exercises")
                TextButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Add Warmup")
                }
            }
        }
        if (exercises.isEmpty()) {
            item {
                Text(
                    "No warmup exercises yet. Tap \"Add Warmup\" to build your routine.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        itemsIndexed(exercises, key = { _, exercise -> exercise.id }) { index, exercise ->
            WarmupExerciseCard(
                exercise = exercise,
                selected = exercise.id == selectedId,
                canMoveUp = index > 0,
                canMoveDown = index < exercises.lastIndex,
                onClick = { selectedId = if (selectedId == exercise.id) null else exercise.id },
                onMoveUp = { scope.launch { WarmupRepository.moveExercise(context, group, exercise.id, -1) } },
                onMoveDown = { scope.launch { WarmupRepository.moveExercise(context, group, exercise.id, 1) } },
                onDelete = {
                    scope.launch { WarmupRepository.deleteExercise(context, group, exercise.id) }
                    selectedId = null
                },
            )
        }
    }

    if (showAddDialog) {
        AddWarmupDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, repRange, description ->
                scope.launch { WarmupRepository.addExercise(context, group, name, repRange, description) }
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun WarmupExerciseCard(
    exercise: WarmupExercise,
    selected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (exercise.repRange.isNotBlank()) {
                    Text(
                        exercise.repRange,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (exercise.description.isNotBlank()) {
                Text(
                    exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            AnimatedVisibility(visible = selected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddWarmupDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, repRange: String, description: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var repRange by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Warmup Exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = repRange,
                    onValueChange = { repRange = it },
                    label = { Text("Rep Range") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), repRange.trim(), description.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun StretchesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exercisesFlow = remember(context) { StretchRepository.observeExercises(context) }
    val exercises by exercisesFlow.collectAsState(initial = emptyList())
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                eyebrow = "Accessory Training",
                title = "Stretches",
                value = "${exercises.size} Exercises",
                caption = "Mobility and breath work to support range of motion and recovery.",
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeading("Stretches")
                TextButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Add Stretch")
                }
            }
        }
        if (exercises.isEmpty()) {
            item {
                Text(
                    "No stretches yet. Tap \"Add Stretch\" to build your routine.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        itemsIndexed(exercises, key = { _, exercise -> exercise.id }) { index, exercise ->
            StretchExerciseCard(
                exercise = exercise,
                selected = exercise.id == selectedId,
                canMoveUp = index > 0,
                canMoveDown = index < exercises.lastIndex,
                onClick = { selectedId = if (selectedId == exercise.id) null else exercise.id },
                onMoveUp = { scope.launch { StretchRepository.moveExercise(context, exercise.id, -1) } },
                onMoveDown = { scope.launch { StretchRepository.moveExercise(context, exercise.id, 1) } },
                onDelete = {
                    scope.launch { StretchRepository.deleteExercise(context, exercise.id) }
                    selectedId = null
                },
            )
        }
    }

    if (showAddDialog) {
        AddStretchDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, repRange, description ->
                scope.launch { StretchRepository.addExercise(context, name, repRange, description) }
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun StretchExerciseCard(
    exercise: StretchExercise,
    selected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (exercise.repRange.isNotBlank()) {
                    Text(
                        exercise.repRange,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (exercise.description.isNotBlank()) {
                Text(
                    exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            AnimatedVisibility(visible = selected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddStretchDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, repRange: String, description: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var repRange by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Stretch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = repRange,
                    onValueChange = { repRange = it },
                    label = { Text("Rep Range") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), repRange.trim(), description.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun ShouldersRehabScreen() = RehabScreen(
    group = RehabGroup.SHOULDERS,
    title = "Shoulders",
    caption = "Joint health and injury-prevention work for the shoulders.",
)

@Composable
fun ThoraxRehabScreen() = RehabScreen(
    group = RehabGroup.THORAX,
    title = "Thorax",
    caption = "Joint health and injury-prevention work for the thorax.",
)

@Composable
fun PostchainRehabScreen() = RehabScreen(
    group = RehabGroup.POSTCHAIN,
    title = "Postchain",
    caption = "Joint health and injury-prevention work for the posterior chain.",
)

@Composable
fun HipsRehabScreen() = RehabScreen(
    group = RehabGroup.HIPS,
    title = "Hips",
    caption = "Joint health and injury-prevention work for the hips.",
)

@Composable
fun KneesRehabScreen() = RehabScreen(
    group = RehabGroup.KNEES,
    title = "Knees",
    caption = "Joint health and injury-prevention work for the knees.",
)

@Composable
private fun RehabScreen(group: RehabGroup, title: String, caption: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exercisesFlow = remember(context, group) { RehabRepository.observeExercises(context, group) }
    val exercises by exercisesFlow.collectAsState(initial = emptyList())
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                eyebrow = "Accessory Training",
                title = title,
                value = "${exercises.size} Exercises",
                caption = caption,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeading("Exercises")
                TextButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Add Exercise")
                }
            }
        }
        if (exercises.isEmpty()) {
            item {
                Text(
                    "No exercises yet. Tap \"Add Exercise\" to build your routine.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        itemsIndexed(exercises, key = { _, exercise -> exercise.id }) { index, exercise ->
            RehabExerciseCard(
                exercise = exercise,
                selected = exercise.id == selectedId,
                canMoveUp = index > 0,
                canMoveDown = index < exercises.lastIndex,
                onClick = { selectedId = if (selectedId == exercise.id) null else exercise.id },
                onMoveUp = { scope.launch { RehabRepository.moveExercise(context, group, exercise.id, -1) } },
                onMoveDown = { scope.launch { RehabRepository.moveExercise(context, group, exercise.id, 1) } },
                onDelete = {
                    scope.launch { RehabRepository.deleteExercise(context, group, exercise.id) }
                    selectedId = null
                },
            )
        }
    }

    if (showAddDialog) {
        AddRehabDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, repRange, description ->
                scope.launch { RehabRepository.addExercise(context, group, name, repRange, description) }
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun RehabExerciseCard(
    exercise: RehabExercise,
    selected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (exercise.repRange.isNotBlank()) {
                    Text(
                        exercise.repRange,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (exercise.description.isNotBlank()) {
                Text(
                    exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            AnimatedVisibility(visible = selected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRehabDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, repRange: String, description: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var repRange by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Rehab Exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = repRange,
                    onValueChange = { repRange = it },
                    label = { Text("Rep Range") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), repRange.trim(), description.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
