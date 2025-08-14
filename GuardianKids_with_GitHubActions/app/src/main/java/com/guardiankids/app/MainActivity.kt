package com.guardiankids.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.ktx.Firebase
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.messaging.ktx.messaging
import com.guardiankids.app.services.*
import com.guardiankids.app.data.CloudSync
import com.guardiankids.app.ui.theme.GKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase init
        Firebase.analytics
        Firebase.messaging.subscribeToTopic("guardian_global")

        // Auto-pull de reglas en arranque
        val sp = getSharedPreferences("guardian_prefs", MODE_PRIVATE)
        CloudSync(this).pullRules("kid-001") { rules ->
            val blocked = (rules["blocked_apps"] as? String) ?: ""
            val always = (rules["always_allowed"] as? String) ?: ""
            val whitelist = (rules["whitelist_mode"] as? Boolean) ?: false
            val start = (rules["sched_start"] as? String)?.toIntOrNull() ?: 0
            val end = (rules["sched_end"] as? String)?.toIntOrNull() ?: 0
            sp.edit().putString("blocked_apps", blocked).putString("always_allowed", always)
                .putBoolean("whitelist_mode", whitelist)
                .putInt("sched_start", start).putInt("sched_end", end).apply()
            Toast.makeText(this, "Reglas cargadas desde la nube", Toast.LENGTH_SHORT).show()
        }

        setContent {
            GKTheme {
                var tab by remember { mutableStateOf(0) }
                val tabs = listOf("Bloqueo","GPS","Entorno","Nube","Premium","Chat","Emociones","Pingüino")
                Scaffold(
                    topBar = { TopAppBar(title={ Text("GuardianKids - ${'$'}{tabs[tab]}") }) },
                    bottomBar = {
                        NavigationBar {
                            tabs.forEachIndexed { i, t ->
                                NavigationBarItem(
                                    selected = tab==i,
                                    onClick = { tab=i },
                                    label={ Text(t) },
                                    icon={ Icon(Icons.Default.Favorite, contentDescription=null) }
                                )
                            }
                        }
                    }
                ) { p ->
                    Box(Modifier.padding(p)) {
                        when(tab){
                            0 -> BlockerScreen(cloud = CloudSync(this@MainActivity))
                            1 -> LocationScreen()
                            2 -> AmbientScreen()
                            3 -> CloudScreen(cloud = CloudSync(this@MainActivity))
                            4 -> PremiumScreen(cloud = CloudSync(this@MainActivity))
                            5 -> ChatScreen()
                            6 -> EmotionsScreen()
                            7 -> PenguinScreen()
                        }
                    }
                }
            }
        }
    }
}
