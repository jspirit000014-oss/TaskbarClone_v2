# 🚀 Guía de Inicio Rápido - Taskbar Clone

## ¿Qué incluye este proyecto?

Una aplicación completa de Android que replica las funcionalidades principales de **Taskbar 6.2.2**:

✅ Barra de tareas flotante personalizable
✅ Menú de inicio con todas las apps
✅ Sistema de apps ancladas
✅ Configuración de posición y tamaño
✅ Inicio automático al encender el dispositivo
✅ Servicio persistente en segundo plano

## Inicio Rápido (5 minutos)

### 1. Descomprime el proyecto
```bash
unzip TaskbarClone.zip
cd TaskbarClone
```

### 2. Abre en Android Studio
1. Abre Android Studio
2. File → Open
3. Selecciona la carpeta `TaskbarClone`
4. Espera a que Gradle sincronice (puede tardar 2-5 minutos)

### 3. Conecta tu dispositivo Android
- Habilita "Opciones de desarrollador" en tu Android
- Activa "Depuración USB"
- Conecta el cable USB

### 4. Ejecuta la app
- Haz clic en el botón Run ▶️ (o Shift+F10)
- Selecciona tu dispositivo
- ¡Listo!

## Estructura del Proyecto

```
TaskbarClone/
│
├── app/
│   ├── src/main/
│   │   ├── java/com/taskbar/clone/
│   │   │   ├── adapter/          # Adaptadores para listas
│   │   │   ├── model/            # Modelos de datos
│   │   │   ├── receiver/         # Receptores de eventos
│   │   │   ├── service/          # Servicio de la barra
│   │   │   ├── ui/               # Actividades (pantallas)
│   │   │   ├── utils/            # Utilidades
│   │   │   └── TaskbarApplication.kt
│   │   │
│   │   ├── res/
│   │   │   ├── layout/           # Diseños XML
│   │   │   ├── drawable/         # Íconos y gráficos
│   │   │   └── values/           # Colores, strings, temas
│   │   │
│   │   └── AndroidManifest.xml   # Configuración de la app
│   │
│   └── build.gradle              # Dependencias del módulo
│
├── build.gradle                  # Configuración del proyecto
├── README.md                     # Documentación principal
├── BUILD_GUIDE.md               # Guía de compilación
└── ADVANCED_FEATURES.md         # Características avanzadas

```

## Archivos Principales

### 1. `TaskbarService.kt` - El Corazón de la App
Maneja:
- La ventana flotante de la barra
- El menú de inicio
- La lista de apps
- Los eventos de clic

### 2. `MainActivity.kt` - Pantalla Principal
- Activar/desactivar la barra
- Solicitar permisos
- Acceso a configuración

### 3. `SettingsActivity.kt` - Configuración
- Posición de la barra
- Tamaño
- Inicio automático

## Personalización Rápida

### Cambiar el color de la barra

Edita `app/src/main/res/values/colors.xml`:
```xml
<color name="taskbar_background">#E0000000</color>  <!-- Negro semi-transparente -->
<color name="primary">#2196F3</color>                <!-- Azul -->
```

### Cambiar el tamaño predeterminado

Edita `app/src/main/java/com/taskbar/clone/model/Models.kt`:
```kotlin
companion object {
    const val SMALL = 48
    const val MEDIUM = 64   // ← Cambia este valor
    const val LARGE = 80
}
```

### Agregar apps pre-ancladas

Edita `PreferenceManager.kt` y modifica:
```kotlin
fun getPinnedApps(): List<String> {
    val pinnedStr = prefs.getString(KEY_PINNED_APPS, 
        "com.android.chrome,com.whatsapp,com.spotify.music") ?: ""
    // ← Agrega paquetes de apps aquí
    ...
}
```

## Permisos Necesarios

### 1. Overlay Permission (Obligatorio)
Permite mostrar la barra sobre otras apps.
```kotlin
Settings.ACTION_MANAGE_OVERLAY_PERMISSION
```

