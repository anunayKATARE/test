package com.lifeos.app.feature.calendar.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.core.common.DateTimeUtils
import com.lifeos.app.core.ui.components.EmptyState
import com.lifeos.app.core.ui.components.LifeOSCard
import com.lifeos.app.core.ui.components.LifeOSScaffold
import com.lifeos.app.core.ui.components.LifeOSTopBar
import com.lifeos.app.feature.calendar.domain.CalendarEvent
import com.lifeos.app.feature.inspiration.presentation.InspirationCarousel
import com.lifeos.app.feature.task.domain.Task
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(viewModel: DayDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LifeOSScaffold(
        topBar = { LifeOSTopBar(title = DateTimeUtils.formatDisplayDate(viewModel.date)) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            InspirationCarousel(modifier = Modifier.fillMaxWidth().padding(16.dp))

            if (state.calendarPermissionGranted || state.scheduledTasks.isNotEmpty()) {
                DayTimelineSection(
                    date = viewModel.date,
                    calendarEvents = state.calendarEvents,
                    scheduledTasks = state.scheduledTasks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (state.items.isNotEmpty()) {
                DayScoreCard(
                    completedCount = state.items.count { it.completed },
                    totalCount = state.items.size,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            if (state.items.isEmpty()) {
                EmptyState(message = "No tasks or habits for this day.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.items, key = { "${it.isHabit}_${it.id}" }) { item ->
                        DayItemRow(item, onToggle = { viewModel.toggle(item) })
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title ->
                viewModel.addTask(title)
                showAddTaskDialog = false
            },
        )
    }
}

@Composable
private fun DayTimelineSection(
    date: LocalDate,
    calendarEvents: List<CalendarEvent>,
    scheduledTasks: List<Task>,
    modifier: Modifier = Modifier,
) {
    val zone = ZoneId.systemDefault()
    val windowStartHour = 6
    val windowEndHour = 22
    val windowStartInstant = date.atTime(windowStartHour, 0).atZone(zone).toInstant()
    val windowEndInstant = date.atTime(windowEndHour, 0).atZone(zone).toInstant()
    val totalMinutes = Duration.between(windowStartInstant, windowEndInstant).toMinutes().toFloat()
    val windowHours = (windowEndHour - windowStartHour).toFloat()
    val nonAllDayEvents = calendarEvents.filter { !it.isAllDay }

    LifeOSCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Day Timeline",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))

            // Hour labels using BoxWithConstraints for correct fractional positioning
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                listOf(6 to "6am", 9 to "9am", 12 to "12pm", 15 to "3pm", 18 to "6pm", 21 to "9pm").forEach { (hour, label) ->
                    val frac = (hour - windowStartHour) / windowHours
                    Text(
                        label,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .absoluteOffset(x = (maxWidth * frac).coerceAtMost(maxWidth - 28.dp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Calendar events layer
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                val totalWidth = maxWidth
                nonAllDayEvents.forEach { event ->
                    val s = maxOf(event.startAt, windowStartInstant)
                    val e = minOf(event.endAt, windowEndInstant)
                    if (s < e) {
                        val startFrac = Duration.between(windowStartInstant, s).toMinutes().toFloat() / totalMinutes
                        val widthFrac = Duration.between(s, e).toMinutes().toFloat() / totalMinutes
                        Box(
                            modifier = Modifier
                                .absoluteOffset(x = totalWidth * startFrac)
                                .width(totalWidth * widthFrac)
                                .height(12.dp)
                                .background(MaterialTheme.colorScheme.errorContainer),
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            // Scheduled tasks layer
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                val totalWidth = maxWidth
                scheduledTasks.forEach { task ->
                    val taskStart = task.scheduledAt ?: return@forEach
                    val taskEnd = task.scheduledEndAt ?: taskStart.plusSeconds(30 * 60)
                    val s = maxOf(taskStart, windowStartInstant)
                    val e = minOf(taskEnd, windowEndInstant)
                    if (s < e) {
                        val startFrac = Duration.between(windowStartInstant, s).toMinutes().toFloat() / totalMinutes
                        val widthFrac = Duration.between(s, e).toMinutes().toFloat() / totalMinutes
                        Box(
                            modifier = Modifier
                                .absoluteOffset(x = totalWidth * startFrac)
                                .width(totalWidth * widthFrac)
                                .height(12.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        )
                    }
                }
            }

            // Legend
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (nonAllDayEvents.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.errorContainer, CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text("Calendar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (scheduledTasks.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text("Tasks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (nonAllDayEvents.isEmpty() && scheduledTasks.isEmpty()) {
                    Text(
                        "No scheduled items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Task time labels
            if (scheduledTasks.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                scheduledTasks.forEach { task ->
                    val at = task.scheduledAt ?: return@forEach
                    val endLabel = task.scheduledEndAt?.let { " – ${timeFormatter.format(it)}" } ?: ""
                    Row(
                        modifier = Modifier.padding(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${timeFormatter.format(at)}$endLabel  ${task.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New task") },
        text = { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }) },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onConfirm(title) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DayScoreCard(completedCount: Int, totalCount: Int, modifier: Modifier = Modifier) {
    val score = if (totalCount == 0) 0 else ((completedCount.toFloat() / totalCount) * 100f).roundToInt()
    val label = when {
        score >= 80 -> "Great day!"
        score >= 50 -> "Good progress"
        score > 0 -> "Getting started"
        else -> "No progress yet"
    }

    LifeOSCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = "Day score", style = MaterialTheme.typography.titleSmall)
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "$score/100",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DayItemRow(item: DayItemUiModel, onToggle: () -> Unit) {
    LifeOSCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = item.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onToggle) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (item.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (item.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = "Toggle item",
                        tint = if (item.completed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
