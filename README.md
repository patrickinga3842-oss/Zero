# ZERO · Asistente Android Kotlin (versión 0.1)

Proyecto nativo Android Studio para Galaxy S26 y Android 8.0 o superior (minSdk 26, targetSdk 35). **Es un prototipo funcional, no una app comprobada en el dispositivo**.

## Abrir y compilar
1. Instalar Android Studio reciente, Android SDK Platform 35 y JDK 17.
2. Abrir la carpeta `ZeroAssistant` como proyecto Gradle.
3. Permitir que Android Studio descargue **Gradle 8.9**, Android Gradle Plugin 8.7.3 y Kotlin 2.0.21. Si no genera el wrapper, en un equipo con Gradle 8.9 ejecutar `gradle wrapper --gradle-version 8.9`.
4. Conectar Galaxy S26 por USB con depuración USB o usar un emulador con servicio de reconocimiento de voz.
5. Pulsar Run ▶. Para obtener APK: Build > Build APK(s), se genera `app/build/outputs/apk/debug/app-debug.apk`.
6. Autorizar Micrófono, Notificaciones y, en el botón de la app, Alarmas exactas si se desean tiempos precisos.

## Pruebas manuales
- Botón Micrófono -> "pon una alarma a las 18:30".
- "recuérdame enviar el informe mañana a las 9".
- "recuérdame llamar en 2 minutos".
- "cuáles son mis recordatorios".
- Activar manos libres -> "Hola Zero, recuérdame comprar pan en 3 minutos" con servicio visible.
- Bloquear pantalla, reiniciar teléfono, quitar permiso de alarmas exactas y comprobar restricciones (NO hay garantía de hotword con pantalla apagada).

## Alcance y restricciones
- Escucha bajo demanda con SpeechRecognizer del sistema; la disponibilidad y conexión dependen del reconocedor instalado y sus paquetes de idiomas.
- El modo «Hola Zero» experimental usa servicio en primer plano tipo micrófono, activado desde la interfaz, con notificación permanente. **Android no ofrece a apps ordinarias el wake word de bajo consumo del asistente del sistema**. La continuidad, batería y funcionamiento con pantalla bloqueada pueden variar; no se reinicia automáticamente en el arranque.
- El motor reutiliza SpeechRecognizer por sesiones, no garantiza escucha permanente ni detección robusta de palabras clave. No ejecuta comandos ajenos al conjunto admitido.
- Las alarmas exactas requieren autorización especial en Android 12+; si se deniega, se envía una alarma inexacta y se informa al usuario. Android puede retrasarla. Las notificaciones dependen del permiso de Android 13+.
- Los avisos se guardan en preferencias privadas y se reprograman después del reinicio o actualización de app; el esquema JSON es deliberadamente sencillo para el MVP.
- No se envían comandos a un servidor propio. Sin embargo, el proveedor de reconocimiento del dispositivo podría procesar audio en línea; no se debe prometer reconocimiento offline.
- Las alarmas avisan por notificación y sonido de notificación, **no** constituyen todavía una pantalla de despertador con timbre persistente ni modo posponer.

## Etapas posteriores
- Reconocedor de wake word dedicado compatible con requisitos de Android y servicio en primer plano, optimizado y probado en S26.
- Gestión avanzada: borrar/posponer alarmas, interfaz de edición, días de la semana, repetición, pruebas automatizadas e internacionalización.
- Seguridad y privacidad: política clara de retención y acceso al micrófono, opción para desactivar cualquier escucha.

## Estructura
- `MainActivity.kt`: UI, permisos, micrófono, activación opt-in.
- `Voice.kt`: reconocimiento Android y síntesis de voz.
- `Commands.kt`: análisis limitado y creación de avisos.
- `Alerts.kt`: persistencia, programación y recepción, reinicio.
- `HotwordService.kt`: intento experimental de «Hola Zero» mientras el servicio de micrófono esté activo.

## Estado de verificación
Se comprueba la estructura y sintaxis por inspección local. No se ha ejecutado Android Studio ni compilado un APK por falta de Gradle/Android SDK en el entorno. Debe verificarse el funcionamiento de `SpeechRecognizer` y el servicio de micrófono en el dispositivo real antes de uso intensivo.
