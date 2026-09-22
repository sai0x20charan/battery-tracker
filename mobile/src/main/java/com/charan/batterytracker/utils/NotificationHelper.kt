package com.charan.batterytracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.charan.batterytracker.R
import javax.inject.Inject

open class NotificationHelper @Inject constructor(
    private val context: Context?
) {

    companion object {
        const val HEADPHONES_CHANNEL_ID = "battery-tracker-headphones-notifications"
        const val WEAROS_CHANNEL_ID = "battery-tracker-wearos-notifications"
        const val HEADPHONE_CHANNEL_NAME = "Headphone Battery Notification"
        const val WEAROS_CHANNEL_NAME = "WearOs Battery Notification"
    }

    private val notificationManager: NotificationManager? =
        context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        if (context != null) {
            createNotificationChannel()
        }
    }

    open fun showLowBatteryNotificationForHeadPhones(batteryLevel: String, deviceName: String) {
        val ctx = context ?: return
        val manager = notificationManager ?: return
        val notificationBuilder = NotificationCompat.Builder(ctx, HEADPHONES_CHANNEL_ID)
            .setSmallIcon(R.drawable.earphones)
            .setContentTitle("Low Battery")
            .setContentText("$deviceName is at $batteryLevel%. Please charge your device.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager.notify(1, notificationBuilder)
    }

    open fun showLowBatteryNotificationForWearos(batteryLevel: String, deviceName: String) {
        val ctx = context ?: return
        val manager = notificationManager ?: return
        val notificationBuilder = NotificationCompat.Builder(ctx, WEAROS_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_watch_24)
            .setContentTitle("Low Battery")
            .setContentText("$deviceName is at $batteryLevel%. Please charge your device.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager.notify(2, notificationBuilder)
    }

    private fun createNotificationChannel() {
        val manager = notificationManager ?: return
        val headPhonesChannel = NotificationChannel(
            HEADPHONES_CHANNEL_ID,
            HEADPHONE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val wearOsChannel = NotificationChannel(
            WEAROS_CHANNEL_ID,
            WEAROS_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannels(listOf(headPhonesChannel, wearOsChannel))
    }
}
