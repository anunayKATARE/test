package com.lifeos.app.feature.task.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.calendar.domain.CalendarEvent
import com.lifeos.app.feature.calendar.domain.FreeSlot
import com.lifeos.app.feature.task.domain.Task
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

private val slotFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var permissionDeniedPermanently by remember { mutableStateOf(false) }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onCalendarPermissionResult(granted)
        if (!granted) {
            // If the system didn't show a rationale before asking, the user chose "Never ask again"
            val activity = context as? android.app.Activity
            permissionDeniedPermanently = activity?.shouldShowRequestPermissionRationale(
                Manifest.permission.READ_CALENDAR
            ) == false
        }
    }

    // Fire once per screen entry; if not yet granted, show the system dialog immediately
    LaunchedEffect(Unit) {
        if (!state.calendarPermissionGranted) {
            calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }

    LifeOSScaffold(
        topBar = { LifeOSTopBar(title = "Tasks") },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddSheet) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.selectDate(LocalDate.now().minusDays(1)) }) { Text("Yesterday") }
                    TextButton(onClick = { viewModel.selectDate(LocalDate.now()) }) { Text("Today") }
                    TextButton(onClick = { viewModel.selectDate(LocalDate.now().plusDays(1)) }) { Text("Tomorrow") }
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                CalendarSection(
                    hasPermission = state.calendarPermissionGranted,
                    permanentlyDenied = permissionDeniedPermanently,
                    events = state.calendarEvents,
                    date = state.selectedDate,
                    onRequestPermission = {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    },
                    onOpenSettings = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    },
                )
                Spacer(Modifier.height(16.dp))
            }

            if (state.tasks.isEmpty()) {
                item {
                    Text(
                        "No tasks yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            items(state.tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggle = { viewModel.toggleCompleted(task) },
                    onEdit = { viewModel.openEditSheet(task) },
                    onDelete = { viewModel.deleteTask(task) },
                )
                Spacer(Modifier.height(8.dp))
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (state.showSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::dismissSheet) {
            TaskFormSheet(
                form = state.form,
                isEditing = state.editingTask != null,
                freeSlots = state.freeSlots,
                onTitleChange = viewModel::updateTitle,
                onDescriptionChange = viewModel::updateDescription,
                onTriggerInputChange = viewModel::updateTriggerInput,
                onAddTrigger = viewModel::addTrigger,
                onRemoveTrigger = viewModel::removeTrigger,
                onPickSlot = viewModel::pickScheduledSlot,
                onSave = viewModel::saveTask,
                onDismiss = viewModel::dismissSheet,
            )
        }
    }
}

@Composable
private fun CalendarSection(
    hasPermission: Boolean,
    permanentlyDenied: Boolean,
    events: List<CalendarEvent>,
    date: LocalDate,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LifeOSCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Calendar", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(8.dp))

            when {
                hasPermission -> DayBusyBar(events = events, date = date)
                permanentlyDenied -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Calendar access was denied. Enable it in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onOpenSettings) { Text("Settings") }
                }
                else -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Allow calendar access to see busy times",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onRequestPermission) { Text("Allow") }
                }
            }
        }
    }
}

@Composable
private fun DayBusyBar(events: List<CalendarEvent>, date: LocalDate) {
    val zone = ZoneId.systemDefault()
    val windowStartHour = 7
    val windowEndHour = 22
    val windowStart = date.atTime(windowStartHour, 0).atZone(zone).toInstant()
    val windowEnd = date.atTime(windowEndHour, 0).atZone(zone).toInstant()
    val totalMinutes = Duration.between(windowStart, windowEnd).toMinutes().toFloat()

    val busyColor = MaterialTheme.colorScheme.errorContainer
    val nonAllDay = events.filter { !it.isAllDay }

    // Proportional hour labels: each placed at the exact fractional position it represents
    val windowHours = (windowEndHour - windowStartHour).toFloat()
    val hourLabels = listOf(7 to "7am", 9 to "9am", 11 to "11am", 13 to "1pm",
        15 to "3pm", 17 to "5pm", 19 to "7pm", 22 to "10pm")
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
        hourLabels.forEach { (hour, label) ->
            val frac = (hour - windowStartHour) / windowHours
            Text(
                label,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (maxWidth * frac).coerceAtMost(maxWidth - 20.dp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val totalWidth = maxWidth
        nonAllDay.forEach { event ->
            val s = maxOf(event.startAt, windowStart)
            val e = minOf(event.endAt, windowEnd)
            if (s < e) {
                val startFrac = Duration.between(windowStart, s).toMinutes().toFloat() / totalMinutes
                val widthFrac = Duration.between(s, e).toMinutes().toFloat() / totalMinutes
                Box(
                    modifier = Modifier
                        .offset(x = totalWidth * startFrac)
                        .width(totalWidth * widthFrac)
                        .fillMaxHeight()
                        .background(busyColor),
                )
            }
        }
    }

    if (nonAllDay.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        nonAllDay.forEach { event ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(6.dp).background(busyColor, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${timeFormatter.format(event.startAt)}–${timeFormatter.format(event.endAt)}  ${event.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        Spacer(Modifier.height(4.dp))
        Text(
            "No events — day is clear",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TaskCard(task: Task, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = task.completed, onCheckedChange = { onToggle() })
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodyLarge.let {
                            if (task.completed) it.copy(textDecoration = TextDecoration.LineThrough) else it
                        },
                        color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    task.scheduledAt?.let { at ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                slotFormatter.format(at),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    if (task.triggers.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Triggers: ${task.triggers.joinToString(" · ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskFormSheet(
    form: TaskFormState,
    isEditing: Boolean,
    freeSlots: List<FreeSlot>,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTriggerInputChange: (String) -> Unit,
    onAddTrigger: () -> Unit,
    onRemoveTrigger: (String) -> Unit,
    onPickSlot: (FreeSlot?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
        Text(
            if (isEditing) "Edit Task" else "New Task",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form.title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = form.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )

        if (freeSlots.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Available time slots",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val noSlot: FreeSlot? = null
                FilterChip(
                    selected = form.scheduledAt == null,
                    onClick = { onPickSlot(noSlot) },
                    label = { Text("No time") },
                )
                freeSlots.take(6).forEach { slot ->
                    val label = "${slotFormatter.format(slot.start)} (${slot.durationMinutes}m)"
                    FilterChip(
                        selected = form.scheduledAt == slot.start,
                        onClick = { onPickSlot(slot) },
                        label = { Text(label) },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Triggers — one-word things that could make you fail this task",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = form.triggerInput,
                onValueChange = onTriggerInputChange,
                label = { Text("Add trigger") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onAddTrigger, enabled = form.triggerInput.isNotBlank()) {
                Text("Add")
            }
        }
        if (form.triggers.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                form.triggers.forEach { trigger ->
                    InputChip(
                        selected = false,
                        onClick = {},
                        label = { Text(trigger) },
                        trailingIcon = {
                            IconButton(onClick = { onRemoveTrigger(trigger) }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = onSave,
                enabled = form.title.isNotBlank(),
                modifier = Modifier.weight(1f),
            ) { Text(if (isEditing) "Save" else "Create") }
        }
        Spacer(Modifier.height(32.dp))
    }
}
