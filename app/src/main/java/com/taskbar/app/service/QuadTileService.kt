package com.taskbar.app.service

import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.taskbar.app.utils.FreeformLauncher
import com.taskbar.app.utils.PreferenceManager

@RequiresApi(Build.VERSION_CODES.N)
class QuadTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        val prefs = PreferenceManager.getInstance(applicationContext)
        val configured = listOf(
            prefs.quadApp1, prefs.quadApp2, prefs.quadApp3, prefs.quadApp4
        ).any { it.isNotEmpty() }

        tile.label = "4 Ventanas"
        tile.contentDescription = "Lanzar 4 apps en cuadrantes"
        tile.state = if (configured) Tile.STATE_INACTIVE else Tile.STATE_UNAVAILABLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (configured) "Listo" else "Sin configurar"
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val prefs = PreferenceManager.getInstance(applicationContext)
        val pkgs = listOf(
            prefs.quadApp1, prefs.quadApp2, prefs.quadApp3, prefs.quadApp4
        )

        if (pkgs.all { it.isEmpty() }) {
            // Sin configurar — actualizar tile a unavailable
            val tile = qsTile ?: return
            tile.state = Tile.STATE_UNAVAILABLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Sin configurar"
            }
            tile.updateTile()
            return
        }

        // Marcar tile como activo mientras lanza
        val tile = qsTile
        tile?.state = Tile.STATE_ACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile?.subtitle = "Lanzando..."
        }
        tile?.updateTile()

        // Colapsar panel de notificaciones y lanzar
        val appCtx = applicationContext
        FreeformLauncher.tryEnableFreeformSupport(appCtx)

        val bounds = FreeformLauncher.getScreenBounds(appCtx)
        val left0 = bounds.left
        val top0 = bounds.top
        val width = bounds.width()
        val height = bounds.height()
        val halfW = width / 2
        val halfH = height / 2

        fun getBoundsForSlot(slot: Int): Rect {
            val saved = prefs.getQuadBounds(slot)
            if (!saved.isNullOrBlank()) {
                val parts = saved.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (parts.size == 4) return Rect(parts[0], parts[1], parts[0] + parts[2], parts[1] + parts[3])
            }
            return when (slot) {
                1 -> Rect(left0, top0, left0 + halfW, top0 + halfH)
                2 -> Rect(left0 + halfW, top0, left0 + width, top0 + halfH)
                3 -> Rect(left0, top0 + halfH, left0 + halfW, top0 + height)
                else -> Rect(left0 + halfW, top0 + halfH, left0 + width, top0 + height)
            }
        }

        val rects = (1..4).map { getBoundsForSlot(it) }

        val handler = Handler(Looper.getMainLooper())
        var delay = 300L // pequeño delay para que se cierre el panel primero

        pkgs.forEachIndexed { i, pkg ->
            if (pkg.isNotEmpty()) {
                val r = rects[i]
                handler.postDelayed({
                    FreeformLauncher.launchInFreeformWithBounds(
                        appCtx, pkg,
                        left = r.left, top = r.top,
                        width = r.width(), height = r.height()
                    )
                }, delay)
                delay += 600L
            }
        }

        // Reposicionar tras lanzar (intentos múltiples)
        listOf(delay + 500L, delay + 1200L, delay + 2000L).forEach { offset ->
            handler.postDelayed({
                FreeformLauncher.repositionQuadrantWindows(appCtx, pkgs, rects)
            }, offset)
        }

        // Volver tile a inactivo tras lanzar
        handler.postDelayed({
            val t = qsTile ?: return@postDelayed
            t.state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                t.subtitle = "Listo"
            }
            t.updateTile()
        }, delay + 2500L)
    }

    override fun onStopListening() {
        super.onStopListening()
    }
}
