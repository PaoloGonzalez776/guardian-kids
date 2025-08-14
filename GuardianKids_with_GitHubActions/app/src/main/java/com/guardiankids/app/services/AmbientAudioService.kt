package com.guardiankids.app.services

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.math.log10

class AmbientAudioService : Service() {
    @Volatile private var running=false
    override fun onCreate(){ super.onCreate(); start("Entorno activo"); running=true; Thread{loop()}.start() }
    override fun onStartCommand(intent: android.content.Intent?, flags:Int, startId:Int):Int = START_STICKY
    override fun onDestroy(){ running=false; super.onDestroy() }
    override fun onBind(intent: android.content.Intent?): IBinder? = null
    private fun loop(){
        val rate=16000
        val bufSz=AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val rec=AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSz)
        val buf=ShortArray(bufSz)
        try{
            rec.startRecording()
            while(running){
                val n=rec.read(buf,0,buf.size)
                if(n>0){
                    var sum = 0.0
                    for (i in 0 until n): sum += buf[i].toDouble()*buf[i].toDouble()
                    val rms = kotlin.math.sqrt(sum / n)
                    val db = 20* log10(rms/32768.0 + 1e-9)
                    update("Nivel dB: %.1f".format(db))
                }
            }
            rec.stop(); rec.release()
        }catch(_:Exception){}
    }
    private fun start(text:String){
        val id="guardian_ambient"
        if(Build.VERSION.SDK_INT>=26){ val ch=NotificationChannel(id,"Entorno",NotificationManager.IMPORTANCE_LOW); val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager; nm.createNotificationChannel(ch) }
        val n:Notification = NotificationCompat.Builder(this,id).setContentTitle("GuardianKids").setContentText(text).setSmallIcon(android.R.drawable.ic_btn_speak_now).build()
        startForeground(1002,n)
    }
    private fun update(text:String){ val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager; val n=NotificationCompat.Builder(this,"guardian_ambient").setContentTitle("GuardianKids").setContentText(text).setSmallIcon(android.R.drawable.ic_btn_speak_now).build(); nm.notify(1002,n) }
}
