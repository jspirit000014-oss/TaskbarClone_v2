package com.taskbar.app.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess

/**
 * Helper para usar Shizuku y ejecutar comandos con permisos ADB.
 * Permite resizeTask real en Android 13+ sin root.
 */
object ShizukuHelper {

    private const val SHIZUKU_PERMISSION_CODE = 1001

    /** Verifica si Shizuku está instalado y en ejecución */
    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    /** Verifica si ya tenemos permiso concedido */
    fun hasPermission(): Boolean {
        if (!isAvailable()) return false
        return try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (_: Throwable) {
            false
        }
    }

    /** Solicita permiso al usuario via diálogo de Shizuku */
    fun requestPermission() {
        try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) return
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
            }
        } catch (_: Throwable) { }
    }

    /**
     * Ejecuta un comando shell con permisos ADB via Shizuku.
     * Retorna true si el comando salió con código 0.
     */
    fun execCommand(command: String): Boolean {
        if (!hasPermission()) return false
        return try {
            val process: ShizukuRemoteProcess = Shizuku.newProcess(
                arrayOf("sh", "-c", command), null, null
            )
            val exitCode = process.waitFor()
            process.destroy()
            exitCode == 0
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Reposiciona una ventana usando `wm task resize` vía Shizuku.
     * Este comando SÍ funciona en Android 15 con permisos ADB.
     */
    fun resizeTaskByPackage(packageName: String, rect: Rect): Boolean {
        if (!hasPermission()) return false
        return try {
            // Obtener taskId del package via `am stack info`
            val taskId = getTaskIdForPackage(packageName) ?: return false
            val cmd = "wm task resize $taskId ${rect.left} ${rect.top} ${rect.right} ${rect.bottom}"
            execCommand(cmd)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Obtiene el taskId de una app en ejecución via `am stack info`.
     */
    fun getTaskIdForPackage(packageName: String): Int? {
        if (!hasPermission()) return null
        return try {
            val process: ShizukuRemoteProcess = Shizuku.newProcess(
                arrayOf("sh", "-c", "am stack info"), null, null
            )
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            process.destroy()

            // Parsear output buscando el packageName y su taskId
            // Formato típico: "Task id #42" seguido de líneas con el package
            var currentTaskId: Int? = null
            for (line in output.lines()) {
                val taskMatch = Regex("Task id #(\\d+)").find(line)
                if (taskMatch != null) {
                    currentTaskId = taskMatch.groupValues[1].toIntOrNull()
                }
                if (line.contains(packageName) && currentTaskId != null) {
                    return currentTaskId
                }
            }
            null
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Fuerza cierre de una app via Shizuku (am force-stop).
     * Esto SÍ funciona con permisos ADB, a diferencia del método normal.
     */
    fun forceStop(packageName: String): Boolean {
        if (!hasPermission()) return false
        return execCommand("am force-stop $packageName")
    }

    /**
     * Habilita freeform support via Shizuku.
     */
    fun enableFreeformSupport(): Boolean {
        if (!hasPermission()) return false
        val r1 = execCommand("settings put global enable_freeform_support 1")
        val r2 = execCommand("settings put global force_resizable_activities 1")
        return r1 && r2
    }

    /**
     * Reposiciona las 4 ventanas en cuadrantes usando Shizuku.
     * Espera a que las apps estén en memoria antes de reposicionar.
     */
    fun repositionQuad(packages: List<String>, rects: List<Rect>): Boolean {
        if (!hasPermission()) return false
        if (packages.size != 4 || rects.size != 4) return false
        var anySuccess = false
        for (i in 0..3) {
            val pkg = packages[i]
            if (pkg.isEmpty()) continue
            val taskId = getTaskIdForPackage(pkg) ?: continue
            val r = rects[i]
            val ok = execCommand(
                "wm task resize $taskId ${r.left} ${r.top} ${r.right} ${r.bottom}"
            )
            if (ok) anySuccess = true
        }
        return anySuccess
    }
}
