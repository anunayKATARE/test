package com.lifeos.app.feature.task.presentation

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.task.domain.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifeOSScaffold(
        topBar = { LifeOSTopBar(title = "Tasks") },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddSheet) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.selectDate(LocalDate.now().minusDays(1)) }) {
                        Text("Yesterday")
                    }
                    TextButton(onClick = { viewModel.selectDate(LocalDate.now()) }) {
                        Text("Today")
                    }
                    TextButton(onClick = { viewModel.selectDate(LocalDate.now().plusDays(1)) }) {
                        Text("Tomorrow")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (state.tasks.isEmpty()) {
                item {
                    Text(
                        "No tasks yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }
            items(state.tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggle = { viewModel.toggleCompleted(task) },
                    onEdit = { viewModel.openEditSheet(task) },
                    onDelete = { viewModel.deleteTask(task) },
                )
                Spacer(Modifier.height(8.dp))
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (state.showSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::dismissSheet) {
            TaskFormSheet(
                form = state.form,
                isEditing = state.editingTask != null,
                onTitleChange = viewModel::updateTitle,
                onDescriptionChange = viewModel::updateDescription,
                onTriggerInputChange = viewModel::updateTriggerInput,
                onAddTrigger = viewModel::addTrigger,
                onRemoveTrigger = viewModel::removeTrigger,
                onSave = viewModel::saveTask,
                onDismiss = viewModel::dismissSheet,
            )
        }
    }
}

@Composable
private fun TaskCard(task: Task, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = task.completed, onCheckedChange = { onToggle() })
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodyLarge.let {
                            if (task.completed) it.copy(textDecoration = TextDecoration.LineThrough) else it
                        },
                        color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskFormSheet(
    form: TaskFormState,
    isEditing: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTriggerInputChange: (String) -> Unit,
    onAddTrigger: () -> Unit,
    onRemoveTrigger: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
        Text(
            if (isEditing) "Edit Task" else "New Task",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = form.title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = form.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Triggers — one-word things that could make you fail this task",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = form.triggerInput,
                onValueChange = onTriggerInputChange,
                label = { Text("Add trigger") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onAddTrigger, enabled = form.triggerInput.isNotBlank()) {
                Text("Add")
            }
        }
        if (form.triggers.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                form.triggers.forEach { trigger ->
                    InputChip(
                        selected = false,
                        onClick = {},
                        label = { Text(trigger) },
                        trailingIcon = {
                            IconButton(onClick = { onRemoveTrigger(trigger) }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
            androidx.compose.material3.Button(
                onClick = onSave,
                enabled = form.title.isNotBlank(),
                modifier = Modifier.weight(1f),
            ) { Text(if (isEditing) "Save" else "Create") }
        }
        Spacer(Modifier.height(32.dp))
    }
}
