package ru.automusic.app

import android.content.Context
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.os.Build

internal fun diagnosticReport(context: Context): String = buildString {
    appendLine("AutoMusic v0.9 Diagnostic")
    appendLine("Android: ${Build.VERSION.RELEASE} / SDK ${Build.VERSION.SDK_INT}")
    val c = yandexController(context)
    val p = c?.playbackState
    val m = c?.metadata
    val mask = p?.actions ?: 0L
    appendLine("Session connected: ${c != null}")
    appendLine("Yandex version: " + runCatching { context.packageManager.getPackageInfo("ru.yandex.music", 0).versionName }.getOrNull())
    appendLine("Playback state: ${p?.state}")
    appendLine("Actions mask: ${mask} (0x${mask.toString(16)})")
    val flags = listOf("PLAY" to PlaybackState.ACTION_PLAY, "PAUSE" to PlaybackState.ACTION_PAUSE,
        "NEXT" to PlaybackState.ACTION_SKIP_TO_NEXT, "PREVIOUS" to PlaybackState.ACTION_SKIP_TO_PREVIOUS,
        "SEEK" to PlaybackState.ACTION_SEEK_TO, "SET_RATING" to PlaybackState.ACTION_SET_RATING,
        "PLAY_FROM_MEDIA_ID" to PlaybackState.ACTION_PLAY_FROM_MEDIA_ID,
        "PLAY_FROM_SEARCH" to PlaybackState.ACTION_PLAY_FROM_SEARCH,
        "SKIP_TO_QUEUE_ITEM" to PlaybackState.ACTION_SKIP_TO_QUEUE_ITEM,
        "PREPARE" to PlaybackState.ACTION_PREPARE)
    flags.forEach { (name, bit) -> appendLine("$name: ${mask and bit != 0L}") }
    appendLine("Rating type: ${c?.ratingType}")
    for (key in listOf(MediaMetadata.METADATA_KEY_RATING, MediaMetadata.METADATA_KEY_USER_RATING)) {
        val r = m?.getRating(key)
        appendLine("$key: style=${r?.ratingStyle}, rated=${r?.isRated}, heart=${r?.hasHeart()}, thumbUp=${r?.isThumbUp}")
    }
    appendLine("Queue size: ${c?.queue?.size ?: 0}")
    appendLine("Active queue ID: ${p?.activeQueueItemId}")
    appendLine("Metadata keys: ${m?.keySet()?.sorted()}")
    appendLine("Session extras keys: ${c?.extras?.keySet()?.sorted()}")
    appendLine("Custom actions:")
    p?.customActions.orEmpty().forEach { a ->
        appendLine("  id=${a.action}; label=${a.name}; extras keys=${a.extras?.keySet()?.sorted()}")
    }
    appendLine("Notification listener connected: ${MusicNotificationListener.instance != null}")
    val notifications = runCatching { MusicNotificationListener.instance?.activeNotifications }.getOrNull()
    notifications?.filter { it.packageName == "ru.yandex.music" }?.forEach { n ->
        appendLine("Yandex notification category: ${n.notification.category}")
        n.notification.actions?.forEachIndexed { i, a ->
            appendLine("  action $i: label=${a.title}; semantic=${a.semanticAction}; pendingIntent=${a.actionIntent != null}; extras keys=${a.extras?.keySet()?.sorted()}")
        }
    }
    appendLine("No account credentials or extras values collected.")
}
