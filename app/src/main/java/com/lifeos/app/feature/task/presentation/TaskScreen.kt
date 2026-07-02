package com.lifeos.app.feature.task.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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

private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

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
            val activity = context as? android.app.Activity
            permissionDeniedPermanently = activity?.shouldShowRequestPermissionRationale(
                Manifest.permission.READ_CALENDAR
            ) == false
        }
    }

    LaunchedEffect(Unit) {
        if (!state.calendarPermissionGranted) {
            calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }

    LifeOSScaffold(
        topBar = {
            LifeOSTopBar(title = "Tasks") {
                IconButton(onClick = viewModel::openQuickPlan) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Plan next tasks")
                }
            }
        },
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
                    scheduledTasks = state.tasks.filter { it.scheduledAt != null },
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

            if (state.scheduledHabits.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Habits", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                }
                items(state.scheduledHabits, key = { "habit_${it.id}" }) { habit ->
                    HabitDayCard(
                        item = habit,
                        onToggle = { viewModel.toggleHabitCompleted(habit) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
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
                scheduledHabits = state.scheduledHabits,
                tasks = state.tasks,
                calendarEvents = state.calendarEvents,
                date = state.selectedDate,
                onTitleChange = viewModel::updateTitle,
                onDescriptionChange = viewModel::updateDescription,
                onTriggerInputChange = viewModel::updateTriggerInput,
                onAddTrigger = viewModel::addTrigger,
                onRemoveTrigger = viewModel::removeTrigger,
                onPickSlot = viewModel::pickScheduledSlot,
                onPickTime = viewModel::pickScheduledTime,
                onIsChoreChange = viewModel::updateIsChore,
                onHabitSelect = viewModel::selectHabitForTask,
                onStartTimeChange = viewModel::updateStartTimeText,
                onStartTimeDone = viewModel::applyStartTimeText,
                onEndTimeChange = viewModel::updateEndTimeText,
                onEndTimeDone = viewModel::applyEndTimeText,
                onSave = viewModel::saveTask,
                onDismiss = viewModel::dismissSheet,
            )
        }
    }

    if (state.showQuickPlan) {
        ModalBottomSheet(onDismissRequest = viewModel::dismissQuickPlan) {
            QuickPlanSheet(
                tasks = state.unscheduledUpcoming,
                times = state.quickPlanTimes,
                onTimeChange = viewModel::updateQuickPlanTime,
                onConfirm = viewModel::confirmQuickPlan,
                onDismiss = viewModel::dismissQuickPlan,
            )
        }
    }
}

@Composable
private fun CalendarSection(
    hasPermission: Boolean,
    permanentlyDenied: Boolean,
    events: List<CalendarEvent>,
    scheduledTasks: List<Task>,
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
                hasPermission -> DayBusyBar(events = events, scheduledTasks = scheduledTasks, date = date)
                permanentlyDenied -> {
                    DayBusyBar(events = emptyList(), scheduledTasks = scheduledTasks, date = date)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Calendar access was denied. Enable it in Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onOpenSettings) { Text("Settings") }
                    }
                }
                else -> {
                    DayBusyBar(events = emptyList(), scheduledTasks = scheduledTasks, date = date)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
}

