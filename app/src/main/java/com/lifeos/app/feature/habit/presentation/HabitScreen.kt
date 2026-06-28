package com.lifeos.app.feature.habit.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.app.core.ui.components.EmptyState
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.core.ui.components.StreakBadge
import com.lifeos.app.feature.habit.domain.HabitDifficulty
import com.lifeos.app.feature.habit.domain.HabitImportance
import com.lifeos.app.feature.habit.domain.HabitScheduleType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(viewModel: HabitViewModel = hiltViewModel()) {
    val habits by viewModel.habits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LifeOSTopBar(title = "Habits") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add habit")
            }
        },
    ) { padding ->
        if (habits.isEmpty()) {
            EmptyState(message = "No habits yet. Tap + to start building one.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
                items(habits, key = { it.habit.id }) { item ->
                    HabitRow(
                        item = item,
                        onToggle = { viewModel.toggleCompletion(item.habit, item.completedToday) },
                        onDelete = { viewModel.deleteHabit(item.habit.id) },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, schedule, difficulty, importance ->
                viewModel.addHabit(title, description, schedule, difficulty, importance)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun HabitRow(item: HabitUiModel, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (item.completedToday) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = "Toggle completion",
                        tint = if (item.completedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column {
                    Text(text = item.habit.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${item.habit.scheduleType.name.lowercase()} · ${(item.stats.completionRate * 100).toInt()}% complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row {
                StreakBadge(streak = item.stats.currentStreak)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete habit")
                }
            }
        }
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, HabitScheduleType, HabitDifficulty, HabitImportance) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf(HabitScheduleType.DAILY) }
    var difficulty by remember { mutableStateOf(HabitDifficulty.MEDIUM) }
    var importance by remember { mutableStateOf(HabitImportance.MEDIUM) }
    var scheduleMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New habit") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                Row {
                    TextButton(onClick = { scheduleMenuExpanded = true }) { Text("Schedule: ${schedule.name}") }
                    DropdownMenu(expanded = scheduleMenuExpanded, onDismissRequest = { scheduleMenuExpanded = false }) {
                        HabitScheduleType.entries.forEach { type ->
                            DropdownMenuItem(text = { Text(type.name) }, onClick = { schedule = type; scheduleMenuExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onConfirm(title, description, schedule, difficulty, importance) }) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
