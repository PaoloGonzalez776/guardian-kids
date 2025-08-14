package com.guardiankids.app.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Context
import android.util.Log
import com.guardiankids.app.data.CloudSync

class GuardianNotificationListener : NotificationListenerService() {
    private fun badWords(): List<String> {
        val sp = getSharedPreferences("guardian_prefs", Context.MODE_PRIVATE)
        val csv = sp.getString("bad_words","groseria1,groseria2") ?: ""
        return csv.split(",").map{it.trim().lowercase()}.filter{it.isNotEmpty()}
    }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val text = (sbn.notification.extras.getCharSequence("android.text")?.toString() ?: "") +
            " " + (sbn.notification.extras.getCharSequence("android.title")?.toString() ?: "")
        val lower = text.lowercase()
        if (badWords().any{ lower.contains(it) }) {
            Log.i("GuardianKids","Notificación con palabra prohibida: $text")
            try { CloudSync(this).pushEvent("kid-001", mapOf("type" to "bad_word", "text" to text.take(120))) } catch (_: Exception) {}
        }
    }
}
