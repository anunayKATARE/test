package com.lifeos.app.feature.task.data

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.lifeos.app.R
import com.lifeos.app.feature.settings.domain.NotificationPrefsRepository
import com.lifeos.app.feature.settings.domain.SoundProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Builds and posts task notifications on the channel matching the user's
 * sound profile, with wearable (Galaxy Watch) forwarding enabled.
 */
@Singleton
class TaskNotificationPoster @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPrefsRepository: NotificationPrefsRepository,
) {

    suspend fun post(notificationId: Int, title: String, body: String) {
        val channelId = try {
            notificationPrefsRepository.observeSoundProfile().first().channelId
        } catch (_: Exception) {
            SoundProfile.SOUND_AND_VIBRATE.channelId
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .extend(NotificationCompat.WearableExtender())
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }
}
