package com.lifeos.app.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.lifeos.app.feature.goal.presentation.GoalViewModel
import com.lifeos.app.feature.habit.presentation.HabitViewModel
import com.lifeos.app.feature.inspiration.presentation.InspirationCarousel
import com.lifeos.app.feature.mood.domain.MoodEntry
import com.lifeos.app.feature.mood.presentation.MoodViewModel
import com.lifeos.app.feature.problemsolver.domain.Problem
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import com.lifeos.app.feature.problemsolver.presentation.ProblemViewModel
import java.time.LocalDate

private const val MAX_SECTION_ITEMS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    goalViewModel: GoalViewModel = hiltViewModel(),
    habitViewModel: HabitViewModel = hiltViewModel(),
    moodViewModel: MoodViewModel = hiltViewModel(),
    problemViewModel: ProblemViewModel = hiltViewModel(),
    onDayClick: (LocalDate) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var selectedGoal by remember { mutableStateOf<Goal?>(null) }
    var selectedHabit by remember { mutableStateOf<TodayHabitUiModel?>(null) }
    var selectedMood by remember { mutableStateOf<MoodEntry?>(null) }
    var selectedProblem by remember { mutableStateOf<Problem?>(null) }

    LifeOSScaffold(
        topBar = { TopAppBar(title = { Text("LifeOS") }) },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
            item {
                Text(
                    text = DateTimeUtils.formatDisplayDate(LocalDate.now()),
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
                items(state.todaysGoals.take(MAX_SECTION_ITEMS), key = { "goal_${it.id}" }) { goal ->
                    GoalSummaryRow(goal, onClick = { selectedGoal = goal })
                }
            }
            item { SectionHeader("Today's habits") }
            if (state.todaysHabits.isEmpty()) {
                item { Text("No habits scheduled today.", modifier = Modifier.padding(horizontal = 16.dp)) }
            } else {
                items(state.todaysHabits.take(MAX_SECTION_ITEMS), key = { "habit_${it.habit.id}" }) { habitItem ->
                    HabitSummaryRow(
                        title = habitItem.habit.title,
                        completed = habitItem.completedToday,
                        onToggle = { viewModel.toggleHabit(habitItem.habit, habitItem.completedToday) },
                        onClick = { selectedHabit = habitItem },
                    )
                }
            }
            item { SectionHeader("Recent moods") }
            if (state.recentMoodEntries.isEmpty()) {
                item { Text("No mood entries logged yet.", modifier = Modifier.padding(horizontal = 16.dp)) }
            } else {
                items(state.recentMoodEntries.take(MAX_SECTION_ITEMS), key = { "mood_${it.id}" }) { mood ->
                    MoodSummaryRow(mood, onClick = { selectedMood = mood })
                }
            }
            item { SectionHeader("Open problems (${state.openProblems.size})") }
            if (state.openProblems.isEmpty()) {
                item { Text("No open problems.", modifier = Modifier.padding(horizontal = 16.dp)) }
            } else {
                items(state.openProblems.take(MAX_SECTION_ITEMS), key = { "problem_${it.id}" }) { problem ->
                    ProblemSummaryRow(problem, onClick = { selectedProblem = problem })
                }
            }
        }
    }

    selectedGoal?.let { goal ->
        GoalActionDialog(
            goal = goal,
            onDismiss = { selectedGoal = null },
            onComplete = { goalViewModel.completeGoal(goal.id); viewModel.refresh(); selectedGoal = null },
            onArchive = { goalViewModel.archiveGoal(goal.id); viewModel.refresh(); selectedGoal = null },
            onDelete = { goalViewModel.deleteGoal(goal.id); viewModel.refresh(); selectedGoal = null },
        )
    }
    selectedHabit?.let { habitItem ->
        HabitActionDialog(
            item = habitItem,
            onDismiss = { selectedHabit = null },
            onToggle = { viewModel.toggleHabit(habitItem.habit, habitItem.completedToday); selectedHabit = null },
            onDelete = { habitViewModel.deleteHabit(habitItem.habit.id); viewModel.refresh(); selectedHabit = null },
        )
    }
    selectedMood?.let { mood ->
        MoodDetailDialog(
            mood = mood,
            onDismiss = { selectedMood = null },
            onDelete = { moodViewModel.deleteMood(mood.id); viewModel.refresh(); selectedMood = null },
        )
    }
    selectedProblem?.let { problem ->
        ProblemActionDialog(
            problem = problem,
            onDismiss = { selectedProblem = null },
            onRecordAttempt = { attempt, worked -> problemViewModel.recordAttempt(problem.id, attempt, worked); viewModel.refresh() },
            onResolve = { problemViewModel.setStatus(problem.id, ProblemStatus.RESOLVED); viewModel.refresh(); selectedProblem = null },
            onDelete = { problemViewModel.deleteProblem(problem.id); viewModel.refresh(); selectedProblem = null },
        )
    }
}

