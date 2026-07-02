package com.lifeos.app.feature.timelog.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.timelog.domain.TimeLog
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLogScreen(viewModel: TimeLogViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifeOSScaffold(
        topBar = {
            LifeOSTopBar(title = "Time Log") {
                IconButton(onClick = viewModel::toggleLoggingEnabled) {
                    Icon(
                        if (state.loggingEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                        contentDescription = if (state.loggingEnabled) "Disable 30-min logging prompts" else "Enable 30-min logging prompts",
                        tint = if (state.loggingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        floatingActionButton = {
            if (state.activeTimer == null) {
                FloatingActionButton(onClick = viewModel::openStartSheet) {
                    Icon(Icons.Filled.Add, contentDescription = "Start timer")
                }
            }
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            state.activeTimer?.let { timer ->
                item {
                    Spacer(Modifier.height(8.dp))
                    ActiveTimerCard(timer = timer, onStop = viewModel::stopTimer)
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                Text("Today's Log", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

            if (state.todaysLogs.isEmpty() && state.activeTimer == null) {
                item {
                    Text(
                        "No entries yet. Tap + to start tracking your time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            items(state.todaysLogs.filter { !it.isActive }, key = { it.id }) { log ->
                TimeLogCard(log = log, onDelete = { viewModel.deleteLog(log.id) })
                Spacer(Modifier.height(8.dp))
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (state.showStartSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::dismissStartSheet) {
            StartTimerSheet(
                tasks = state.todaysTasks.map { it.id to it.title },
                recentChores = state.recentChores,
                chore = state.entryChore,
                selectedTaskId = state.entryTaskId,
                onChoreChange = viewModel::updateEntryChore,
                onTaskSelect = viewModel::selectEntryTask,
                onStartTimer = viewModel::startTimer,
                onLogManual = viewModel::logManual,
                onDismiss = viewModel::dismissStartSheet,
            )
        }
    }
}

@Composable
private fun ActiveTimerCard(timer: TimeLog, onStop: () -> Unit) {
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(timer.id) {
        while (true) {
            elapsedSeconds = Duration.between(timer.startedAt, Instant.now()).seconds
            delay(1000L)
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        timer.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        formatElapsed(elapsedSeconds),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Button(onClick = onStop) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Stop")
            }
        }
    }
}

@Composable
private fun TimeLogCard(log: TimeLog, onDelete: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.label, style = MaterialTheme.typography.bodyLarge)
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                val start = timeFormatter.format(log.startedAt)
                val end = log.endedAt?.let { timeFormatter.format(it) } ?: "…"
                val duration = log.endedAt?.let {
                    val secs = Duration.between(log.startedAt, it).seconds
                    formatElapsed(secs)
                } ?: ""
                Text(
                    "$start – $end  $duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun StartTimerSheet(
    tasks: List<Pair<String, String>>,
    recentChores: List<String>,
    chore: String,
    selectedTaskId: String?,
    onChoreChange: (String) -> Unit,
    onTaskSelect: (String?) -> Unit,
    onStartTimer: () -> Unit,
    onLogManual: () -> Unit,
    onDismiss: () -> Unit,
) {
    var useChore by remember { mutableStateOf(selectedTaskId == null) }
    var taskDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
        Text("What are you working on?", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = useChore, onClick = { useChore = true; onTaskSelect(null) }, label = { Text("Chore") })
            FilterChip(selected = !useChore, onClick = { useChore = false }, label = { Text("Task") })
        }
        Spacer(Modifier.height(12.dp))

        if (useChore) {
            OutlinedTextField(
                value = chore,
                onValueChange = onChoreChange,
                label = { Text("Chore (1-2 words)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (recentChores.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Recent:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentChores.forEach { c ->
                        FilterChip(selected = chore == c, onClick = { onChoreChange(c) }, label = { Text(c) })
                    }
                }
            }
        } else {
            ExposedDropdownMenuBox(
                expanded = taskDropdownExpanded,
                onExpandedChange = { taskDropdownExpanded = it },
            ) {
                val selectedLabel = tasks.firstOrNull { it.first == selectedTaskId }?.second ?: "Select task"
                OutlinedTextField(
                    value = selectedLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Task") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(expanded = taskDropdownExpanded, onDismissRequest = { taskDropdownExpanded = false }) {
                    tasks.forEach { (id, title) ->
                        DropdownMenuItem(
                            text = { Text(title) },
                            onClick = { onTaskSelect(id); taskDropdownExpanded = false },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        val canSubmit = if (useChore) chore.isNotBlank() else selectedTaskId != null
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(onClick = onStartTimer, enabled = canSubmit, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Timer, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Start Timer")
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onLogManual, enabled = canSubmit, modifier = Modifier.fillMaxWidth()) {
            Text("Log last 30 min instead")
        }
        Spacer(Modifier.height(24.dp))
    }
}

private fun formatElapsed(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

@Composable
fun TimeLogPromptDialog(
    onDismiss: () -> Unit,
    viewModel: TimeLogPromptViewModel = hiltViewModel(),
) {
    var choreInput by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = { viewModel.dismiss() },
        title = { Text("What have you been doing?") },
        text = {
            Column {
                Text(
                    "You haven't logged in 30 minutes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = choreInput,
                    onValueChange = { choreInput = it },
                    label = { Text("What were you doing?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.logChore(choreInput) },
                enabled = choreInput.isNotBlank(),
            ) { Text("Log") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismiss(); onDismiss() }) { Text("Dismiss") }
        },
    )
}
