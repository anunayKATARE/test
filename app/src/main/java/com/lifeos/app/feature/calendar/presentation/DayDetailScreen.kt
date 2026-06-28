package com.lifeos.app.feature.calendar.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.app.core.common.DateTimeUtils
import com.lifeos.app.core.ui.components.EmptyState
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.inspiration.presentation.InspirationCarousel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(viewModel: DayDetailViewModel = hiltViewModel()) {
    val dayItems by viewModel.items.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LifeOSTopBar(title = DateTimeUtils.formatDisplayDate(viewModel.date)) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            InspirationCarousel(modifier = Modifier.fillMaxWidth().padding(16.dp))
            if (dayItems.isEmpty()) {
                EmptyState(message = "No tasks or habits for this day.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(dayItems, key = { "${it.isHabit}_${it.id}" }) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            Checkbox(checked = item.completed, onCheckedChange = { viewModel.toggle(item) })
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title ->
                viewModel.addTask(title)
                showAddTaskDialog = false
            },
        )
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New task") },
        text = { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }) },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onConfirm(title) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
