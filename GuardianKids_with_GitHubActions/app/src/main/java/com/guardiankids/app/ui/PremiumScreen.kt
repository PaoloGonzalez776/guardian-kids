package com.guardiankids.app

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.guardiankids.app.data.CloudSync

@Composable fun PremiumScreen(cloud: CloudSync){
    val ctx = LocalContext.current
    var childId by remember { mutableStateOf("kid-001") }
    var sosNote by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Funciones Premium", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value=childId,onValueChange={childId=it},label={Text("ID del niño")}, modifier=Modifier.fillMaxWidth())

        Button(onClick={ cloud.pushEvent(childId, mapOf("type" to "SOS", "note" to sosNote)) }){ Text("Enviar SOS (nube)") }
        OutlinedTextField(value=sosNote,onValueChange={sosNote=it},label={Text("Nota SOS (opcional)")}, modifier=Modifier.fillMaxWidth())

        Button(onClick={
            val sp = ctx.getSharedPreferences("guardian_prefs", Context.MODE_PRIVATE)
            sp.edit().putBoolean("remote_lock_total", true).apply()
        }){ Text("Activar bloqueo total (remoto)") }

        Button(onClick={
            val sp = ctx.getSharedPreferences("guardian_prefs", Context.MODE_PRIVATE)
            sp.edit().putBoolean("remote_lock_total", false).apply()
        }){ Text("Desactivar bloqueo total (remoto)") }

        Button(onClick={ cloud.pushEvent(childId, mapOf("type" to "request_daily_report")) }){ Text("Solicitar reporte diario") }
    }
}
