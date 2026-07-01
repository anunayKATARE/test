package com.lifeos.app.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
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
import com.lifeos.app.feature.checkin.domain.CheckInSession
import com.lifeos.app.feature.checkin.presentation.CheckInViewModel
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
    checkInViewModel: CheckInViewModel = hiltViewModel(),
    onDayClick: (LocalDate) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val checkInState by checkInViewModel.uiState.collectAsState()
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
                CheckInStatusCard(
                    session = checkInState.activeSession,
                    onSchedule = checkInViewModel::openDialog,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
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
            item {
                DashboardSection(
                    title = "Today's goals",
                    items = state.todaysGoals.take(MAX_SECTION_ITEMS),
                    emptyMessage = "No goals for today.",
                ) { goal ->
                    GoalSummaryRow(goal, onClick = { selectedGoal = goal })
                }
            }
            item {
                DashboardSection(
                    title = "Today's habits",
                    items = state.todaysHabits.take(MAX_SECTION_ITEMS),
                    emptyMessage = "No habits scheduled today.",
                ) { habitItem ->
                    HabitSummaryRow(
                        title = habitItem.habit.title,
                        completed = habitItem.completedToday,
                        onToggle = { viewModel.toggleHabit(habitItem.habit, habitItem.completedToday) },
                        onClick = { selectedHabit = habitItem },
                    )
                }
            }
            item {
                DashboardSection(
                    title = "Recent moods",
                    items = state.recentMoodEntries.take(MAX_SECTION_ITEMS),
                    emptyMessage = "No mood entries logged yet.",
                ) { mood ->
                    MoodSummaryRow(mood, onClick = { selectedMood = mood })
                }
            }
            item {
                DashboardSection(
                    title = "Open problems (${state.openProblems.size})",
                    items = state.openProblems.take(MAX_SECTION_ITEMS),
                    emptyMessage = "No open problems.",
                ) { problem ->
                    ProblemSummaryRow(problem, onClick = { selectedProblem = problem })
                }
            }
        }
    }

    selectedGoal?.let { goal ->
        GoalActionDialog(
            goal = goal,
            onDismiss = { selectedGoal = null },
            onComplete = { goalViewModel.completeGoal(goal.id); selectedGoal = null },
            onArchive = { goalViewModel.archiveGoal(goal.id); selectedGoal = null },
            onDelete = { goalViewModel.deleteGoal(goal.id); selectedGoal = null },
        )
    }
    selectedHabit?.let { habitItem ->
        HabitActionDialog(
            item = habitItem,
            onDismiss = { selectedHabit = null },
            onToggle = { viewModel.toggleHabit(habitItem.habit, habitItem.completedToday); selectedHabit = null },
            onDelete = { habitViewModel.deleteHabit(habitItem.habit.id); selectedHabit = null },
        )
    }
    selectedMood?.let { mood ->
        MoodDetailDialog(
            mood = mood,
            onDismiss = { selectedMood = null },
            onDelete = { moodViewModel.deleteMood(mood.id); selectedMood = null },
        )
    }
    selectedProblem?.let { problem ->
        ProblemActionDialog(
            problem = problem,
            onDismiss = { selectedProblem = null },
            onRecordAttempt = { attempt, worked -> problemViewModel.recordAttempt(problem.id, attempt, worked) },
            onResolve = { problemViewModel.setStatus(problem.id, ProblemStatus.RESOLVED); selectedProblem = null },
            onDelete = { problemViewModel.deleteProblem(problem.id); selectedProblem = null },
        )
    }
}

/**
 * Groups a dashboard subsection's header, rows (or empty message) into a single [LifeOSCard]
 * so each subsection reads as one distinct, bounded block rather than bare text.
 */
@Composable
private fun <T> DashboardSection(
    title: String,
    items: List<T>,
    emptyMessage: String,
    row: @Composable (T) -> Unit,
) {
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            items.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                row(item)
            }
        }
    }
}

@Composable
private fun GoalSummaryRow(goal: Goal, onClick: () -> Unit) {
    Text(
        text = goal.title,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
    )
}

@Composable
private fun HabitSummaryRow(title: String, completed: Boolean, onToggle: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
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

@Composable
private fun MoodSummaryRow(mood: MoodEntry, onClick: () -> Unit) {
    Text(
        text = "${mood.emotion.name.lowercase()} · ${mood.intensity}/10",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
    )
}

@Composable
private fun ProblemSummaryRow(problem: Problem, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp)) {
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

@Composable
private fun CheckInStatusCard(
    session: com.lifeos.app.feature.checkin.domain.CheckInSession?,
    onSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOverdue = session?.isOverdue == true
    val borderColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    LifeOSCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isOverdue) Modifier.border(1.5.dp, borderColor, RoundedCornerShape(16.dp)) else Modifier),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        session == null -> "No check-in scheduled"
                        isOverdue -> "Check-in overdue!"
                        else -> "Check-in scheduled"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
                if (session != null) {
                    Text(
                        text = DateTimeUtils.formatDisplayDateTime(session.scheduledAt) +
                            if (session.commitments.isNotEmpty()) " · ${session.commitments.size} commitment(s)" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            if (isOverdue) {
                OutlinedButton(onClick = onSchedule) { Text("Review") }
            } else {
                OutlinedButton(onClick = onSchedule) { Text(if (session == null) "Schedule" else "Edit") }
            }
        }
    }
}
