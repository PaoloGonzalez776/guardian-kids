package com.guardiankids.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class BatteryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_LOW || intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = "guardian_battery"
            if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(NotificationChannel(ch, "Batería", NotificationManager.IMPORTANCE_DEFAULT))
            val n = NotificationCompat.Builder(context, ch).setContentTitle("GuardianKids").setContentText("Batería: ${level}%").setSmallIcon(android.R.drawable.ic_dialog_alert).build()
            nm.notify(3001, n)
        }
    }
}