### 2. Usage Stats (Opcional)
Para ver apps recientes.
```kotlin
Settings.ACTION_USAGE_ACCESS_SETTINGS
```

### 3. Boot Completed (Opcional)
Para inicio automático.
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## Compilar APK

### Debug (para pruebas)
```bash
./gradlew assembleDebug
# APK en: app/build/outputs/apk/debug/app-debug.apk
```

### Release (para distribución)
```bash
./gradlew assembleRelease
# APK en: app/build/outputs/apk/release/app-release.apk
```

## Solución de Problemas Comunes

### ❌ "SDK not found"
**Solución**: Instala Android SDK desde Android Studio
```
Tools → SDK Manager → SDK Platforms
```

### ❌ "Gradle sync failed"
**Solución**: 
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### ❌ La barra no aparece
**Solución**:
1. Verifica que el permiso overlay esté concedido
2. Comprueba que el switch esté activado
3. Reinicia la app

### ❌ El servicio se detiene solo
**Solución**: En configuración del dispositivo:
- Deshabilita optimización de batería para esta app
- Permite ejecución en segundo plano

## Próximos Pasos

### Agregar más funcionalidades

Revisa `ADVANCED_FEATURES.md` para implementar:
- Apps recientes con UsageStatsManager
- Ventanas flotantes (freeform mode)
- Modo escritorio completo
- Temas personalizables
- Widgets en la barra

### Mejorar el diseño

1. Íconos personalizados en `res/drawable/`
2. Animaciones en los layouts
3. Efectos de desenfoque (blur)
4. Transiciones suaves

### Optimizar rendimiento

1. Lazy loading de apps
2. Caché de íconos
3. Background processing con WorkManager
4. Reducir uso de batería

## Recursos Útiles

### Documentación
- [Android Developers](https://developer.android.com)
- [Kotlin Docs](https://kotlinlang.org/docs)
- [Material Design](https://m3.material.io)

### Librerías Útiles
- [Coil](https://coil-kt.github.io/coil/) - Carga de imágenes
- [Lottie](https://airbnb.io/lottie/) - Animaciones
- [Timber](https://github.com/JakeWharton/timber) - Logging

### Comunidad
- [Stack Overflow](https://stackoverflow.com/questions/tagged/android)
- [Reddit r/androiddev](https://reddit.com/r/androiddev)
- [Android Weekly](https://androidweekly.net)

## Versiones y Actualizaciones

Para actualizar la versión:

1. Edita `app/build.gradle`:
```gradle
versionCode 2         // Incrementa este número
versionName "1.1.0"   // Actualiza la versión
```

2. Actualiza el CHANGELOG:
```markdown
## [1.1.0] - 2026-02-08
### Añadido
- Nueva funcionalidad X
- Mejora en Y

### Corregido
- Bug en Z
```

## Testing

### Tests unitarios
```bash
./gradlew test
```

### Tests de UI
```bash
./gradlew connectedAndroidTest
```

## Contribuir

Si quieres mejorar este proyecto:

1. Fork el repositorio
2. Crea una rama: `git checkout -b feature/nueva-funcionalidad`
3. Commit: `git commit -am 'Agrega nueva funcionalidad'`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Pull Request

## Licencia

MIT License - Puedes usar, modificar y distribuir libremente.

## Créditos

- Inspirado en **Taskbar** de Braden Farmer
- Desarrollado con ❤️ para la comunidad Android

---

## 🎯 Checklist de Desarrollo

- [ ] Proyecto compilado exitosamente
- [ ] App instalada en dispositivo
- [ ] Permisos concedidos
- [ ] Barra de tareas visible
- [ ] Menú de inicio funcional
- [ ] Apps se pueden abrir
- [ ] Configuración funciona
- [ ] Personalización aplicada
- [ ] Tests pasados
- [ ] APK release creado

---

¡Disfruta desarrollando! 🚀

**Versión del proyecto**: 1.0.0
**Fecha**: Febrero 2026
**Compatibilidad**: Android 7.0+ (API 24+)