@Composable
private fun GoalSummaryRow(goal: Goal, onClick: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick)) {
        Text(text = goal.title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun HabitSummaryRow(title: String, completed: Boolean, onToggle: () -> Unit, onClick: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick)) {
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
private fun MoodSummaryRow(mood: MoodEntry, onClick: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick)) {
        Text(
            text = "${mood.emotion.name.lowercase()} · ${mood.intensity}/10",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ProblemSummaryRow(problem: Problem, onClick: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick)) {
        Column {
            Text(text = problem.title, style = MaterialTheme.typography.bodyLarge)
            if (problem.attemptsMade.isNotEmpty()) {
                Text(
                    text = "${problem.attemptsMade.size} attempt(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GoalActionDialog(goal: Goal, onDismiss: () -> Unit, onComplete: () -> Unit, onArchive: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(goal.title) },
        text = {
            Column {
                if (goal.description.isNotBlank()) Text(goal.description)
                Text(
                    text = goal.horizon.name.replace('_', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onComplete) { Text("Complete") } },
        dismissButton = {
            Row {
                TextButton(onClick = onArchive) { Text("Archive") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        },
    )
}

@Composable
private fun HabitActionDialog(item: TodayHabitUiModel, onDismiss: () -> Unit, onToggle: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.habit.title) },
        text = {
            Column {
                if (item.habit.description.isNotBlank()) Text(item.habit.description)
                Text(
                    text = if (item.completedToday) "Completed today" else "Not completed today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onToggle) { Text(if (item.completedToday) "Mark incomplete" else "Mark complete") } },
        dismissButton = { TextButton(onClick = onDelete) { Text("Delete") } },
    )
}

@Composable
private fun MoodDetailDialog(mood: MoodEntry, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(mood.emotion.name.lowercase()) },
        text = {
            Column {
                Text(
                    text = DateTimeUtils.formatDisplayDateTime(mood.dateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = "Intensity: ${mood.intensity}/10")
                if (mood.trigger.isNotBlank()) Text(text = "Trigger: ${mood.trigger}")
                if (mood.situation.isNotBlank()) Text(text = "Situation: ${mood.situation}")
                if (mood.lessonsLearned.isNotBlank()) Text(text = "Lessons: ${mood.lessonsLearned}")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = { TextButton(onClick = onDelete) { Text("Delete") } },
    )
}

@Composable
private fun ProblemActionDialog(
    problem: Problem,
    onDismiss: () -> Unit,
    onRecordAttempt: (String, Boolean?) -> Unit,
    onResolve: () -> Unit,
    onDelete: () -> Unit,
) {
    var showRecordAttempt by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(problem.title) },
        text = {
            Column {
                if (problem.description.isNotBlank()) Text(problem.description)
                if (problem.attemptsMade.isNotEmpty()) {
                    Text(
                        text = "Attempts: ${problem.attemptsMade.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onResolve) { Text("Mark resolved") } },
        dismissButton = {
            Row {
                TextButton(onClick = { showRecordAttempt = true }) { Text("Record attempt") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        },
    )

    if (showRecordAttempt) {
        RecordAttemptDialog(
            onDismiss = { showRecordAttempt = false },
            onConfirm = { attempt, worked ->
                onRecordAttempt(attempt, worked)
                showRecordAttempt = false
            },
        )
    }
}

@Composable
private fun RecordAttemptDialog(onDismiss: () -> Unit, onConfirm: (String, Boolean?) -> Unit) {
    var attempt by remember { mutableStateOf("") }
    var worked by remember { mutableStateOf<Boolean?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record attempt") },
        text = {
            Column {
                OutlinedTextField(
                    value = attempt,
                    onValueChange = { attempt = it },
                    label = { Text("What did you try?") },
                )
                Row {
                    TextButton(onClick = { worked = true }) { Text(if (worked == true) "✓ Worked" else "Worked") }
                    TextButton(onClick = { worked = false }) { Text(if (worked == false) "✓ Failed" else "Failed") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (attempt.isNotBlank()) onConfirm(attempt, worked) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
