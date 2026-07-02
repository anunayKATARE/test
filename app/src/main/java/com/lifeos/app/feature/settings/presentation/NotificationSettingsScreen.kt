package com.lifeos.app.feature.settings.presentation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.settings.domain.SoundProfile
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val testTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

@Composable
fun NotificationSettingsScreen(viewModel: NotificationSettingsViewModel = hiltViewModel()) {
    val currentProfile by viewModel.soundProfile.collectAsStateWithLifecycle()
    val diagnostics by viewModel.diagnostics.collectAsStateWithLifecycle()
    val testAlarmAt by viewModel.testAlarmScheduledAt.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.refreshDiagnostics() }

    LifeOSScaffold(topBar = { LifeOSTopBar(title = "Notification Settings") }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("Alert sound profile", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Controls how task alarms sound, including on your Galaxy Watch.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
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

            Spacer(Modifier.height(24.dp))
            Text("Status checks", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "All three must be green for task alarms to fire reliably.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            LifeOSCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    StatusRow(
                        ok = diagnostics.notificationsEnabled,
                        label = "Notifications allowed",
                        fixLabel = "Enable",
                        onFix = {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                },
                            )
                        },
                    )
                    StatusRow(
                        ok = diagnostics.exactAlarmsAllowed,
                        label = "Exact alarms allowed",
                        fixLabel = "Allow",
                        onFix = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                context.startActivity(
                                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    },
                                )
                            }
                        },
                    )
                    StatusRow(
                        ok = diagnostics.batteryUnrestricted,
                        label = "Battery optimization off",
                        fixLabel = "Fix",
                        onFix = {
                            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        },
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = viewModel::refreshDiagnostics) { Text("Refresh") }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Test", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Test now checks channel + watch delivery instantly. Test alarm goes through " +
                    "the full alarm pipeline and should appear in about 1 minute — lock your phone to test Doze.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::sendTestNotification, modifier = Modifier.weight(1f)) {
                    Text("Test now")
                }
                OutlinedButton(onClick = viewModel::scheduleTestAlarm, modifier = Modifier.weight(1f)) {
                    Text("Test alarm (1 min)")
                }
            }
            testAlarmAt?.let { at ->
                Spacer(Modifier.height(8.dp))
                Text(
                    "Test alarm scheduled — expect a notification at ${testTimeFormatter.format(at)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatusRow(ok: Boolean, label: String, fixLabel: String, onFix: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (ok) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = null,
            tint = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (!ok) {
            TextButton(onClick = onFix) { Text(fixLabel) }
        }
    }
}
