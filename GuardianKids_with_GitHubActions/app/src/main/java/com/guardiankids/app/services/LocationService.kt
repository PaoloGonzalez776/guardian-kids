package com.guardiankids.app.services

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import kotlin.math.pow
import com.guardiankids.app.data.CloudSync

class LocationService : Service(), LocationListener {
    private lateinit var lm: LocationManager
    private val inside = mutableSetOf<Int>()
    private val childId = "kid-001"

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification("Ubicación activa")
        lm = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, this)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 5f, this)
        } catch (_: SecurityException) {}
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onDestroy(){ super.onDestroy(); try{ lm.removeUpdates(this) } catch(_:Exception){} }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onLocationChanged(location: Location) {
        update("GPS: %.5f, %.5f".format(location.latitude, location.longitude))
        CloudSync(this).pushEvent(childId, mapOf("type" to "location", "lat" to location.latitude, "lng" to location.longitude))
        checkGeofences(location.latitude, location.longitude)
    }
    private fun checkGeofences(lat: Double, lng: Double){
        val sp = getSharedPreferences("guardian_prefs", MODE_PRIVATE)
        val json = sp.getString("geofences","") ?: return
        val arr = JSONArray(json)
        for (i in 0 until arr.length()){
            val o = arr.getJSONObject(i)
            val d = dist(lat,lng,o.getDouble("lat"),o.getDouble("lng"))
            val r = o.getDouble("radius")
            val insideNow = d <= r
            if (insideNow && !inside.contains(i)) { inside.add(i); push("Entró a geocerca #$i"); CloudSync(this).pushEvent(childId, mapOf("type" to "geofence_in", "idx" to i)) }
            if (!insideNow && inside.contains(i)) { inside.remove(i); push("Salió de geocerca #$i"); CloudSync(this).pushEvent(childId, mapOf("type" to "geofence_out", "idx" to i)) }
        }
    }
    private fun dist(aLat: Double, aLng: Double, bLat: Double, bLng: Double): Double {
        val R=6371000.0
        val dLat = Math.toRadians(bLat-aLat); val dLng = Math.toRadians(bLng-aLng)
        val s1 = Math.sin(dLat/2).pow(2.0) + Math.cos(Math.toRadians(aLat))*Math.cos(Math.toRadians(bLat))*Math.sin(dLng/2).pow(2.0)
        val c = 2 * Math.atan2(Math.sqrt(s1), Math.sqrt(1-s1))
        return R*c
    }
    private fun push(text:String){
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val n = NotificationCompat.Builder(this,"guardian_location").setContentTitle("GuardianKids").setContentText(text).setSmallIcon(android.R.drawable.ic_menu_mylocation).build()
        nm.notify(2000+text.hashCode(), n)
    }
    private fun startForegroundWithNotification(text:String){
        val id="guardian_location"
        if (Build.VERSION.SDK_INT>=26){ val ch=NotificationChannel(id,"Ubicación",NotificationManager.IMPORTANCE_LOW); val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager; nm.createNotificationChannel(ch) }
        val n:Notification = NotificationCompat.Builder(this,id).setContentTitle("GuardianKids").setContentText(text).setSmallIcon(android.R.drawable.ic_menu_mylocation).build()
        startForeground(1001,n)
    }
    private fun update(text:String){ val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager; val n=NotificationCompat.Builder(this,"guardian_location").setContentTitle("GuardianKids").setContentText(text).setSmallIcon(android.R.drawable.ic_menu_mylocation).build(); nm.notify(1001,n) }
    @Deprecated("Deprecated") override fun onStatusChanged(provider:String?, status:Int, extras:Bundle?) {}
}
