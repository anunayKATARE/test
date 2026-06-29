package com.lifeos.app.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.app.core.common.DateTimeUtils
import com.lifeos.app.core.ui.components.CalendarMonthGrid
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.SectionHeader
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.inspiration.presentation.InspirationCarousel
import com.lifeos.app.feature.mood.domain.MoodEntry
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel(), onDayClick: (LocalDate) -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()

    LifeOSScaffold(
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
                LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { viewModel.previousMonth() }) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                        }
                        Text(
                            text = DateTimeUtils.formatMonthTitle(state.heatmapMonth),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                        }
                    }
                    val maxIntensity = (state.heatmap.values.maxOrNull() ?: 1).coerceAtLeast(1)
                    CalendarMonthGrid(
                        month = state.heatmapMonth,
                        intensityByDate = state.heatmap.mapValues { (_, count) -> (count.toFloat() / maxIntensity).coerceIn(0f, 1f) },
                        onDayClick = onDayClick,
                        modifier = Modifier.padding(top = 8.dp),
                    )
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
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(text = goal.title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun HabitSummaryRow(title: String, completed: Boolean, onToggle: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onToggle) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = "Toggle habit",
                        tint = if (completed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodSummaryRow(mood: MoodEntry) {
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = "${mood.emotion.name.lowercase()} · ${mood.intensity}/10",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
