package com.lifeos.app.feature.problemsolver.presentation

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import com.lifeos.app.feature.problemsolver.domain.Problem
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemScreen(viewModel: ProblemViewModel = hiltViewModel()) {
    val problems by viewModel.problems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var attemptDialogFor by remember { mutableStateOf<Problem?>(null) }

    Scaffold(
        topBar = { LifeOSTopBar(title = "Problem Solver") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Register problem")
            }
        },
    ) { padding ->
        if (problems.isEmpty()) {
            EmptyState(message = "No problems tracked yet. Tap + to register one.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
                items(problems, key = { it.id }) { problem ->
                    ProblemRow(
                        problem = problem,
                        onRecordAttempt = { attemptDialogFor = problem },
                        onResolve = { viewModel.setStatus(problem.id, ProblemStatus.RESOLVED) },
                        onDelete = { viewModel.deleteProblem(problem.id) },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        RegisterProblemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description ->
                viewModel.registerProblem(title, description)
                showAddDialog = false
            },
        )
    }

    attemptDialogFor?.let { problem ->
        RecordAttemptDialog(
            onDismiss = { attemptDialogFor = null },
            onConfirm = { attempt, worked ->
                viewModel.recordAttempt(problem.id, attempt, worked)
                attemptDialogFor = null
            },
        )
    }
}

@Composable
private fun ProblemRow(problem: Problem, onRecordAttempt: () -> Unit, onResolve: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = problem.title, style = MaterialTheme.typography.titleMedium)
                    Text(text = problem.status.name, style = MaterialTheme.typography.labelSmall)
                }
                Row {
                    IconButton(onClick = onResolve) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Mark resolved", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete problem")
                    }
                }
            }
            if (problem.description.isNotBlank()) {
                Text(text = problem.description, style = MaterialTheme.typography.bodySmall)
            }
            if (problem.attemptsMade.isNotEmpty()) {
                Text(text = "Attempts: ${problem.attemptsMade.size}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onRecordAttempt) { Text("Record attempt") }
        }
    }
}

@Composable
private fun RegisterProblemDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register problem") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onConfirm(title, description) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun RecordAttemptDialog(onDismiss: () -> Unit, onConfirm: (String, Boolean?) -> Unit) {
    var attempt by remember { mutableStateOf("") }
    var worked by remember { mutableStateOf<Boolean?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record attempt") },
        text = {
            Column {
                OutlinedTextField(value = attempt, onValueChange = { attempt = it }, label = { Text("What did you try?") })
                Row {
                    TextButton(onClick = { worked = true }) { Text(if (worked == true) "✓ Worked" else "Worked") }
                    TextButton(onClick = { worked = false }) { Text(if (worked == false) "✓ Failed" else "Failed") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (attempt.isNotBlank()) onConfirm(attempt, worked) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
