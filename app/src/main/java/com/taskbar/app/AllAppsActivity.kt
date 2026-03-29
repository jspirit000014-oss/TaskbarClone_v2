package com.taskbar.app

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.taskbar.app.service.AppsAdapter
import com.taskbar.app.utils.AppInfo
import com.taskbar.app.utils.AppManager
import com.taskbar.app.utils.FreeformLauncher
import com.taskbar.app.utils.PreferenceManager
import com.taskbar.app.R

/**
 * Ventana de todas las aplicaciones. Se abre al pulsar el botón de cuadraditos.
 * Toque = abrir en ventana flotante. Mantener pulsado = menú contextual.
 */
class AllAppsActivity : AppCompatActivity() {

    companion object {
        const val ACTION_ALL_APPS_SHOWN = "com.taskbar.app.ALL_APPS_SHOWN"
    }

    private lateinit var appManager: AppManager
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var appsRecyclerView: RecyclerView
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_menu_container)

        appManager = AppManager(this)
        preferenceManager = PreferenceManager.getInstance(this)
        appsRecyclerView = findViewById(R.id.allAppsRecyclerView)
        allApps = appManager.getAllInstalledApps(includeSystemApps = false)

        findViewById<android.view.View>(R.id.menuBackdrop).setOnClickListener { finish() }
        findViewById<android.widget.ImageButton>(R.id.closeMenuButton).setOnClickListener { finish() }

        val isGridMode = preferenceManager.startView != "Lista"
        val layoutManager = when (preferenceManager.startView) {
            "Cuadrícula" -> GridLayoutManager(this, 4)
            "Lista" -> LinearLayoutManager(this)
            else -> GridLayoutManager(this, 3)
        }
        appsRecyclerView.layoutManager = layoutManager
        appsRecyclerView.adapter = AppsAdapter(
            allApps,
            onAppClick = { appInfo ->
                FreeformLauncher.launchInFreeform(this, appInfo.packageName)
                if (preferenceManager.hideAfterSelect) finish()
            },
            isGridMode = isGridMode,
            onAppLongClick = { appInfo -> showAppContextMenu(appInfo, fromPinned = false) }
        )

        findViewById<SearchView>(R.id.searchView).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                val q = newText ?: ""
                val filtered = if (q.isEmpty()) allApps else allApps.filter {
                    it.name.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true)
                }
                (appsRecyclerView.adapter as? AppsAdapter)?.updateApps(filtered)
                return true
            }
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            sendBroadcast(Intent(ACTION_ALL_APPS_SHOWN).setPackage(packageName))
        }
    }

    private fun showAppContextMenu(appInfo: AppInfo, fromPinned: Boolean) {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        options += "Nueva ventana..."
        actions += {
            FreeformLauncher.launchInFreeform(this, appInfo.packageName)
            if (preferenceManager.hideAfterSelect) finish()
        }
        if (fromPinned) {
            options += "Desanclar de aplicaciones..."
            actions += { preferenceManager.removePinnedApp(appInfo.packageName) }
        } else {
            options += "Anclar a aplicaciones..."
            actions += { preferenceManager.addPinnedApp(appInfo.packageName) }
            options += "Bloquear de recientes"
            actions += { preferenceManager.addBlockedApp(appInfo.packageName) }
        }
        options += "Información de la app"
        actions += {
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${appInfo.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (_: Exception) {}
        }
        options += "Desinstalar"
        actions += {
            try {
                startActivity(Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:${appInfo.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (_: Exception) {}
        }

        AlertDialog.Builder(this)
            .setTitle(appInfo.name)
            .setItems(options.toTypedArray()) { _, which -> actions.getOrNull(which)?.invoke() }
            .create()
            .show()
    }
}
