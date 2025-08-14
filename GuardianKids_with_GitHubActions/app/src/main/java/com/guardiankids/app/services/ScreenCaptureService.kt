package com.guardiankids.app.services

import android.app.*
import android.content.*
import android.media.MediaRecorder
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class ScreenCaptureService : Service() {
    companion object {
        var data: Intent? = null
        var resultCode: Int = Activity.RESULT_CANCELED
    }

    private var recorder: MediaRecorder? = null
    private var virtualDisplay: android.hardware.display.VirtualDisplay? = null
    private var projection: android.media.projection.MediaProjection? = null
    private var file: File? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundWithNotification("Grabando pantalla…")
        startRecording()
        return START_STICKY
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    private fun startRecording(){
        if (data == null) return
        val dir = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "captures")
        dir.mkdirs()
        file = File(dir, "capture_${System.currentTimeMillis()}.mp4")
        recorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoFrameRate(20)
            setVideoSize(720, 1280)
            setVideoEncodingBitRate(3_000_000)
            setOutputFile(file!!.absolutePath)
            prepare()
        }
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mpm.getMediaProjection(resultCode, data!!)
        val metrics = resources.displayMetrics
        virtualDisplay = projection?.createVirtualDisplay(
            "gk_capture",
            720, 1280, metrics.densityDpi,
            0, recorder!!.surface, null, null
        )
        recorder?.start()
        // Programar paro después de 20s por demo y subir a Storage
        android.os.Handler(mainLooper).postDelayed({ stopRecording() }, 20_000)
    }

    private fun stopRecording(){
        try { recorder?.stop() } catch (_: Exception) {}
        try { recorder?.release() } catch (_: Exception) {}
        recorder = null
        try { virtualDisplay?.release() } catch (_: Exception) {}
        try { projection?.stop() } catch (_: Exception) {}
        if (file?.exists() == true) {
            val ref = Firebase.storage.reference.child("captures/${file!!.name}")
            ref.putFile(android.net.Uri.fromFile(file!!))
        }
        stopForeground(true); stopSelf()
    }

    private fun startForegroundWithNotification(text: String){
        val id="guardian_capture"
        if (Build.VERSION.SDK_INT>=26){
            val ch = NotificationChannel(id, "Grabación de pantalla", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        val n = NotificationCompat.Builder(this,"guardian_capture").setContentTitle("GuardianKids").setContentText(text).setSmallIcon(android.R.drawable.presence_video_online).build()
        startForeground(1003,n)
    }
}
