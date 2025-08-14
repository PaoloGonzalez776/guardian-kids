package com.guardiankids.app

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import com.guardiankids.app.services.ScreenCaptureService

class ScreenCaptureRequestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mpm.createScreenCaptureIntent(), 5001)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==5001 && data!=null) {
            ScreenCaptureService.resultCode = resultCode
            ScreenCaptureService.data = data
            startForegroundService(Intent(this, com.guardiankids.app.services.ScreenCaptureService::class.java))
        }
        finish()
    }
}
