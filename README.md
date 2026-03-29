# Taskbar App - Android (Kotlin)

Proyecto Android completo convertido desde el código Kivy/Pydroid original.

## Estructura del proyecto

```
TaskbarApp/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml          ← Todos los permisos declarados
│       ├── java/com/taskbar/app/
│       │   ├── MainActivity.kt          ← Actividad principal
│       │   └── ui/
│       │       ├── BaseFragment.kt      ← Navegación base
│       │       ├── MainFragment.kt      ← Pantalla principal + permisos
│       │       ├── GeneralFragment.kt   ← Ajustes generales
│       │       ├── AppearanceFragment.kt ← Apariencia
│       │       ├── RecentFragment.kt    ← Últimas aplicaciones
│       │       ├── FreeWindowFragment.kt ← Modo ventana libre
│       │       ├── DesktopFragment.kt   ← Modo escritorio
│       │       └── AdvancedFragment.kt  ← Opciones avanzadas
│       └── res/
│           ├── layout/                  ← Todos los XML de pantallas
│           ├── values/                  ← Colores, strings, themes
│           ├── drawable/                ← Botones, fondos, divisores
│           └── anim/                    ← Animaciones de transición
└── build.gradle / settings.gradle
```

## Cómo abrir en Android Studio

1. Abre **Android Studio**
2. Selecciona **Open** → elige la carpeta del proyecto
3. Espera a que Gradle sincronice
4. Conecta tu dispositivo Android o usa un emulador
5. Pulsa **Run ▶**

## Requisitos

- Android Studio Hedgehog (2023) o superior
- SDK mínimo: Android 8.0 (API 26)
- SDK objetivo: Android 14 (API 34)
- Kotlin 1.9.x

## Funcionalidades implementadas

✅ Pantalla principal con menú de navegación
✅ Panel de permisos con botón "Conceder todos"
    - Superponer sobre otras apps (SYSTEM_ALERT_WINDOW)
    - Acceso a datos de uso (PACKAGE_USAGE_STATS)
    - Acceso a notificaciones
    - Accesibilidad
    - Administrador del dispositivo
✅ Ajustes generales (7 opciones + 7 switches)
✅ Apariencia (color picker, icon packs, 6 switches)
✅ Últimas aplicaciones (5 selectores + 7 switches)
✅ Modo ventana libre (4 switches + selector tamaño)
✅ Modo escritorio (switch, selectors, dialogs de ayuda)
✅ Opciones avanzadas (switches, SecondScreen, gestión de datos)
✅ Animaciones de deslizamiento entre pantallas
✅ Dialogs de selección única para todas las opciones
✅ Toast de confirmación en cada acción
✅ Diseño oscuro fiel al original Taskbar

## Notas de permisos

Algunos permisos especiales requieren que el usuario los active manualmente
en los Ajustes del sistema. Al pulsar "Conceder" se abre la pantalla
correspondiente del sistema Android.