@Composable
private fun DayBusyBar(events: List<CalendarEvent>, scheduledTasks: List<Task>, date: LocalDate) {
    val zone = ZoneId.systemDefault()
    val windowStartHour = 7
    val windowEndHour = 22
    val windowStart = date.atTime(windowStartHour, 0).atZone(zone).toInstant()
    val windowEnd = date.atTime(windowEndHour, 0).atZone(zone).toInstant()
    val totalMinutes = Duration.between(windowStart, windowEnd).toMinutes().toFloat()

    val eventColor = MaterialTheme.colorScheme.errorContainer
    val taskColor = MaterialTheme.colorScheme.primaryContainer
    val nonAllDay = events.filter { !it.isAllDay }

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

    // Calendar events bar
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
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
                        .background(eventColor),
                )
            }
        }
    }

    Spacer(Modifier.height(2.dp))

    // Scheduled tasks bar
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val totalWidth = maxWidth
        scheduledTasks.forEach { task ->
            val taskStart = task.scheduledAt ?: return@forEach
            val taskEnd = task.scheduledEndAt ?: taskStart.plusSeconds(30 * 60)
            val s = maxOf(taskStart, windowStart)
            val e = minOf(taskEnd, windowEnd)
            if (s < e) {
                val startFrac = Duration.between(windowStart, s).toMinutes().toFloat() / totalMinutes
                val widthFrac = Duration.between(s, e).toMinutes().toFloat() / totalMinutes
                Box(
                    modifier = Modifier
                        .offset(x = totalWidth * startFrac)
                        .width(totalWidth * widthFrac)
                        .fillMaxHeight()
                        .background(taskColor),
                )
            }
        }
    }

    // Legend
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (nonAllDay.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(eventColor, CircleShape))
                Spacer(Modifier.width(4.dp))
                Text("Calendar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (scheduledTasks.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(taskColor, CircleShape))
                Spacer(Modifier.width(4.dp))
                Text("Tasks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (nonAllDay.isEmpty() && scheduledTasks.isEmpty()) {
            Text(
                "No events or scheduled tasks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (nonAllDay.isNotEmpty()) {
        Spacer(Modifier.height(6.dp))
        nonAllDay.forEach { event ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(6.dp).background(eventColor, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${timeFormatter.format(event.startAt)}–${timeFormatter.format(event.endAt)}  ${event.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
                        val endLabel = task.scheduledEndAt?.let { " – ${slotFormatter.format(it)}" } ?: ""
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "${slotFormatter.format(at)}$endLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    if (task.isChore) {
                        Text(
                            "Chore",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
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

@Composable
private fun HabitDayCard(item: HabitDayItem, onToggle: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.completedToday, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyLarge.let {
                        if (item.completedToday) it.copy(textDecoration = TextDecoration.LineThrough) else it
                    },
                    color = if (item.completedToday) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
                if (item.triggers.isNotEmpty()) {
                    Text(
                        "Triggers: ${item.triggers.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// Vertical day timeline — tapping sets scheduledAt to the tapped time
@Composable
private fun VerticalDayTimeline(
    date: LocalDate,
    events: List<CalendarEvent>,
    tasks: List<Task>,
    selectedTime: Instant?,
    onTimeSelected: (Instant) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone = ZoneId.systemDefault()
    val windowStartHour = 6
    val windowEndHour = 22
    val totalWindowMinutes = ((windowEndHour - windowStartHour) * 60).toFloat()
    val windowStartInstant = date.atTime(windowStartHour, 0).atZone(zone).toInstant()
    val windowEndInstant = date.atTime(windowEndHour, 0).atZone(zone).toInstant()
    val labelWidth = 34.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(date) {
                detectTapGestures { offset ->
                    val frac = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                    val minutes = (frac * totalWindowMinutes).toLong()
                    val rounded = (minutes / 30) * 30
                    onTimeSelected(windowStartInstant.plusSeconds(rounded * 60))
                }
            }
    ) {
        val h = maxHeight
        val w = maxWidth
        val contentWidth = w - labelWidth

        // Hour guide lines every 2 hours
        for (hour in windowStartHour..windowEndHour step 2) {
            val frac = (hour - windowStartHour).toFloat() / (windowEndHour - windowStartHour)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .absoluteOffset(y = h * frac)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                )
                Text(
                    text = when {
                        hour < 12 -> "${hour}am"
                        hour == 12 -> "12pm"
                        else -> "${hour - 12}pm"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                )
            }
        }

        // Calendar events (error container color, left half of content area)
        events.filter { !it.isAllDay }.forEach { event ->
            val s = maxOf(event.startAt, windowStartInstant)
            val e = minOf(event.endAt, windowEndInstant)
            if (s < e) {
                val topFrac = Duration.between(windowStartInstant, s).toMinutes().toFloat() / totalWindowMinutes
                val heightFrac = Duration.between(s, e).toMinutes().toFloat() / totalWindowMinutes
                Box(
                    modifier = Modifier
                        .absoluteOffset(x = labelWidth, y = h * topFrac)
                        .width(contentWidth * 0.47f)
                        .height((h * heightFrac).coerceAtLeast(14.dp))
                        .padding(end = 2.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        // Scheduled tasks (primary container color, right half of content area)
        tasks.filter { it.scheduledAt != null }.forEach { task ->
            val s = task.scheduledAt!!
            if (s >= windowStartInstant && s < windowEndInstant) {
                val topFrac = Duration.between(windowStartInstant, s).toMinutes().toFloat() / totalWindowMinutes
                Box(
                    modifier = Modifier
                        .absoluteOffset(x = labelWidth + contentWidth * 0.49f, y = h * topFrac)
                        .width(contentWidth * 0.47f)
                        .height(14.dp)
                        .padding(start = 2.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // Selected time indicator (bright primary line)
        selectedTime?.let { st ->
            if (st >= windowStartInstant && st < windowEndInstant) {
                val topFrac = Duration.between(windowStartInstant, st).toMinutes().toFloat() / totalWindowMinutes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .absoluteOffset(y = h * topFrac)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    slotFormatter.format(st),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.absoluteOffset(x = labelWidth, y = h * topFrac - 14.dp),
                )
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
    scheduledHabits: List<HabitDayItem>,
    tasks: List<Task>,
    calendarEvents: List<CalendarEvent>,
    date: LocalDate,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTriggerInputChange: (String) -> Unit,
    onAddTrigger: () -> Unit,
    onRemoveTrigger: (String) -> Unit,
    onPickSlot: (FreeSlot?) -> Unit,
    onPickTime: (Instant) -> Unit,
    onIsChoreChange: (Boolean) -> Unit,
    onHabitSelect: (HabitDayItem) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onStartTimeDone: () -> Unit,
    onEndTimeChange: (String) -> Unit,
    onEndTimeDone: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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

        // Vertical day timeline — tap to pick start time
        Spacer(Modifier.height(12.dp))
        Text(
            "Day Timeline — tap to set start time",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        VerticalDayTimeline(
            date = date,
            events = calendarEvents,
            tasks = tasks,
            selectedTime = form.scheduledAt,
            onTimeSelected = onPickTime,
        )

        // Start / End time text inputs
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = form.startTimeText,
                onValueChange = onStartTimeChange,
                label = { Text("Start (HH:mm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onStartTimeDone()
                }),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = form.endTimeText,
                onValueChange = onEndTimeChange,
                label = { Text("End (HH:mm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onEndTimeDone()
                }),
                modifier = Modifier.weight(1f),
            )
        }

        if (form.scheduledAt != null) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                val endLabel = form.scheduledEndAt?.let { " – ${slotFormatter.format(it)}" } ?: ""
                Text(
                    "Scheduled ${slotFormatter.format(form.scheduledAt)}$endLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { onPickSlot(null) }) { Text("Clear") }
            }
        }

        // Free slot chips (alternative to tapping timeline)
        if (freeSlots.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Free slots",
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
                freeSlots.take(5).forEach { slot ->
                    val label = "${slotFormatter.format(slot.start)} (${slot.durationMinutes}m)"
                    FilterChip(
                        selected = form.scheduledAt == slot.start,
                        onClick = { onPickSlot(slot) },
                        label = { Text(label) },
                    )
                }
            }
        }

        // Today's habits — tapping a chip pre-fills the title for quick scheduling
        if (scheduledHabits.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Today's Habits — tap to schedule",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                scheduledHabits.forEach { habit ->
                    val isSelected = form.title == habit.title
                    val isRelated = !isSelected && form.triggers.isNotEmpty() && habit.triggers.any { ht ->
                        form.triggers.any { ft -> ht.contains(ft, ignoreCase = true) || ft.contains(ht, ignoreCase = true) }
                    }
                    FilterChip(
                        selected = isSelected || isRelated,
                        onClick = { onHabitSelect(habit) },
                        label = { Text(habit.title, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (habit.completedToday) {
                            { Icon(Icons.Filled.Check, null, modifier = Modifier.size(14.dp)) }
                        } else null,
                    )
                }
            }

            // Chore toggle: shown for new tasks when no habits match the current triggers
            val hasRelatedHabit = scheduledHabits.any { habit ->
                form.triggers.isNotEmpty() && habit.triggers.any { ht ->
                    form.triggers.any { ft -> ht.contains(ft, ignoreCase = true) || ft.contains(ht, ignoreCase = true) }
                }
            }
            if (!isEditing && !hasRelatedHabit) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mark as Chore", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Routine sustaining task (cleaning, eating, maintenance)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = form.isChore, onCheckedChange = onIsChoreChange)
                }
            }
        } else if (!isEditing) {
            // No habits today — always offer chore option
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mark as Chore", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Routine sustaining task (cleaning, eating, maintenance)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = form.isChore, onCheckedChange = onIsChoreChange)
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

@Composable
private fun QuickPlanSheet(
    tasks: List<Task>,
    times: Map<String, String>,
    onTimeChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text("Plan Next Tasks", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "Assign start times to schedule upcoming unscheduled tasks",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        if (tasks.isEmpty()) {
            Text(
                "No unscheduled upcoming tasks.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            tasks.forEach { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            task.date.format(shortDateFormatter),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = times[task.id] ?: "",
                        onValueChange = { onTimeChange(task.id, it) },
                        label = { Text("HH:mm") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.width(100.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = onConfirm,
                enabled = tasks.any { times[it.id]?.trim()?.isNotBlank() == true },
                modifier = Modifier.weight(1f),
            ) { Text("Schedule") }
        }
        Spacer(Modifier.height(32.dp))
    }
}
