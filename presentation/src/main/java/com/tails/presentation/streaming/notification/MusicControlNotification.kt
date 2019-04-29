package com.tails.presentation.streaming.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.tails.domain.entities.VideoMeta
import com.tails.presentation.R
import androidx.media.app.NotificationCompat as MediaNotificationCompat

object MusicControlNotification {

    private const val CHANNEL_ID = "KKORI_NOTIFICATION"
    private const val name = "Kkori Music"
    private const val description = "Music Control"

    private var notificationManager: NotificationManager? = null

    private val prevIntent = Intent("kkori.previous")
    private val nextIntent = Intent("kkori.next")
    private val pauseIntent = Intent("kkori.pause")
    private val cancelIntent = Intent("kkori.cancel")

    private var prevPendingIntent: PendingIntent? = null
    private var pausePendingIntent: PendingIntent? = null
    private var nextPendingIntent: PendingIntent? = null
    private var cancelPendingIntent: PendingIntent? = null

    fun showNotification(context: Context, videoMeta: VideoMeta) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager == null) {
                val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
                channel.description = description
                channel.setSound(null, null)
                channel.setShowBadge(false)

                notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager!!.createNotificationChannel(channel)
            }
        }

        if (prevPendingIntent == null || pausePendingIntent == null || nextPendingIntent == null) {
            prevPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                prevIntent,
                0
            )

            pausePendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                pauseIntent,
                0
            )

            nextPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                nextIntent,
                0
            )

            cancelPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                cancelIntent,
                0
            )
        }

        val mediaSessionCompat = MediaSessionCompat(context, "Kkori Music")
        AsyncTask.execute {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID).apply {
                setSmallIcon(R.drawable.ic_tail)
                setLargeIcon(Glide.with(context).asBitmap().load(videoMeta.getMqImageUrl()).submit().get())

                setContentTitle(videoMeta.title)
                setContentText(videoMeta.author)

                setStyle(
                    MediaNotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                )

                addAction(R.drawable.ic_previous_12dp, "Previous", prevPendingIntent)
                addAction(R.drawable.ic_play_pause_12dp, "Pause", pausePendingIntent)
                addAction(R.drawable.ic_next_12dp, "Next", nextPendingIntent)

                setOngoing(true)
                setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            }.build()
            with(NotificationManagerCompat.from(context)) {
                notify(20011203, notification)
            }
        }
    }

    fun removeNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) { cancelAll() }
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager!!.deleteNotificationChannel(CHANNEL_ID)
                notificationManager!!.cancelAll()
                notificationManager = null
            }
        }
    }
}