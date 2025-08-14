# GuardianKids v2.4 (Full)

Novedades importantes:
- **Chat cifrado (AES-GCM)** end-to-end en la app (cifra antes de escribir a Firestore).
- **Emociones en tiempo real** con **CameraX + ML Kit** (sonrisa/ojos).
- **Grabación de pantalla real (MP4)** con MediaProjection + MediaRecorder y **subida a Firebase Storage** (20s demo).
- **Antifuga con foto**: al bloquear **Ajustes** se toma una foto frontal y se sube a Storage.

Notas de uso:
- MediaProjection requiere consentimiento 1 vez. Lanza `ScreenCaptureRequestActivity` para otorgarlo y luego el servicio grabará ante alertas.
- CameraX requiere permisos de cámara.
- Puedes disparar el bloqueo de Ajustes agregándola a la lista negra (ya viene por defecto). Eso activará la foto antifuga.
