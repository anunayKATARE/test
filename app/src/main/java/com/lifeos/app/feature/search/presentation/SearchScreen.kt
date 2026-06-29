package com.lifeos.app.feature.search.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.lifeos.app.core.ui.components.EmptyState
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.results.collectAsState()

    LifeOSScaffold(topBar = { TopAppBar(title = { Text("Search") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; viewModel.search(it) },
                    label = { androidx.compose.material3.Text("Search journal, goals, habits, problems") },
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                )
            }
            val r = results
            if (r == null) {
                item { EmptyState(message = "Type to search across your journal, goals, habits, and problems.") }
            } else if (r.journalEntries.isEmpty() && r.goals.isEmpty() && r.habits.isEmpty() && r.problems.isEmpty()) {
                item { EmptyState(message = "No matches found.") }
            } else {
                if (r.journalEntries.isNotEmpty()) {
                    item { SectionHeader("Journal") }
                    items(r.journalEntries, key = { "j_${it.id}" }) { entry ->
                        ResultRow(title = entry.title, subtitle = entry.body)
                    }
                }
                if (r.goals.isNotEmpty()) {
                    item { SectionHeader("Goals") }
                    items(r.goals, key = { "g_${it.id}" }) { goal ->
                        ResultRow(title = goal.title, subtitle = goal.description)
                    }
                }
                if (r.habits.isNotEmpty()) {
                    item { SectionHeader("Habits") }
                    items(r.habits, key = { "h_${it.id}" }) { habit ->
                        ResultRow(title = habit.title, subtitle = habit.description)
                    }
                }
                if (r.problems.isNotEmpty()) {
                    item { SectionHeader("Problems") }
                    items(r.problems, key = { "p_${it.id}" }) { problem ->
                        ResultRow(title = problem.title, subtitle = problem.description)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(title: String, subtitle: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            if (subtitle.isNotBlank()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}
