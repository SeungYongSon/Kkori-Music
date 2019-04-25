package com.tails.presentation.streaming.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tails.presentation.R

// Operation required
object MusicControlNotification {

    private const val CHANNEL_ID = "KKORI_NOTIFICATION"

    fun makeMusicControllerNotifi(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Kkori Music"
            val description = "Music Control"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }

        val prevIntent = Intent("kkori.previous")
        val nextIntent = Intent("kkori.next")
        val pauseIntent = Intent("kkori.pause")

        val prevPendingIntent = PendingIntent.getActivity(
            context,
            0,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pausePendingIntent = PendingIntent.getActivity(
            context,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextPendingIntent = PendingIntent.getActivity(
            context,
            0,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.notification_template_icon_low_bg)
            .addAction(R.drawable.notification_template_icon_low_bg, "Previous", prevPendingIntent) // #0
            .addAction(R.drawable.notification_template_icon_low_bg, "Pause", pausePendingIntent) // #1
            .addAction(R.drawable.notification_template_icon_low_bg, "Next", nextPendingIntent) // #2
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1)
            )
            .setContentTitle("Music Title")
            .setContentText("Music Maker")
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(127123,notification)
        }
    }
}