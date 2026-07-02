package com.lifeos.app.feature.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.settings.domain.SoundProfile

@Composable
fun NotificationSettingsScreen(viewModel: NotificationSettingsViewModel = hiltViewModel()) {
    val currentProfile by viewModel.soundProfile.collectAsStateWithLifecycle()

    LifeOSScaffold(topBar = { LifeOSTopBar(title = "Notification Settings") }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Alert sound profile", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Controls how task alarms sound, including on your Galaxy Watch.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            LifeOSCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SoundProfile.entries.forEach { profile ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = currentProfile == profile,
                                onClick = { viewModel.setSoundProfile(profile) },
                            )
                            Text(profile.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
