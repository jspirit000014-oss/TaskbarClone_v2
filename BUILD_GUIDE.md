# Guía de Compilación - Taskbar Clone

## Requisitos previos

1. **Java Development Kit (JDK) 17**
   - Descarga desde: https://www.oracle.com/java/technologies/downloads/
   - O usa OpenJDK 17

2. **Android Studio**
   - Versión recomendada: Iguana (2023.2.1) o posterior
   - Descarga desde: https://developer.android.com/studio

3. **Android SDK**
   - API Level 34 (Android 14)
   - Build Tools 34.0.0
   - Se instala automáticamente con Android Studio

## Configuración del entorno

### Opción 1: Con Android Studio (Recomendado)

1. **Abre el proyecto**
   ```
   File > Open > Selecciona la carpeta TaskbarClone
   ```

2. **Espera a que Gradle sincronice**
   - Android Studio descargará automáticamente las dependencias
   - Puede tardar varios minutos la primera vez

3. **Conecta un dispositivo o inicia el emulador**
   - Dispositivo físico: Habilita "Opciones de desarrollador" y "Depuración USB"
   - Emulador: Tools > Device Manager > Create Virtual Device

4. **Compila y ejecuta**
   - Haz clic en el botón Run (▶️) o presiona Shift+F10
   - Selecciona tu dispositivo
   - La app se instalará automáticamente

### Opción 2: Línea de comandos

1. **Navega al directorio del proyecto**
   ```bash
   cd TaskbarClone
   ```

2. **Compila el APK de depuración**
   ```bash
   # En Windows
   gradlew.bat assembleDebug
   
   # En Linux/Mac
   ./gradlew assembleDebug
   ```

3. **El APK estará en:**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Instala en un dispositivo conectado**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Compilar APK de release

### Preparación

1. **Crea un keystore para firmar la app**
   ```bash
   keytool -genkey -v -keystore taskbar-release.keystore -alias taskbar -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configura el archivo de firma** (no incluir en git)
   Crea `app/keystore.properties`:
   ```properties
   storePassword=TU_PASSWORD
   keyPassword=TU_PASSWORD
   keyAlias=taskbar
   storeFile=../taskbar-release.keystore
   ```

3. **Actualiza app/build.gradle** (ya configurado en el proyecto)

4. **Compila el release**
   ```bash
   ./gradlew assembleRelease
   ```

5. **El APK firmado estará en:**
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

## Solución de problemas

### Error: "SDK not found"
```bash
# Configura ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### Error: "Kotlin compiler not found"
- Asegúrate de que Android Studio esté actualizado
- Invalida cachés: File > Invalidate Caches > Invalidate and Restart

### Error de sincronización de Gradle
```bash
# Limpia el proyecto
./gradlew clean

# Re-sincroniza
./gradlew build --refresh-dependencies
```

### Problemas de compilación por dependencias
- Verifica tu conexión a internet
- Asegúrate de que Google y Maven Central estén accesibles
- Intenta con VPN si estás en una región con restricciones

## Optimizaciones para producción

### ProGuard/R8 (Ofuscación)

Descomenta en `app/build.gradle`:
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

### App Bundle (AAB) para Google Play

```bash
./gradlew bundleRelease
```

El AAB estará en: `app/build/outputs/bundle/release/app-release.aab`

## Versiones y actualización

Para actualizar la versión en `app/build.gradle`:

```gradle
defaultConfig {
    versionCode 2        // Incrementa para cada release
    versionName "1.1.0"  // Versión visible para usuarios
}
```

## Testing

### Ejecutar tests unitarios
```bash
./gradlew test
```

### Ejecutar tests instrumentados
```bash
./gradlew connectedAndroidTest
```

## Herramientas útiles

### Ver el log de la app
```bash
adb logcat | grep TaskbarClone
```

### Desinstalar la app
```bash
adb uninstall com.taskbar.clone
```

### Ver apps instaladas
```bash
adb shell pm list packages | grep taskbar
```

### Limpiar datos de la app
```bash
adb shell pm clear com.taskbar.clone
```

## Recursos adicionales

- [Documentación oficial de Android](https://developer.android.com/docs)
- [Guía de Kotlin](https://kotlinlang.org/docs/home.html)
- [Material Design 3](https://m3.material.io/)

---

¡Feliz desarrollo! 🚀
