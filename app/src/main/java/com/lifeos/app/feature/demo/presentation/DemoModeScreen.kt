package com.lifeos.app.feature.demo.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.app.core.demo.DemoProfile
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoModeScreen(viewModel: DemoModeViewModel = hiltViewModel()) {
    val activeProfile by viewModel.activeProfile.collectAsState()

    LifeOSScaffold(
        topBar = { LifeOSTopBar(title = "Demo Mode") },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth()) {
            val active = activeProfile
            if (active != null) {
                Text("Demo mode is active: ${active.label}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your own goals, habits, moods, and other entries are hidden while a demo " +
                        "profile is active. Exit demo mode to bring them back.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.deactivate() }) { Text("Exit Demo Mode") }
            } else {
                Text(
                    "Explore LifeOS filled with sample data. Starting a demo replaces your view " +
                        "with sample entries — your own data is hidden, not deleted, and comes back " +
                        "the moment you exit.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                viewModel.profiles.forEach { profile ->
                    DemoProfileCard(profile = profile, onActivate = { viewModel.activate(profile) })
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DemoProfileCard(profile: DemoProfile, onActivate: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(profile.label, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(profile.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onActivate) { Text("Start ${profile.label}") }
        }
    }
}
