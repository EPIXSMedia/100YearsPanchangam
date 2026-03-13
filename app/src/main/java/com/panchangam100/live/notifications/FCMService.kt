package com.panchangam100.live.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging service.
 *
 * Payload conventions (send from Firebase Console or backend):
 *
 * Data payload keys:
 *   title   – notification title
 *   body    – notification body text
 *   image   – (optional) HTTPS URL of image for BigPicture style
 *   channel – (optional) "panchangam_general" | "panchangam_festivals" | "panchangam_daily"
 *   id      – (optional) integer notification ID (for deduplication)
 */
class FCMService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
        // TODO: send token to your backend server so it can target this device
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")

        // Prefer data payload over notification payload for full control
        val data    = message.data
        val title   = data["title"]   ?: message.notification?.title   ?: "పంచాంగం"
        val body    = data["body"]    ?: message.notification?.body    ?: ""
        val imageUrl = data["image"]  ?: message.notification?.imageUrl?.toString()
        val channel = data["channel"] ?: NotificationHelper.CHANNEL_ID_GENERAL
        val notifId = data["id"]?.toIntOrNull() ?: System.currentTimeMillis().toInt()

        scope.launch {
            if (!imageUrl.isNullOrBlank()) {
                NotificationHelper.showImageNotification(
                    context    = applicationContext,
                    title      = title,
                    body       = body,
                    imageUrl   = imageUrl,
                    channelId  = channel,
                    notificationId = notifId
                )
            } else {
                NotificationHelper.showNotification(
                    context        = applicationContext,
                    title          = title,
                    body           = body,
                    channelId      = channel,
                    notificationId = notifId
                )
            }
        }
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
