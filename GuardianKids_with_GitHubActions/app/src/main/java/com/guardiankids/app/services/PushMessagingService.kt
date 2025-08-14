package com.guardiankids.app.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import android.content.Intent
import android.content.Context

class PushMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) { Log.i("GuardianKids","FCM token: $token") }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.i("GuardianKids","Push recibido: ${message.data}")
        val data = message.data
        val sp = getSharedPreferences("guardian_prefs", MODE_PRIVATE)

        when (data["cmd"]) {
            "lock_total_on" -> sp.edit().putBoolean("remote_lock_total", true).apply()
            "lock_total_off" -> sp.edit().putBoolean("remote_lock_total", false).apply()
            "set_rules" -> {
                sp.edit()
                    .putString("blocked_apps", data["blocked_apps"] ?: "")
                    .putString("always_allowed", data["always_allowed"] ?: "")
                    .putBoolean("whitelist_mode", data["whitelist_mode"]?.toBoolean() ?: false)
                    .putInt("sched_start", data["sched_start"]?.toIntOrNull() ?: 0)
                    .putInt("sched_end", data["sched_end"]?.toIntOrNull() ?: 0)
                    .apply()
            }
            "start_gps" -> startService(Intent(this, LocationService::class.java))
            "stop_gps" -> stopService(Intent(this, LocationService::class.java))
            "start_audio" -> startService(Intent(this, AmbientAudioService::class.java))
            "stop_audio" -> stopService(Intent(this, AmbientAudioService::class.java))
        }
    }
}
