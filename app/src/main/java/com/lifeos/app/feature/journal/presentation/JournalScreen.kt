package com.lifeos.app.feature.journal.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.TopAppBar
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
import com.lifeos.app.feature.journal.domain.JournalEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: JournalViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var query by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    if (showAddForm) {
        AddJournalEntryForm(
            onBack = { showAddForm = false },
            onSave = { title, body, tags, location, weather ->
                viewModel.createEntry(title, body, tags, location, weather)
                showAddForm = false
            },
        )
        return
    }

    val displayedEntries = searchResults ?: entries

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Journal") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddForm = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New journal entry")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; viewModel.search(it) },
                label = { Text("Search journal") },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            )
            if (displayedEntries.isEmpty()) {
                EmptyState(message = "No journal entries yet. Tap + to write your first one.")
            } else {
                LazyColumn {
                    items(displayedEntries, key = { it.id }) { entry ->
                        JournalRow(entry = entry, onDelete = { viewModel.deleteEntry(entry.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalRow(entry: JournalEntry, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = entry.title, style = MaterialTheme.typography.titleMedium)
                Text(text = DateTimeUtils.formatDisplayDateTime(entry.dateTime), style = MaterialTheme.typography.bodySmall)
                Text(text = entry.body, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                if (entry.tags.isNotEmpty()) {
                    Text(text = entry.tags.joinToString(", ") { "#$it" }, style = MaterialTheme.typography.labelSmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete entry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddJournalEntryForm(
    onBack: () -> Unit,
    onSave: (String, String, List<String>, String?, String?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New entry") },
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
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("What's on your mind?") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            OutlinedTextField(value = tagsText, onValueChange = { tagsText = it }, label = { Text("Tags (comma separated)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = weather, onValueChange = { weather = it }, label = { Text("Weather") }, modifier = Modifier.fillMaxWidth())
            TextButton(
                onClick = {
                    val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onSave(title, body, tags, location.ifBlank { null }, weather.ifBlank { null })
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
        }
    }
}
