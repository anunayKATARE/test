package com.lifeos.app.feature.selfbelief.presentation

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
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.selfbelief.domain.SelfBeliefReflection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfBeliefScreen(viewModel: SelfBeliefViewModel = hiltViewModel()) {
    val reflections by viewModel.reflections.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }

    if (showAddForm) {
        AddReflectionForm(
            onBack = { showAddForm = false },
            onSave = { whatHappened, story, evFor, evAgainst, advice, strengths, action ->
                viewModel.addReflection(whatHappened, story, evFor, evAgainst, advice, strengths, action)
                showAddForm = false
            },
        )
        return
    }

    LifeOSScaffold(
        topBar = { LifeOSTopBar(title = "Self-Belief") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddForm = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New reflection")
            }
        },
    ) { padding ->
        if (reflections.isEmpty()) {
            EmptyState(message = "Challenge self-doubt with evidence-based reflection.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
                items(reflections, key = { it.id }) { reflection ->
                    ReflectionRow(reflection = reflection, onDelete = { viewModel.deleteReflection(reflection.id) })
                }
            }
        }
    }
}

@Composable
private fun ReflectionRow(reflection: SelfBeliefReflection, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = reflection.whatHappened, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(text = DateTimeUtils.formatDisplayDateTime(reflection.dateTime), style = MaterialTheme.typography.bodySmall)
                if (reflection.nextSmallAction.isNotBlank()) {
                    Text(text = "Next: ${reflection.nextSmallAction}", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete reflection")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReflectionForm(
    onBack: () -> Unit,
    onSave: (String, String, String, String, String, String, String) -> Unit,
) {
    var whatHappened by remember { mutableStateOf("") }
    var storyTelling by remember { mutableStateOf("") }
    var evidenceFor by remember { mutableStateOf("") }
    var evidenceAgainst by remember { mutableStateOf("") }
    var friendAdvice by remember { mutableStateOf("") }
    var strengthsThatRemain by remember { mutableStateOf("") }
    var nextSmallAction by remember { mutableStateOf("") }

    LifeOSScaffold(
        topBar = {
            TopAppBar(
                title = { Text("New reflection") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            OutlinedTextField(value = whatHappened, onValueChange = { whatHappened = it }, label = { Text("What happened?") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = storyTelling, onValueChange = { storyTelling = it }, label = { Text("What story am I telling myself?") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = evidenceFor, onValueChange = { evidenceFor = it }, label = { Text("Evidence for that story") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = evidenceAgainst, onValueChange = { evidenceAgainst = it }, label = { Text("Evidence against it") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = friendAdvice, onValueChange = { friendAdvice = it }, label = { Text("What would I tell a friend?") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = strengthsThatRemain, onValueChange = { strengthsThatRemain = it }, label = { Text("Strengths that remain true") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nextSmallAction, onValueChange = { nextSmallAction = it }, label = { Text("Next small action") }, modifier = Modifier.fillMaxWidth())
            TextButton(
                onClick = { onSave(whatHappened, storyTelling, evidenceFor, evidenceAgainst, friendAdvice, strengthsThatRemain, nextSmallAction) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
        }
    }
}
