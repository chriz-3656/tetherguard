package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.models.Incident
import com.example.data.models.IncidentSeverity

class TetherNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TetherGuard Tamper Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time security notifications for laptop physical tamper events"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250, 150, 400)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showIncidentNotification(incident: Incident) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_INCIDENT_ID, incident.id)
            putExtra(EXTRA_EVENT_ID, incident.eventId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            incident.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val priority = when (incident.severity) {
            IncidentSeverity.CRITICAL, IncidentSeverity.HIGH -> NotificationCompat.PRIORITY_MAX
            else -> NotificationCompat.PRIORITY_HIGH
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("🚨 TAMPER DETECTED: ${incident.eventType.displayName}")
            .setContentText("Device: ${incident.deviceName} • Laptop: ${incident.deviceId}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Event: ${incident.eventType.displayName}\n" +
                        "Device: ${incident.deviceName}\n" +
                        "Time: ${incident.timestamp}\n" +
                        "Workstation locked automatically. Tap to review evidence and respond."
                    )
            )
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        notificationManager.notify(incident.id.toInt().coerceAtLeast(1001), notification)
    }

    companion object {
        const val CHANNEL_ID = "tetherguard_alerts_channel"
        const val EXTRA_INCIDENT_ID = "extra_incident_id"
        const val EXTRA_EVENT_ID = "extra_event_id"
    }
}
