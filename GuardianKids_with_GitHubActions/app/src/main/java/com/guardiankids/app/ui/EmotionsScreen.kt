package com.guardiankids.app

import android.Manifest
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

@Composable
fun EmotionsScreen(){
    val ctx = LocalContext.current
    var result by remember { mutableStateOf("Iniciando cámara…") }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(ctx) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    Column(Modifier.fillMaxSize()) {
        AndroidView(factory = { c ->
            val view = PreviewView(c)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                val selector = CameraSelector.DEFAULT_FRONT_CAMERA
                val options = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build()
                val detector = FaceDetection.getClient(options)
                val analysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640,480)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val img = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        detector.process(img).addOnSuccessListener { faces ->
                            result = if (faces.isNotEmpty()) {
                                val f = faces[0]
                                val smile = f.smilingProbability ?: -1f
                                val leftEye = f.leftEyeOpenProbability ?: -1f
                                val rightEye = f.rightEyeOpenProbability ?: -1f
                                "Caras: ${faces.size} | Sonrisa: %.2f | Ojo izq: %.2f | Ojo der: %.2f".format(smile, leftEye, rightEye)
                            } else "Sin rostros"
                        }.addOnCompleteListener { imageProxy.close() }
                    } else imageProxy.close()
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner = (ctx as androidx.lifecycle.LifecycleOwner), selector, preview, analysis)
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(c))
            view
        }, modifier = Modifier.weight(1f))
        Text(result, modifier = Modifier.padding(12.dp))
    }
}
