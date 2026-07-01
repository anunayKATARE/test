package com.lifeos.app.feature.demo.presentation

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.core.demo.DemoTemplate
import com.lifeos.app.core.demo.Profile
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoModeScreen(viewModel: DemoModeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifeOSScaffold(topBar = { LifeOSTopBar(title = "Profiles") }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Active profile banner
            item {
                Spacer(Modifier.height(12.dp))
                ActiveProfileBanner(
                    activeProfile = state.activeProfile,
                    onDeactivate = viewModel::deactivate,
                )
                Spacer(Modifier.height(20.dp))
            }

            // User profiles section
            item {
                Text("Your Profiles", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }
            if (state.userProfiles.isEmpty()) {
                item {
                    Text(
                        "No custom profiles yet. Create one to separate different areas of your life.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            items(state.userProfiles, key = { it.id }) { profile ->
                UserProfileCard(
                    profile = profile,
                    isActive = state.activeProfile?.id == profile.id,
                    onActivate = { viewModel.activateProfile(profile) },
                    onDeactivate = viewModel::deactivate,
                    onDelete = { viewModel.deleteProfile(profile.id) },
                )
                Spacer(Modifier.height(8.dp))
            }
            item {
                OutlinedButton(
                    onClick = viewModel::openCreateDialog,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("+ Add Profile") }
                Spacer(Modifier.height(24.dp))
            }

            // Demo templates section
            item {
                Text("Demo Profiles", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Explore LifeOS with pre-filled sample data. Your real data stays safe and comes back when you exit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
            }
            items(viewModel.demoTemplates, key = { it.id }) { template ->
                DemoTemplateCard(
                    template = template,
                    isActive = state.activeProfile?.demoTemplate == template,
                    onActivate = { viewModel.activateDemoTemplate(template) },
                    onDeactivate = viewModel::deactivate,
                )
                Spacer(Modifier.height(8.dp))
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (state.showCreateDialog) {
        CreateProfileDialog(
            onConfirm = viewModel::createUserProfile,
            onDismiss = viewModel::dismissCreateDialog,
        )
    }
}

@Composable
private fun ActiveProfileBanner(activeProfile: Profile?, onDeactivate: () -> Unit) {
    if (activeProfile == null) {
        LifeOSCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Filled.Person, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
                Text("Viewing your own data", style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Active: ${activeProfile.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (activeProfile.isDemo) "Demo data is shown. Your own entries are hidden, not deleted."
                    else "This profile's data is shown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDeactivate,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                ) { Text("Exit to My Data") }
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    profile: Profile,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit,
) {
    LifeOSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                if (isActive) {
                    Text("Active", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Row {
                if (isActive) {
                    TextButton(onClick = onDeactivate) { Text("Exit") }
                } else {
                    TextButton(onClick = onActivate) { Text("Switch") }
                }
                IconButton(onClick = onDelete, enabled = !isActive) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete",
                        tint = if (isActive) MaterialTheme.colorScheme.outlineVariant
                        else MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun DemoTemplateCard(
    template: DemoTemplate,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
) {
    LifeOSCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(template.label, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(template.description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            if (isActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Active", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterVertically))
                    OutlinedButton(onClick = onDeactivate) { Text("Exit Demo") }
                }
            } else {
                Button(onClick = onActivate, modifier = Modifier.fillMaxWidth()) {
                    Text("Start ${template.label}")
                }
            }
        }
    }
}

@Composable
private fun CreateProfileDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Profile") },
        text = {
            Column {
                Text(
                    "Create a named profile to separate different areas of your life (e.g. Work, Self Growth).",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
