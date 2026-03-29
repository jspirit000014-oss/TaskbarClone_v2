package com.taskbar.app.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class TaskbarTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        val running = isTaskbarRunning()
        if (running) { stopTaskbar() } else { startTaskbar() }
        updateTile()
    }

    private fun isTaskbarRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == TaskbarService::class.java.name }
    }

    private fun startTaskbar() {
        val intent = Intent(applicationContext, TaskbarService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(intent)
        } else {
            applicationContext.startService(intent)
        }
    }

    private fun stopTaskbar() {
        val intent = Intent(applicationContext, TaskbarService::class.java)
        applicationContext.stopService(intent)
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val running = isTaskbarRunning()
        tile.label = "Taskbar XXX"
        tile.state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (running) "Activo" else "Inactivo"
        }
        tile.updateTile()
    }

    override fun onStopListening() { super.onStopListening() }
}