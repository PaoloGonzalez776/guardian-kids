# GuardianKids v2.3 (Firebase + Premium + Chat + Emociones + Captura de Pantalla)

Novedades:
- **Chat seguro** padre–hijo en Firestore (colección `chats/{roomId}/messages`).
- **Detección de emociones** (ML Kit, demo preparada para conectar a cámara).
- **Solicitud de captura de pantalla** por MediaProjection (actividad `ScreenCaptureRequestActivity` y servicio `ScreenCaptureService`).
- Tabs nuevas en la app: Premium, Chat, Emociones.

Notas:
- MediaProjection requiere **consentimiento del usuario** 1 sola vez. Después podemos lanzar el servicio cuando haya una alerta.
- Para chat E2E, cifrar el campo `text` antes de enviar (se puede añadir en v2.4).
