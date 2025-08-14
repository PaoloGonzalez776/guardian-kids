package com.guardiankids.app.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.WindowManager
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import android.graphics.PixelFormat
import android.graphics.Color
import android.os.Build
import android.content.Context
import java.util.Calendar

class GuardianAccessibilityService : AccessibilityService() {
    private var overlay: FrameLayout? = null
    private var currentPkg: String? = null
    private var lastTs = System.currentTimeMillis()
    private var showing = false

    private fun sp() = getSharedPreferences("guardian_prefs", Context.MODE_PRIVATE)
    private fun getSet(key:String): Set<String> { val csv = sp().getString(key,"") ?: ""; return csv.split(",").map{it.trim()}.filter{it.isNotEmpty()}.toSet() }
    private fun inSchedule(): Boolean {
        val s = sp().getInt("sched_start",-1); val e = sp().getInt("sched_end",-1); if (s<0||e<0) return false
        val cal = Calendar.getInstance(); val m = cal.get(Calendar.HOUR_OF_DAY)*60 + cal.get(Calendar.MINUTE)
        return if (s<=e) (m in s..e) else (m>=s || m<=e)
    }
    private fun limitFor(pkg: String) = sp().getInt("limit_$pkg",0)
    private fun usedMin(pkg: String) = (sp().getLong("used_$pkg",0)/60000L).toInt()
    private fun addUsage(pkg: String, ms: Long){ sp().edit().putLong("used_$pkg", sp().getLong("used_$pkg",0)+ms).apply() }

    private fun shouldBlock(pkg: String): Boolean {
        if (sp().getBoolean("remote_lock_total", false)) return true
        val whitelistMode = sp().getBoolean("whitelist_mode", false)
        val blocked = getSet("blocked_apps")
        val always = getSet("always_allowed")
        if (always.contains(pkg)) return false
        if (inSchedule()) { if (whitelistMode) return !blocked.contains(pkg); if (blocked.contains(pkg)) return true }
        else { if (blocked.contains(pkg)) return true }
        val lim = limitFor(pkg); if (lim>0 && usedMin(pkg)>=lim) return true
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event==null) return
        if (event.eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            val now = System.currentTimeMillis()
            currentPkg?.let { addUsage(it, now-lastTs) }; lastTs = now
            val pkg = event.packageName?.toString() ?: return
            currentPkg = pkg
            if (shouldBlock(pkg)) { showOverlay(pkg); if (pkg=="com.android.settings") startService(Intent(this, AntiEscapePhotoService::class.java)) } else hideOverlay()
        }
    }

    private fun showOverlay(pkg: String){
        if (showing) return
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        val layout = FrameLayout(this)
        layout.setBackgroundColor(Color.parseColor("#E6F7FF"))
        val tv = TextView(this)
        tv.text = "🐧 Ups… se te acabó tu tiempo"
        tv.setTextColor(Color.parseColor("#0B3D91")); tv.textSize = 22f; tv.gravity = Gravity.CENTER
        layout.addView(tv, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        try { wm.addView(layout, params); overlay=layout; showing=true } catch (_: Exception) {}
    }
    private fun hideOverlay(){
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        overlay?.let { try { wm.removeView(it) } catch (_: Exception) {} ; overlay=null }
        showing=false
    }
    override fun onInterrupt(){ hideOverlay() }
}
