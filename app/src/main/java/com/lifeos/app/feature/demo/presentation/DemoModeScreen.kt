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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
            // Your Profiles section
            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Your Profiles",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
            }
            items(state.userProfiles, key = { it.id }) { profile ->
                UserProfileCard(
                    profile = profile,
                    isActive = state.activeProfile?.id == profile.id,
                    isDefault = profile.id == viewModel.defaultProfileId,
                    onSwitch = { viewModel.activateProfile(profile) },
                    onRename = { viewModel.startRename(profile) },
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
                Text(
                    "Demo Profiles",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Explore LifeOS with pre-filled sample data. Your real data stays safe.",
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

    state.renamingProfile?.let { profile ->
        RenameProfileDialog(
            currentName = profile.name,
            onConfirm = { viewModel.renameProfile(profile.id, it) },
            onDismiss = viewModel::dismissRenameDialog,
        )
    }

    if (state.showCreateDialog) {
        CreateProfileDialog(
            onConfirm = viewModel::createUserProfile,
            onDismiss = viewModel::dismissCreateDialog,
        )
    }
}

@Composable
private fun UserProfileCard(
    profile: Profile,
    isActive: Boolean,
    isDefault: Boolean,
    onSwitch: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val cardColors = if (isActive) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = cardColors) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                    )
                    if (isActive) {
                        Text(
                            "Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (isDefault) {
                        Text(
                            "Default profile",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row {
                    IconButton(onClick = onRename) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Rename",
                            tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!isDefault) {
                        IconButton(onClick = onDelete, enabled = !isActive) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = if (isActive) MaterialTheme.colorScheme.outlineVariant
                                       else MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
            if (!isActive) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onSwitch, modifier = Modifier.fillMaxWidth()) {
                    Text("Switch to ${profile.name}")
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
            Text(
                template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            if (isActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Active",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
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
private fun RenameProfileDialog(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Profile") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
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
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
