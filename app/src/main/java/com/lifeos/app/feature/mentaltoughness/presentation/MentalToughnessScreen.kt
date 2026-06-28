package com.lifeos.app.feature.mentaltoughness.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.lifeos.app.core.common.DateTimeUtils
import com.lifeos.app.core.ui.components.EmptyState
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessEntry
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentalToughnessScreen(viewModel: MentalToughnessViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LifeOSTopBar(title = "Mental Toughness") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Log entry")
            }
        },
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyState(message = "Log discomfort challenges, fears faced, and setbacks recovered from.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
                items(entries, key = { it.id }) { entry ->
                    EntryRow(entry = entry, onDelete = { viewModel.deleteEntry(entry.id) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddEntryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, title, description, outcome, lesson ->
                viewModel.logEntry(type, title, description, outcome, lesson)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun EntryRow(entry: MentalToughnessEntry, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = entry.title, style = MaterialTheme.typography.titleMedium)
                Text(text = entry.type.name.replace('_', ' '), style = MaterialTheme.typography.labelSmall)
                Text(text = DateTimeUtils.formatDisplayDateTime(entry.dateTime), style = MaterialTheme.typography.bodySmall)
                if (entry.lessonLearned.isNotBlank()) {
                    Text(text = "Lesson: ${entry.lessonLearned}", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete entry")
            }
        }
    }
}

@Composable
private fun AddEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (MentalToughnessType, String, String, String, String) -> Unit,
) {
    var type by remember { mutableStateOf(MentalToughnessType.DISCOMFORT_CHALLENGE) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("") }
    var lesson by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New entry") },
        text = {
            Column {
                TextButton(onClick = { typeMenuExpanded = true }) { Text("Type: ${type.name.replace('_', ' ')}") }
                DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    MentalToughnessType.entries.forEach { t ->
                        DropdownMenuItem(text = { Text(t.name.replace('_', ' ')) }, onClick = { type = t; typeMenuExpanded = false })
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = outcome, onValueChange = { outcome = it }, label = { Text("Outcome") })
                OutlinedTextField(value = lesson, onValueChange = { lesson = it }, label = { Text("Lesson learned") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onConfirm(type, title, description, outcome, lesson) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
