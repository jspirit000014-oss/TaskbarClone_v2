package com.taskbar.app.utils

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.graphics.Insets
import android.view.WindowInsets
import android.view.WindowManager
import org.lsposed.hiddenapibypass.HiddenApiBypass

object FreeformLauncher {

    private const val WINDOWING_MODE_FREEFORM = 5

    private var reflectionAllowed = false

    private fun allowReflection() {
        if (reflectionAllowed) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                HiddenApiBypass.addHiddenApiExemptions("L")
            } catch (_: Throwable) { }
        }
        reflectionAllowed = true
    }

    fun launchInFreeform(context: Context, packageName: String) {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName) ?: return
        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )

        val screenBounds = getScreenBounds(context)
        val windowWidth = (screenBounds.width() * 0.75).toInt()
        val windowHeight = (screenBounds.height() * 0.7).toInt()
        val left = (screenBounds.width() - windowWidth) / 2
        val top = (screenBounds.height() - windowHeight) / 4
        val bounds = Rect(left, top, left + windowWidth, top + windowHeight)

        val options = ActivityOptions.makeBasic()
        allowReflection()
        try {
            val method = ActivityOptions::class.java.getDeclaredMethod(
                "setLaunchWindowingMode",
                Int::class.java
            )
            method.isAccessible = true
            method.invoke(options, WINDOWING_MODE_FREEFORM)
        } catch (_: Exception) { }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            options.launchBounds = bounds
        }
        try {
            context.startActivity(launchIntent, options.toBundle())
        } catch (e: Exception) {
            context.startActivity(launchIntent)
        }
    }

    /**
     * Lanza con tamaño y posición personalizados (ventanas libres).
     * CLEAR_TASK + setLaunchWindowingMode(FREEFORM) + launchBounds para sup.izq, sup.der, inf.izq, inf.der.
     */
    fun launchInFreeformWithBounds(
        context: Context,
        packageName: String,
        left: Int,
        top: Int,
        width: Int,
        height: Int
    ) {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName) ?: return

        // Android 13+ ignora launchBounds si la app tiene posición guardada.
        // FLAG_ACTIVITY_CLEAR_TASK + FLAG_ACTIVITY_NO_HISTORY fuerzan inicio limpio.
        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                Intent.FLAG_ACTIVITY_NO_HISTORY
        )

        val options = ActivityOptions.makeBasic()
        allowReflection()

        // Intentar setLaunchWindowingMode(FREEFORM) vía reflection
        try {
            val method = ActivityOptions::class.java.getDeclaredMethod(
                "setLaunchWindowingMode",
                Int::class.java
            )
            method.isAccessible = true
            method.invoke(options, WINDOWING_MODE_FREEFORM)
        } catch (_: Exception) { }

        // Intentar también setLaunchBounds vía reflection para Android 15
        val rect = Rect(left, top, left + width, top + height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            options.launchBounds = rect
        }
        try {
            val method = ActivityOptions::class.java.getDeclaredMethod(
                "setLaunchBounds",
                Rect::class.java
            )
            method.isAccessible = true
            method.invoke(options, rect)
        } catch (_: Exception) { }

        try {
            context.startActivity(launchIntent, options.toBundle())
        } catch (_: Exception) {
            context.startActivity(launchIntent)
        }
    }

    /**
     * Fuerza cierre de una app.
     * Con Shizuku: usa `am force-stop` real (funciona siempre).
     * Sin Shizuku: intenta reflection (casi nunca funciona en Android 13+).
     */
    fun forceStopApp(context: Context, packageName: String) {
        // Shizuku: funciona de verdad
        if (ShizukuHelper.hasPermission()) {
            ShizukuHelper.forceStop(packageName)
            return
        }
        // Fallback sin Shizuku
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val method = android.app.ActivityManager::class.java.getMethod("forceStopPackage", String::class.java)
            method.isAccessible = true
            method.invoke(am, packageName)
        } catch (_: Exception) { }
    }

    fun tryEnableFreeformSupport(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Settings.Global.putInt(
                    context.contentResolver,
                    "enable_freeform_support",
                    1
                )
            }
            try {
                Settings.Global.putInt(
                    context.contentResolver,
                    "force_resizable_activities",
                    1
                )
            } catch (_: Throwable) { }
        } catch (_: Throwable) { }
    }

    /**
     * Reposiciona las 4 ventanas en cuadrantes.
     * Usa Shizuku si está disponible (funciona en Android 15).
     * Fallback a reflection si no hay Shizuku.
     */
    fun repositionQuadrantWindows(context: Context, packages: List<String>, rects: List<Rect>) {
        if (packages.size != 4 || rects.size != 4) return

        // Intentar con Shizuku primero (funciona en Android 13+)
        if (ShizukuHelper.hasPermission()) {
            ShizukuHelper.repositionQuad(packages, rects)
            return
        }

        // Fallback: reflection (solo funciona en Android 12 o menos)
        allowReflection()
        try {
            val atmClass = Class.forName("android.app.ActivityTaskManager")
            val getInstance = atmClass.getMethod("getInstance")
            val atm = getInstance.invoke(null) ?: return
            val userHandleClass = Class.forName("android.os.UserHandle")
            val userId = (userHandleClass.getMethod("myUserId").invoke(null) as? Number)?.toInt() ?: 0
            val getRecentTasks = atmClass.getMethod(
                "getRecentTasks",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            @Suppress("UNCHECKED_CAST")
            val recentList = getRecentTasks.invoke(atm, 30, 1, userId) as? List<Any> ?: return
            val found = mutableSetOf<Int>()
            val taskIdForIndex = IntArray(4) { -1 }
            for (taskObj in recentList) {
                if (found.size == 4) break
                val taskId = taskObj.javaClass.getMethod("getTaskId").invoke(taskObj) as? Int ?: continue
                val topActivity = taskObj.javaClass.getMethod("getTopActivity").invoke(taskObj)
                    ?: taskObj.javaClass.getMethod("getBaseActivity").invoke(taskObj) ?: continue
                val pkg = topActivity.javaClass.getMethod("getPackageName").invoke(topActivity) as? String ?: continue
                val index = (0..3).firstOrNull { i -> packages[i] == pkg && i !in found } ?: continue
                found.add(index)
                taskIdForIndex[index] = taskId
            }
            if (found.isEmpty()) return
            val resizeTaskMethod = atmClass.getMethod("resizeTask", Int::class.javaPrimitiveType, Rect::class.java)
            for (i in 0..3) {
                if (taskIdForIndex[i] >= 0) {
                    try {
                        resizeTaskMethod.invoke(atm, taskIdForIndex[i], rects[i])
                    } catch (_: Throwable) { }
                }
            }
        } catch (_: Throwable) { }
    }

    fun clearSavedLaunchParams(context: Context, packageNames: List<String>) {
        if (packageNames.isEmpty()) return
        allowReflection()
        try {
            val atmClass = Class.forName("android.app.ActivityTaskManager")
            val getInstance = atmClass.getMethod("getInstance")
            val atm = getInstance.invoke(null) ?: return
            val clearMethod = atmClass.getMethod(
                "clearLaunchParamsForPackages",
                List::class.java
            )
            clearMethod.invoke(atm, packageNames.filter { it.isNotEmpty() })
        } catch (_: Throwable) { }
    }

    /**
     * Devuelve los bounds reales de la pantalla completa (píxeles).
     * Usa getMaximumWindowMetrics() para el display completo, no la ventana actual
     * (evita que desde un overlay se use un tamaño pequeño y las apps no se posicionen en cuadrantes).
     */
    fun getScreenBounds(context: Context): Rect {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = wm.maximumWindowMetrics
            val bounds = metrics.bounds
            Rect(0, 0, bounds.width(), bounds.height())
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            Rect(0, 0, metrics.widthPixels, metrics.heightPixels)
        }
    }

    /**
     * Devuelve el área útil de la pantalla (restando barra de estado y navegación)
     * para que los 4 cuadrantes queden bien dimensionados y visibles.
     */
    fun getScreenBoundsUsable(context: Context): Rect {
        val full = getScreenBounds(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = wm.maximumWindowMetrics
            val insets = metrics.windowInsets.getInsets(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
            return Rect(
                full.left + insets.left,
                full.top + insets.top,
                full.right - insets.right,
                full.bottom - insets.bottom
            )
        }
        return full
    }
}

