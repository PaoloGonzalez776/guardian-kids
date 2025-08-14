# Premium conectado (v2.2)

**Novedades Premium:**

- **Bloqueo total remoto** por FCM (`cmd: lock_total_on/off`).
- **Actualización remota de reglas** por FCM (`cmd: set_rules` con campos).
- **SOS** desde la app (sube evento a Firestore).
- **Iniciar/detener GPS y Entorno** por FCM (`start_gps/stop_gps/start_audio/stop_audio`).
- **Historial**: las ubicaciones y eventos quedan en `children/{childId}/events` en Firestore.
- **Pestaña Premium** en la UI del niño.

## Ejemplos de payload FCM (Data)
- Bloqueo total ON
```
{ "cmd": "lock_total_on" }
```
- Actualizar reglas
```
{
  "cmd":"set_rules",
  "blocked_apps":"com.instagram.android, com.zhiliaoapp.musically",
  "always_allowed":"com.whatsapp, com.android.dialer",
  "whitelist_mode":"false",
  "sched_start":"1260",
  "sched_end":"420"
}
```

> En la consola de Firebase: Cloud Messaging → *Send test message* → ingresa el **token** del dispositivo o suscripción al **topic**.
