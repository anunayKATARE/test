package com.lifeos.app.feature.plan.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.task.domain.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDayScreen(
    onBack: () -> Unit,
    viewModel: PlanDayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tomorrow = LocalDate.now().plusDays(1)
    val dateLabel = tomorrow.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    LifeOSScaffold(topBar = { LifeOSTopBar(title = "Plan Tomorrow") }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(dateLabel, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Select the tasks you plan to work on and set your intentions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Text("Tasks for tomorrow", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
            }

            if (state.tomorrowsTasks.isEmpty()) {
                item {
                    Text(
                        "No tasks scheduled for tomorrow yet. Add some from the Tasks screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            items(state.tomorrowsTasks, key = { it.id }) { task ->
                TaskPlanRow(
                    task = task,
                    isSelected = task.id in state.selectedTaskIds,
                    onToggle = { viewModel.toggleTask(task.id) },
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.intentions,
                    onValueChange = viewModel::updateIntentions,
                    label = { Text("Intentions / notes for tomorrow") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.savePlan(); onBack() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save Day Plan")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TaskPlanRow(task: Task, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(modifier = Modifier.weight(1f).padding(top = 12.dp)) {
            Text(task.title, style = MaterialTheme.typography.bodyLarge)
            if (task.description.isNotBlank()) {
                Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (task.triggers.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "Triggers: ${task.triggers.joinToString(" · ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun PlanReminderDialog(
    onPlanNow: () -> Unit,
    onSnooze30: () -> Unit,
    onSnooze60: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plan Your Day") },
        text = {
            Text(
                "It's 9 PM — time to plan tomorrow! Select your tasks and set your intentions for the day ahead.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(onClick = onPlanNow) { Text("Plan Now") }
        },
        dismissButton = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                androidx.compose.material3.TextButton(onClick = onSnooze30) { Text("Remind in 30 min") }
                androidx.compose.material3.TextButton(onClick = onSnooze60) { Text("Remind in 1 hr") }
            }
        },
    )
}
