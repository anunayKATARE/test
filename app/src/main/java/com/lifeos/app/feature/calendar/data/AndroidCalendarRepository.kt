package com.lifeos.app.feature.calendar.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import com.lifeos.app.feature.calendar.domain.CalendarEvent
import com.lifeos.app.feature.calendar.domain.CalendarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidCalendarRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : CalendarRepository {

    override fun hasPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    override suspend fun getEventsForDay(date: LocalDate): List<CalendarEvent> = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext emptyList()
        val zone = ZoneId.systemDefault()
        val fromMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val toMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        queryInstances(fromMillis, toMillis)
    }

    private fun queryInstances(fromMillis: Long, toMillis: Long): List<CalendarEvent> {
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .also { ContentUris.appendId(it, fromMillis) }
            .also { ContentUris.appendId(it, toMillis) }
            .build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.CALENDAR_ID,
        )

        val events = mutableListOf<CalendarEvent>()
        context.contentResolver.query(uri, projection, null, null, CalendarContract.Instances.BEGIN)
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    events.add(
                        CalendarEvent(
                            id = cursor.getLong(0),
                            title = cursor.getString(1) ?: "(No title)",
                            startAt = Instant.ofEpochMilli(cursor.getLong(2)),
                            endAt = Instant.ofEpochMilli(cursor.getLong(3)),
                            isAllDay = cursor.getInt(4) != 0,
                            calendarId = cursor.getLong(5),
                        )
                    )
                }
            }
        return events
    }
}
