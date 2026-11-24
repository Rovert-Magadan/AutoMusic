package ru.automusic.app

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService

class MusicNotificationListener : NotificationListenerService()

internal fun yandexController(context: Context): MediaController? {
    return try {
        val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        manager.getActiveSessions(ComponentName(context, MusicNotificationListener::class.java))
            .firstOrNull { it.packageName == "ru.yandex.music" }
    } catch (_: Exception) { null }
}
