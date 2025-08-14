package com.guardiankids.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import android.os.Environment
import androidx.camera.core.ImageCaptureException

class AntiEscapePhotoService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        takePhoto()
        return START_NOT_STICKY
    }

    private fun takePhoto(){
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val capture = ImageCapture.Builder().setTargetRotation(resources.configuration.orientation).build()
            val selector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                provider.unbindAll()
                provider.bindToLifecycle(this as androidx.lifecycle.LifecycleOwner, selector, capture)
            } catch (_: Exception) {}
            val dir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "antifuga"); dir.mkdirs()
            val file = File(dir, "af_${System.currentTimeMillis()}.jpg")
            capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(),
                ContextCompat.getMainExecutor(this),
                object: ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) { stopSelf() }
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Firebase.storage.reference.child("antifuga/${file.name}")
                            .putFile(android.net.Uri.fromFile(file))
                        stopSelf()
                    }
                })
        }, ContextCompat.getMainExecutor(this))
    }
}
