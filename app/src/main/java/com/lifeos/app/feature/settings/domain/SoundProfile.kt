package com.lifeos.app.feature.settings.domain

enum class SoundProfile(val label: String, val channelId: String) {
    SOUND_AND_VIBRATE("Sound & Vibrate", "task_alarms"),
    VIBRATE_ONLY("Vibrate Only", "task_alarms_vibrate"),
    SILENT("Silent", "task_alarms_silent"),
}
