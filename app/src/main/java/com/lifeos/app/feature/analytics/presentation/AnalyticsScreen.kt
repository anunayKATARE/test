package com.lifeos.app.feature.analytics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Analytics") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
            item {
                LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(text = "Journal entries", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${state.totalJournalEntries}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            item { SectionHeader("Mood frequency (last 90 days)") }
            items(state.emotionFrequency.entries.toList()) { (emotion, count) ->
                BarRow(label = emotion.name.lowercase(), value = count, maxValue = state.emotionFrequency.values.maxOrNull() ?: 1)
            }

            item { SectionHeader("Common mood triggers") }
            items(state.commonTriggers.entries.toList()) { (trigger, count) ->
                BarRow(label = trigger, value = count, maxValue = state.commonTriggers.values.maxOrNull() ?: 1)
            }

            item { SectionHeader("Habit completion rates") }
            items(state.habitStats) { stat ->
                BarRow(
                    label = "${stat.title} (streak ${stat.currentStreak})",
                    value = (stat.completionRate * 100).toInt(),
                    maxValue = 100,
                )
            }

            item { SectionHeader("Goals by status") }
            items(state.goalsByStatus.entries.toList()) { (status, count) ->
                BarRow(label = status.name, value = count, maxValue = state.goalsByStatus.values.maxOrNull() ?: 1)
            }
        }
    }
}

@Composable
private fun BarRow(label: String, value: Int, maxValue: Int) {
    val fraction = if (maxValue == 0) 0f else (value.toFloat() / maxValue).coerceIn(0f, 1f)
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column {
            Text(text = "$label: $value", style = MaterialTheme.typography.bodyMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}
