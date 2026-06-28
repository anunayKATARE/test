package com.lifeos.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * GitHub-style consistency heatmap: one column per week, one cell per day, shaded by intensity
 * (0 = no activity .. 4 = highest activity in the supplied range). [intensityByDate] need not
 * cover every date in [weeks] worth of days; missing dates render as the lowest intensity.
 */
@Composable
fun ConsistencyHeatmap(
    intensityByDate: Map<LocalDate, Int>,
    weeks: Int = 12,
    modifier: Modifier = Modifier,
    cellSize: androidx.compose.ui.unit.Dp = 12.dp,
) {
    val today = LocalDate.now()
    val totalDays = weeks * 7
    val startDate = today.minusDays((totalDays - 1).toLong())
    val maxIntensity = (intensityByDate.values.maxOrNull() ?: 1).coerceAtLeast(1)

    val columns = (0 until weeks).map { weekIndex ->
        (0 until 7).map { dayIndex ->
            startDate.plusDays((weekIndex * 7 + dayIndex).toLong())
        }
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        columns.forEach { week ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                week.forEach { date ->
                    val intensity = if (date.isAfter(today)) -1 else (intensityByDate[date] ?: 0)
                    HeatmapCell(intensity = intensity, maxIntensity = maxIntensity, size = cellSize)
                }
            }
        }
    }
}

@Composable
private fun HeatmapCell(intensity: Int, maxIntensity: Int, size: androidx.compose.ui.unit.Dp) {
    val baseColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val color = when {
        intensity < 0 -> androidx.compose.ui.graphics.Color.Transparent
        intensity == 0 -> emptyColor
        else -> baseColor.copy(alpha = (0.25f + 0.75f * (intensity.toFloat() / maxIntensity)).coerceIn(0.25f, 1f))
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(2.dp))
            .background(color),
    )
}
