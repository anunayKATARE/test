package com.lifeos.app.feature.mood.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.app.core.common.DateTimeUtils
import com.lifeos.app.core.ui.components.EmptyState
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.mood.domain.Emotion
import com.lifeos.app.feature.mood.domain.MoodEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(viewModel: MoodViewModel = hiltViewModel()) {
    val entries by viewModel.moodEntries.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }

    if (showAddForm) {
        AddMoodForm(
            onBack = { showAddForm = false },
            onSave = { emotion, intensity, trigger, situation, thoughts, sensations, actions, lessons ->
                viewModel.addMood(emotion, intensity, trigger, situation, thoughts, sensations, actions, lessons)
                showAddForm = false
            },
        )
        return
    }

    LifeOSScaffold(
        topBar = { LifeOSTopBar(title = "Mood") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddForm = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Log mood")
            }
        },
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyState(message = "No mood entries yet. Tap + to log how you're feeling.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
                items(entries, key = { it.id }) { entry ->
                    MoodRow(entry = entry, onDelete = { viewModel.deleteMood(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun MoodRow(entry: MoodEntry, onDelete: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.intensity.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = entry.emotion.name.lowercase(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = DateTimeUtils.formatDisplayDateTime(entry.dateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (entry.trigger.isNotBlank()) {
                    Text(text = "Trigger: ${entry.trigger}", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete mood entry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMoodForm(
    onBack: () -> Unit,
    onSave: (Emotion, Int, String, String, String, String, String, String) -> Unit,
) {
    var emotion by remember { mutableStateOf(Emotion.NEUTRAL) }
    var emotionMenuExpanded by remember { mutableStateOf(false) }
    var intensity by remember { mutableFloatStateOf(5f) }
    var trigger by remember { mutableStateOf("") }
    var situation by remember { mutableStateOf("") }
    var automaticThoughts by remember { mutableStateOf("") }
    var physicalSensations by remember { mutableStateOf("") }
    var actionsTaken by remember { mutableStateOf("") }
    var lessonsLearned by remember { mutableStateOf("") }

    LifeOSScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log mood") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            TextButton(onClick = { emotionMenuExpanded = true }) { Text("Emotion: ${emotion.name}") }
            DropdownMenu(expanded = emotionMenuExpanded, onDismissRequest = { emotionMenuExpanded = false }) {
                Emotion.entries.forEach { e ->
                    DropdownMenuItem(text = { Text(e.name) }, onClick = { emotion = e; emotionMenuExpanded = false })
                }
            }
            Text("Intensity: ${intensity.toInt()}/10")
            Slider(value = intensity, onValueChange = { intensity = it }, valueRange = 1f..10f, steps = 8)
            OutlinedTextField(value = trigger, onValueChange = { trigger = it }, label = { Text("Trigger") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = situation, onValueChange = { situation = it }, label = { Text("Situation") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = automaticThoughts, onValueChange = { automaticThoughts = it }, label = { Text("Automatic thoughts") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = physicalSensations, onValueChange = { physicalSensations = it }, label = { Text("Physical sensations") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = actionsTaken, onValueChange = { actionsTaken = it }, label = { Text("Actions taken") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lessonsLearned, onValueChange = { lessonsLearned = it }, label = { Text("Lessons learned") }, modifier = Modifier.fillMaxWidth())
            TextButton(
                onClick = {
                    onSave(emotion, intensity.toInt(), trigger, situation, automaticThoughts, physicalSensations, actionsTaken, lessonsLearned)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
        }
    }
}
