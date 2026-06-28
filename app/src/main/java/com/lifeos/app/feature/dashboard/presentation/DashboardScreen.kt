package com.lifeos.app.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.app.core.common.DateTimeUtils
import com.lifeos.app.core.ui.components.ConsistencyHeatmap
import com.lifeos.app.core.ui.components.SectionHeader
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.inspiration.presentation.InspirationCarousel
import com.lifeos.app.feature.mood.domain.MoodEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("LifeOS") }) },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
            item {
                Text(
                    text = DateTimeUtils.formatDisplayDate(java.time.LocalDate.now()),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
            item {
                InspirationCarousel(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Consistency", style = MaterialTheme.typography.titleSmall)
                        ConsistencyHeatmap(intensityByDate = state.heatmap, weeks = 12, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            item { SectionHeader("Today's goals") }
            if (state.todaysGoals.isEmpty()) {
                item { Text("No goals for today.", modifier = Modifier.padding(horizontal = 16.dp)) }
            } else {
                items(state.todaysGoals, key = { "goal_${it.id}" }) { goal -> GoalSummaryRow(goal) }
            }
            item { SectionHeader("Today's habits") }
            if (state.todaysHabits.isEmpty()) {
                item { Text("No habits scheduled today.", modifier = Modifier.padding(horizontal = 16.dp)) }
            } else {
                items(state.todaysHabits, key = { "habit_${it.habit.id}" }) { item ->
                    HabitSummaryRow(item.habit.title, item.completedToday) { viewModel.toggleHabit(item.habit, item.completedToday) }
                }
            }
            item { SectionHeader("Recent moods") }
            if (state.recentMoodEntries.isEmpty()) {
                item { Text("No mood entries logged yet.", modifier = Modifier.padding(horizontal = 16.dp)) }
            } else {
                items(state.recentMoodEntries, key = { "mood_${it.id}" }) { mood -> MoodSummaryRow(mood) }
            }
            item { SectionHeader("Open problems: ${state.openProblemsCount}") }
        }
    }
}

@Composable
private fun GoalSummaryRow(goal: Goal) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(text = goal.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun HabitSummaryRow(title: String, completed: Boolean, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(8.dp))
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Toggle habit",
                    tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MoodSummaryRow(mood: MoodEntry) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = "${mood.emotion.name.lowercase()} · ${mood.intensity}/10",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(12.dp),
        )
    }
}
