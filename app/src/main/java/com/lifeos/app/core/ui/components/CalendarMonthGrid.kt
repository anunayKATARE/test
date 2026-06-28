package com.lifeos.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.common.DateTimeUtils
import java.time.LocalDate
import java.time.YearMonth

/**
 * Month-grid calendar where each day cell is shaded green by [intensityByDate] (0f..1f fraction of
 * that day's items completed), GitHub-heatmap style. Days with no entry in the map render neutral.
 */
@Composable
fun CalendarMonthGrid(
    month: YearMonth,
    intensityByDate: Map<LocalDate, Float>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = DateTimeUtils.monthGrid(month)
    val weekdayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    CalendarDayCell(
                        date = date,
                        inCurrentMonth = YearMonth.from(date) == month,
                        fraction = intensityByDate[date],
                        onClick = { onDayClick(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    inCurrentMonth: Boolean,
    fraction: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val backgroundColor = when {
        fraction == null -> Color.Transparent
        fraction <= 0f -> emptyColor
        else -> baseColor.copy(alpha = (0.25f + 0.75f * fraction).coerceIn(0.25f, 1f))
    }
    val isDark = fraction != null && fraction > 0.5f
    val textColor = when {
        !inCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        isDark -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.bodySmall, color = textColor)
    }
}
