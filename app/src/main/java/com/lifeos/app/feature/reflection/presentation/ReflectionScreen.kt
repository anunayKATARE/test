package com.lifeos.app.feature.reflection.presentation

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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.app.core.common.DateTimeUtils
import com.lifeos.app.core.ui.components.EmptyState
import com.lifeos.app.feature.reflection.domain.ReflectionEntry
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import com.lifeos.app.feature.reflection.domain.ReflectionTemplates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen(viewModel: ReflectionViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    var activeTemplate by remember { mutableStateOf<ReflectionTemplateType?>(null) }
    var templateMenuExpanded by remember { mutableStateOf(false) }

    val template = activeTemplate
    if (template != null) {
        ReflectionForm(
            templateType = template,
            onBack = { activeTemplate = null },
            onSave = { title, answers ->
                viewModel.saveEntry(template, title, answers)
                activeTemplate = null
            },
        )
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reflection Library") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { templateMenuExpanded = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Start reflection")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxWidth()) {
            DropdownMenu(expanded = templateMenuExpanded, onDismissRequest = { templateMenuExpanded = false }) {
                ReflectionTemplateType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name.replace('_', ' ')) },
                        onClick = { activeTemplate = type; templateMenuExpanded = false },
                    )
                }
            }
            if (entries.isEmpty()) {
                EmptyState(message = "Tap + to start a guided reflection from 16 templates.")
            } else {
                LazyColumn {
                    items(entries, key = { it.id }) { entry ->
                        EntryRow(entry = entry, onDelete = { viewModel.deleteEntry(entry.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: ReflectionEntry, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = entry.title.ifBlank { entry.templateType.name.replace('_', ' ') },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(text = DateTimeUtils.formatDisplayDateTime(entry.dateTime), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete reflection")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReflectionForm(
    templateType: ReflectionTemplateType,
    onBack: () -> Unit,
    onSave: (String, Map<String, String>) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    val prompts = ReflectionTemplates.prompts[templateType].orEmpty()
    val answers = remember { mutableStateMapOf<String, String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(templateType.name.replace('_', ' ')) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (optional)") }, modifier = Modifier.fillMaxWidth())
            prompts.forEach { prompt ->
                OutlinedTextField(
                    value = answers[prompt] ?: "",
                    onValueChange = { answers[prompt] = it },
                    label = { Text(prompt) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            TextButton(onClick = { onSave(title, answers.toMap()) }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
        }
    }
}
