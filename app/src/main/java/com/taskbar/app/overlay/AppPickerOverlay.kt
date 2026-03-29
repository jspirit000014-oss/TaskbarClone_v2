package com.taskbar.app.overlay

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taskbar.app.R
import com.taskbar.app.utils.FreeformLauncher
import com.taskbar.app.utils.PreferenceManager

class AppPickerOverlay(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var isShowing = false
    private val preferenceManager = PreferenceManager.getInstance(context)

    data class AppInfo(
        val packageName: String,
        val label: String,
        val icon: android.graphics.drawable.Drawable
    )

    fun toggle() {
        if (isShowing) hide() else show()
    }

    fun show() {
        if (isShowing) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            return
        }

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.overlay_app_picker, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.y = 60 // px sobre la taskbar

        val recycler = view.findViewById<RecyclerView>(R.id.rvApps)
        val searchBar = view.findViewById<EditText>(R.id.etSearch)
        val btnClose = view.findViewById<View>(R.id.btnCloseOverlay)

        // Cargar apps instaladas
        val apps = loadInstalledApps()
        val adapter = OverlayAppAdapter(apps,
            onAppClick = { packageName ->
                hide()
                FreeformLauncher.launchInFreeform(context, packageName)
            },
            onAppLongClick = { appInfo ->
                showAppContextMenu(appInfo)
            }
        )

        recycler.layoutManager = GridLayoutManager(context, 4)
        recycler.adapter = adapter

        // Búsqueda en tiempo real
        searchBar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        btnClose.setOnClickListener { hide() }

        // Clic fuera cierra el overlay
        view.setOnClickListener { hide() }
        view.findViewById<View>(R.id.cardOverlay)?.setOnClickListener { /* no cerrar */ }

        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        overlayView = view
        isShowing = true
    }

    fun hide() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
            }
        }
        overlayView = null
        isShowing = false
    }

    private fun loadInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
        intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)

        return pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map {
                AppInfo(
                    packageName = it.activityInfo.packageName,
                    label = it.loadLabel(pm).toString(),
                    icon = it.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    // ── Menú contextual de apps (como en Taskbar) ───────────────────────────
    private fun showAppContextMenu(appInfo: AppInfo) {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        // Siempre disponible
        options += "Nueva ventana..."
        actions += {
            hide()
            FreeformLauncher.launchInFreeform(context, appInfo.packageName)
        }

        val isPinned = preferenceManager.getPinnedApps().contains(appInfo.packageName)

        if (isPinned) {
            options += "Desanclar de aplicaci.."
            actions += {
                preferenceManager.removePinnedApp(appInfo.packageName)
            }
        } else {
            options += "Anclar a aplicaciones.."
            actions += {
                preferenceManager.addPinnedApp(appInfo.packageName)
            }

            options += "Bloquear de recientes"
            actions += {
                preferenceManager.addBlockedApp(appInfo.packageName)
            }
        }

        options += "Información de la apl.."
        actions += {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${appInfo.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }

        options += "Desinstalar"
        actions += {
            try {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:${appInfo.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(appInfo.label)
            .setItems(options.toTypedArray()) { _, which ->
                actions.getOrNull(which)?.invoke()
            }
            .create()

        dialog.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                @Suppress("DEPRECATION")
                window.setType(WindowManager.LayoutParams.TYPE_PHONE)
            }
        }

        dialog.show()
    }

    // ── Adapter inline ──────────────────────────────────────────────────────
    inner class OverlayAppAdapter(
        private val allApps: List<AppInfo>,
        private val onAppClick: (String) -> Unit,
        private val onAppLongClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<OverlayAppAdapter.VH>() {

        private var filtered = allApps.toMutableList()

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.ivAppIcon)
            val name: TextView = view.findViewById(R.id.tvAppName)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_overlay_app, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = filtered[position]
            holder.icon.setImageDrawable(app.icon)
            holder.name.text = app.label
            holder.itemView.setOnClickListener { onAppClick(app.packageName) }
            holder.itemView.setOnLongClickListener {
                onAppLongClick(app)
                true
            }
        }

        override fun getItemCount() = filtered.size

        fun filter(query: String) {
            filtered = if (query.isEmpty()) allApps.toMutableList()
            else allApps.filter {
                it.label.contains(query, ignoreCase = true)
            }.toMutableList()
            notifyDataSetChanged()
        }
    }
}
