package com.lifeos.app.feature.checkin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lifeos.app.core.common.DateTimeUtils
import com.lifeos.app.feature.checkin.domain.CheckInCommitment
import com.lifeos.app.feature.checkin.domain.CheckInSession
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.task.domain.Task
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private enum class DialogStep { REVIEW, SCHEDULE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInDialog(
    activeSession: CheckInSession?,
    todaysTasks: List<Task>,
    todaysHabits: List<Habit>,
    todaysGoals: List<Goal>,
    onSchedule: (scheduledAt: Instant, commitments: List<CheckInCommitment>) -> Unit,
    onCompleteAndScheduleNext: (completedIds: Set<String>, nextAt: Instant?, nextCommitments: List<CheckInCommitment>) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
    buildFromTask: (Task) -> CheckInCommitment,
    buildFromHabit: (Habit) -> CheckInCommitment,
    buildFromGoal: (Goal) -> CheckInCommitment,
    buildFreeText: (String) -> CheckInCommitment,
) {
    val startStep = if (activeSession?.isOverdue == true) DialogStep.REVIEW else DialogStep.SCHEDULE
    var step by remember { mutableStateOf(startStep) }

    // Review step state
    val checkedIds = remember(activeSession) {
        mutableStateMapOf<String, Boolean>().also { map ->
            activeSession?.commitments?.forEach { map[it.id] = it.isCompleted }
        }
    }
    var completedIdsForNextStep by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Schedule step state
    var showTimePicker by remember { mutableStateOf(false) }
    var scheduledAt by remember { mutableStateOf<Instant?>(null) }
    val selectedTaskIds = remember { mutableStateMapOf<String, Boolean>() }
    val selectedHabitIds = remember { mutableStateMapOf<String, Boolean>() }
    val selectedGoalIds = remember { mutableStateMapOf<String, Boolean>() }
    var freeText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                when (step) {
                    DialogStep.REVIEW -> ReviewStep(
                        session = activeSession!!,
                        checkedIds = checkedIds,
                        onToggle = { id, v -> checkedIds[id] = v },
                        onSkip = onSkip,
                        onContinue = {
                            completedIdsForNextStep = checkedIds.filterValues { it }.keys.toSet()
                            step = DialogStep.SCHEDULE
                        },
                    )

                    DialogStep.SCHEDULE -> ScheduleStep(
                        todaysTasks = todaysTasks,
                        todaysHabits = todaysHabits,
                        todaysGoals = todaysGoals,
                        selectedTaskIds = selectedTaskIds,
                        selectedHabitIds = selectedHabitIds,
                        selectedGoalIds = selectedGoalIds,
                        freeText = freeText,
                        onFreeTextChange = { freeText = it },
                        scheduledAt = scheduledAt,
                        onPresetSelected = { scheduledAt = it },
                        onShowTimePicker = { showTimePicker = true },
                        onConfirm = {
                            val at = scheduledAt ?: return@ScheduleStep
                            val commitments = buildCommitmentList(
                                selectedTaskIds, selectedHabitIds, selectedGoalIds,
                                freeText, todaysTasks, todaysHabits, todaysGoals,
                                buildFromTask, buildFromHabit, buildFromGoal, buildFreeText,
                            )
                            if (startStep == DialogStep.REVIEW) {
                                onCompleteAndScheduleNext(completedIdsForNextStep, at, commitments)
                            } else {
                                onSchedule(at, commitments)
                            }
                        },
                        onSkipSchedule = if (startStep == DialogStep.REVIEW) {
                            { onCompleteAndScheduleNext(completedIdsForNextStep, null, emptyList()) }
                        } else null,
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = LocalTime.now().hour,
            initialMinute = 0,
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = state)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            val today = LocalDate.now()
                            scheduledAt = today.atTime(state.hour, state.minute)
                                .atZone(ZoneId.systemDefault()).toInstant()
                                .let { if (it <= Instant.now()) it.plusSeconds(86400) else it }
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewStep(
    session: CheckInSession,
    checkedIds: Map<String, Boolean>,
    onToggle: (String, Boolean) -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
) {
    Text("Check-in time!", style = MaterialTheme.typography.titleLarge)
    Text(
        "Scheduled for ${DateTimeUtils.formatDisplayDateTime(session.scheduledAt)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(16.dp))
    if (session.commitments.isEmpty()) {
        Text("No commitments were set for this check-in.", style = MaterialTheme.typography.bodyMedium)
    } else {
        Text("How did you do?", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        session.commitments.forEachIndexed { index, commitment ->
            if (index > 0) HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = checkedIds[commitment.id] == true,
                    onCheckedChange = { onToggle(commitment.id, it) },
                )
                Text(commitment.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onSkip) { Text("Skip") }
        Button(onClick = onContinue) { Text("Done — schedule next") }
    }
}

@Composable
private fun ScheduleStep(
    todaysTasks: List<Task>,
    todaysHabits: List<Habit>,
    todaysGoals: List<Goal>,
    selectedTaskIds: MutableMap<String, Boolean>,
    selectedHabitIds: MutableMap<String, Boolean>,
    selectedGoalIds: MutableMap<String, Boolean>,
    freeText: String,
    onFreeTextChange: (String) -> Unit,
    scheduledAt: Instant?,
    onPresetSelected: (Instant) -> Unit,
    onShowTimePicker: () -> Unit,
    onConfirm: () -> Unit,
    onSkipSchedule: (() -> Unit)?,
) {
    Text("Schedule next check-in", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    Text("When?", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))
    TimePresets(selectedAt = scheduledAt, onSelect = onPresetSelected, onCustom = onShowTimePicker)
    scheduledAt?.let {
        Spacer(Modifier.height(4.dp))
        Text(
            "Set for ${DateTimeUtils.formatDisplayDateTime(it)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    Spacer(Modifier.height(16.dp))
    Text("What will you commit to?", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))
    if (todaysTasks.isNotEmpty()) {
        Text("Tasks", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        todaysTasks.forEach { task ->
            CheckboxRow(
                label = task.title,
                checked = selectedTaskIds[task.id] == true,
                onToggle = { selectedTaskIds[task.id] = it },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
    if (todaysHabits.isNotEmpty()) {
        Text("Habits", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        todaysHabits.forEach { habit ->
            CheckboxRow(
                label = habit.title,
                checked = selectedHabitIds[habit.id] == true,
                onToggle = { selectedHabitIds[habit.id] = it },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
    if (todaysGoals.isNotEmpty()) {
        Text("Goals", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        todaysGoals.forEach { goal ->
            CheckboxRow(
                label = goal.title,
                checked = selectedGoalIds[goal.id] == true,
                onToggle = { selectedGoalIds[goal.id] = it },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
    OutlinedTextField(
        value = freeText,
        onValueChange = onFreeTextChange,
        label = { Text("Add custom commitment…") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    Spacer(Modifier.height(20.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        if (onSkipSchedule != null) {
            TextButton(onClick = onSkipSchedule) { Text("No new check-in") }
        } else {
            Spacer(Modifier.width(1.dp))
        }
        Button(onClick = onConfirm, enabled = scheduledAt != null) { Text("Set check-in") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePresets(selectedAt: Instant?, onSelect: (Instant) -> Unit, onCustom: () -> Unit) {
    val now = Instant.now()
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()

    data class Preset(val label: String, val instant: Instant)

    val presets = listOf(
        Preset("In 1 hr", now.plusSeconds(3600)),
        Preset("In 3 hrs", now.plusSeconds(10800)),
        Preset("Tonight 9 PM", today.atTime(21, 0).atZone(zone).toInstant().let {
            if (it <= now) it.plusSeconds(86400) else it
        }),
        Preset("Tomorrow 8 AM", today.plusDays(1).atTime(8, 0).atZone(zone).toInstant()),
    )

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        presets.forEach { preset ->
            FilterChip(
                selected = selectedAt == preset.instant,
                onClick = { onSelect(preset.instant) },
                label = { Text(preset.label, style = MaterialTheme.typography.labelSmall) },
            )
        }
        FilterChip(
            selected = false,
            onClick = onCustom,
            label = { Text("Custom…", style = MaterialTheme.typography.labelSmall) },
        )
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = onToggle)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun buildCommitmentList(
    selectedTaskIds: Map<String, Boolean>,
    selectedHabitIds: Map<String, Boolean>,
    selectedGoalIds: Map<String, Boolean>,
    freeText: String,
    todaysTasks: List<Task>,
    todaysHabits: List<Habit>,
    todaysGoals: List<Goal>,
    buildFromTask: (Task) -> CheckInCommitment,
    buildFromHabit: (Habit) -> CheckInCommitment,
    buildFromGoal: (Goal) -> CheckInCommitment,
    buildFreeText: (String) -> CheckInCommitment,
): List<CheckInCommitment> = buildList {
    todaysTasks.filter { selectedTaskIds[it.id] == true }.forEach { add(buildFromTask(it)) }
    todaysHabits.filter { selectedHabitIds[it.id] == true }.forEach { add(buildFromHabit(it)) }
    todaysGoals.filter { selectedGoalIds[it.id] == true }.forEach { add(buildFromGoal(it)) }
    if (freeText.isNotBlank()) add(buildFreeText(freeText.trim()))
}
