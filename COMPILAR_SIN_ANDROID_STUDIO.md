# Compilar ZERO sin Android Studio (GitHub Actions)

Esta opción requiere una cuenta de GitHub e Internet, pero no instalar Android Studio.

1. Inicia sesión en https://github.com y crea un repositorio **privado** llamado `zero-android`.
2. Descomprime este ZIP. Sube **el contenido dentro de** la carpeta `ZeroAssistant` a la raíz del repositorio (incluida la carpeta oculta `.github`). Desde la web de GitHub, puedes elegir `Add file > Upload files`; si tu navegador no acepta carpetas, utiliza GitHub Desktop en computadora o subir los archivos individualmente.
3. Abre `Actions > Compilar APK ZERO > Run workflow`.
4. Cuando finalice con éxito, abre esa ejecución y descarga el artefacto `ZERO-APK-debug`.
5. Extrae el archivo `app-debug.apk` del artefacto descargado, transfiérelo al Samsung e instálalo autorizando, cuando Android lo solicite, la instalación desde esa fuente.

Importante: un APK `debug` es para pruebas personales, no para publicación. La compilación todavía no fue ejecutada ni validada en un Samsung real. Si GitHub muestra errores, comparte el registro y se corregirán. El reconocimiento de voz depende de los servicios de reconocimiento disponibles en el teléfono; la detección manos libres está condicionada por Android.
