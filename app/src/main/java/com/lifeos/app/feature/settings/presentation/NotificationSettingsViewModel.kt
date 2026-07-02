package com.lifeos.app.feature.settings.presentation

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.settings.domain.NotificationPrefsRepository
import com.lifeos.app.feature.settings.domain.SoundProfile
import com.lifeos.app.feature.task.data.TaskNotificationPoster
import com.lifeos.app.feature.task.domain.Task
import com.lifeos.app.feature.task.domain.TaskAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationDiagnostics(
    val notificationsEnabled: Boolean = false,
    val exactAlarmsAllowed: Boolean = false,
    val batteryUnrestricted: Boolean = false,
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPrefsRepository: NotificationPrefsRepository,
    private val notificationPoster: TaskNotificationPoster,
    private val alarmScheduler: TaskAlarmScheduler,
) : ViewModel() {

    val soundProfile: StateFlow<SoundProfile> = notificationPrefsRepository
        .observeSoundProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SoundProfile.SOUND_AND_VIBRATE)

    private val _diagnostics = MutableStateFlow(readDiagnostics())
    val diagnostics: StateFlow<NotificationDiagnostics> = _diagnostics.asStateFlow()

    private val _testAlarmScheduledAt = MutableStateFlow<Instant?>(null)
    val testAlarmScheduledAt: StateFlow<Instant?> = _testAlarmScheduledAt.asStateFlow()

    fun setSoundProfile(profile: SoundProfile) {
        viewModelScope.launch { notificationPrefsRepository.setSoundProfile(profile) }
    }

    fun refreshDiagnostics() {
        _diagnostics.value = readDiagnostics()
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            notificationPoster.post(
                TEST_NOTIFICATION_ID,
                "Test notification",
                "If you can see this, notifications work. Check your watch too!",
            )
        }
    }

    fun scheduleTestAlarm() {
        val fireAt = Instant.now().plusSeconds(60)
        alarmScheduler.schedule(
            Task(
                id = "diagnostic_test_alarm",
                title = "Test alarm",
                description = "This alarm was scheduled 1 minute ago — task alarms work!",
                date = LocalDate.now(),
                scheduledAt = fireAt,
            ),
        )
        _testAlarmScheduledAt.value = fireAt
    }

    private fun readDiagnostics(): NotificationDiagnostics {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val powerManager = context.getSystemService(PowerManager::class.java)
        return NotificationDiagnostics(
            notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            exactAlarmsAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms(),
            batteryUnrestricted = powerManager.isIgnoringBatteryOptimizations(context.packageName),
        )
    }

    companion object {
        private const val TEST_NOTIFICATION_ID = 999_001
    }
}
