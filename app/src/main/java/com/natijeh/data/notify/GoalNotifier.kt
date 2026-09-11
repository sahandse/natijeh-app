package com.natijeh.data.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.natijeh.MainActivity
import com.natijeh.R
import com.natijeh.data.model.MatchAlert

class GoalNotifier(private val context: Context) {
    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ALERTS, context.getString(R.string.live_channel_name), NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_STATUS, context.getString(R.string.live_channel_status), NotificationManager.IMPORTANCE_LOW)
        )
    }

    fun notify(alerts: List<MatchAlert>) {
        if (alerts.isEmpty()) return
        ensureChannels()
        val manager = NotificationManagerCompat.from(context)
        alerts.forEach { alert ->
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_MATCH_ID, alert.matchId)
            }
            val pending = PendingIntent.getActivity(
                context,
                alert.matchId.hashCode() + alert.kind.ordinal,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
                .setSmallIcon(R.drawable.ic_stat_natijeh)
                .setContentTitle(alert.title)
                .setContentText(alert.body)
                .setAutoCancel(true)
                .setContentIntent(pending)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            try {
                manager.notify((alert.matchId + alert.kind.name).hashCode(), notification)
            } catch (_: SecurityException) {
            }
        }
    }

    fun ongoingLive(text: String) = NotificationCompat.Builder(context, CHANNEL_STATUS)
        .setSmallIcon(R.drawable.ic_stat_natijeh)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(text)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    companion object {
        const val CHANNEL_ALERTS = "natijeh_goals"
        const val CHANNEL_STATUS = "natijeh_live_status"
        const val EXTRA_MATCH_ID = "matchId"
        const val FOREGROUND_ID = 1101
    }
}
