package com.guardiankids.app

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.guardiankids.app.data.CloudSync
import com.guardiankids.app.services.AmbientAudioService
import com.guardiankids.app.services.LocationService

@Composable fun BlockerScreen(cloud: CloudSync){
    val ctx = LocalContext.current
    val sp = ctx.getSharedPreferences("guardian_prefs", MODE_PRIVATE)
    var childId by remember { mutableStateOf("kid-001") }
    var blacklist by remember { mutableStateOf("com.zhiliaoapp.musically, com.instagram.android, com.android.settings, com.google.android.youtube") }
    var always by remember { mutableStateOf("com.whatsapp, com.android.dialer") }
    var whitelist by remember { mutableStateOf(false) }
    var start by remember { mutableStateOf("1260") }
    var end by remember { mutableStateOf("420") }
    var limitPkg by remember { mutableStateOf("com.zhiliaoapp.musically") }
    var limitMin by remember { mutableStateOf("30") }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Bloqueo amigable y colorido", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value=childId,onValueChange={childId=it},label={Text("ID del niño")}, modifier=Modifier.fillMaxWidth())
        OutlinedTextField(value=blacklist,onValueChange={blacklist=it},label={Text("Apps a bloquear (paquetes)")}, modifier=Modifier.fillMaxWidth())
        OutlinedTextField(value=always,onValueChange={always=it},label={Text("Siempre permitidas")}, modifier=Modifier.fillMaxWidth())
        Row {
            Text("Modo lista blanca (escuela)"); Spacer(Modifier.width(8.dp))
            Switch(checked=whitelist, onCheckedChange={whitelist=it})
        }
        Row {
            OutlinedTextField(value=start,onValueChange={start=it},label={Text("Inicio (min)")}, modifier=Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(value=end,onValueChange={end=it},label={Text("Fin (min)")}, modifier=Modifier.weight(1f))
        }
        Row {
            OutlinedTextField(value=limitPkg,onValueChange={limitPkg=it},label={Text("Paquete con límite")}, modifier=Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(value=limitMin,onValueChange={limitMin=it},label={Text("Minutos")}, modifier=Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick={ if (!Settings.canDrawOverlays(ctx)) ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE082), contentColor = Color(0xFF0B3D91))
            ){ Text("Permiso overlay") }

            Button(
                onClick={
                    sp.edit().putString("blocked_apps", blacklist).putString("always_allowed", always)
                        .putBoolean("whitelist_mode", whitelist)
                        .putInt("sched_start", start.toIntOrNull()?:0).putInt("sched_end", end.toIntOrNull()?:0)
                        .putInt("limit_$limitPkg", limitMin.toIntOrNull()?:0).apply()
                    cloud.pushRules(childId, mapOf(
                        "blocked_apps" to blacklist, "always_allowed" to always,
                        "whitelist_mode" to whitelist, "sched_start" to start, "sched_end" to end,
                        "limits" to mapOf(limitPkg to limitMin)
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7), contentColor = Color(0xFF0B3D91))
            ){ Text("Guardar + Subir a nube") }
        }
    }
}

@Composable fun LocationScreen(){
    val ctx = LocalContext.current
    var geofence by remember { mutableStateOf("19.4326, -99.1332, 200") }
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row {
            Button(onClick={ ctx.startForegroundService(Intent(ctx, LocationService::class.java)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BD9FF), contentColor = Color(0xFF0B3D91))
            ){ Text("Iniciar GPS") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick={ ctx.stopService(Intent(ctx, LocationService::class.java)) }){ Text("Detener") }
        }
        OutlinedTextField(value=geofence,onValueChange={geofence=it},label={Text("Geocerca lat,lng,radio")}, modifier=Modifier.fillMaxWidth())
        Button(
            onClick={
                val sp = ctx.getSharedPreferences("guardian_prefs", MODE_PRIVATE)
                val p = geofence.split(",")
                if (p.size>=3){
                    val arr = org.json.JSONArray()
                    val o = org.json.JSONObject()
                    o.put("lat", p[0].trim().toDoubleOrNull()?:0.0)
                    o.put("lng", p[1].trim().toDoubleOrNull()?:0.0)
                    o.put("radius", p[2].trim().toDoubleOrNull()?:200.0)
                    arr.put(o)
                    sp.edit().putString("geofences", arr.toString()).apply()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCCBC), contentColor = Color(0xFF0B3D91))
        ){ Text("Guardar geocerca") }
    }
}

@Composable fun AmbientScreen(){
    val ctx = LocalContext.current
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row {
            Button(onClick={ ctx.startForegroundService(Intent(ctx, AmbientAudioService::class.java)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8BBD0), contentColor = Color(0xFF0B3D91))
            ){ Text("Iniciar entorno") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick={ ctx.stopService(Intent(ctx, AmbientAudioService::class.java)) }){ Text("Detener") }
        }
        Text("El nivel de sonido se muestra en la notificación del sistema.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable fun CloudScreen(cloud: CloudSync){
    var childId by remember { mutableStateOf("kid-001") }
    var lastRules by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value=childId,onValueChange={childId=it},label={Text("ID del niño")})
        Button(onClick={ cloud.pullRules(childId){ lastRules = it } },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BD9FF), contentColor = Color(0xFF0B3D91))
        ){ Text("Bajar reglas de nube") }
        Text("Reglas descargadas: ${lastRules}")
    }
}

@Composable fun PenguinScreen(){
    Text("🐧 Ups… se te acabó tu tiempo", modifier=Modifier.padding(16.dp), color = Color(0xFF0B3D91))
}
