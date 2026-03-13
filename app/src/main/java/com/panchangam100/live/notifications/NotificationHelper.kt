package com.panchangam100.live.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.panchangam100.live.MainActivity
import com.panchangam100.live.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

object NotificationHelper {

    const val CHANNEL_ID_GENERAL    = "panchangam_general"
    const val CHANNEL_ID_FESTIVALS  = "panchangam_festivals"
    const val CHANNEL_ID_DAILY      = "panchangam_daily"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "General app notifications" }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID_FESTIVALS,
                "Festivals & Events",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Festival reminders and special events"
                enableVibration(true)
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID_DAILY,
                "Daily Panchangam",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily Panchangam details" }
        )
    }

    /** Show a simple text notification. */
    fun showNotification(
        context: Context,
        title: String,
        body: String,
        channelId: String = CHANNEL_ID_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(context, notificationId, notification)
    }

    /** Show a notification with a large image (BigPicture style). */
    suspend fun showImageNotification(
        context: Context,
        title: String,
        body: String,
        imageUrl: String,
        channelId: String = CHANNEL_ID_FESTIVALS,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val bitmap = loadBitmapFromUrl(imageUrl)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (bitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as Bitmap?)   // hides large icon when expanded
                    .setSummaryText(body)
            )
            builder.setLargeIcon(bitmap)
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        notify(context, notificationId, builder.build())
    }

    private fun notify(context: Context, id: Int, notification: Notification) {
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted on API 33+
        }
    }

    private suspend fun loadBitmapFromUrl(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val stream = URL(url).openStream()
            BitmapFactory.decodeStream(stream)
        } catch (_: IOException) {
            null
        }
    }
}
