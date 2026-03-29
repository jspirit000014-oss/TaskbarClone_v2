package com.taskbar.app.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.taskbar.app.AllAppsActivity
import com.taskbar.app.MainActivity
import com.taskbar.app.R
import com.taskbar.app.overlay.AppPickerOverlay
import com.taskbar.app.utils.AppInfo
import com.taskbar.app.utils.AppManager
import com.taskbar.app.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class TaskbarService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var taskbarView: View
    private lateinit var startMenuView: View
    private var taskbarParams: WindowManager.LayoutParams? = null
    private var startMenuParams: WindowManager.LayoutParams? = null
    private var isStartMenuVisible = false
    private lateinit var appPickerOverlay: AppPickerOverlay
    private lateinit var appManager: AppManager
    private lateinit var preferenceManager: PreferenceManager
    private val handler = Handler(Looper.getMainLooper())
    private var clockRunnable: Runnable? = null
    private var isCollapsed = false
    private var currentPosition: String = ""
    private var allAppsShownReceiver: BroadcastReceiver? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "taskbar_service_channel"
    }
    
    override fun onCreate() {
        super.onCreate()

        // Seguridad extra: no intentar dibujar si no tenemos permiso de superposición.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        appManager = AppManager(this)
        preferenceManager = PreferenceManager.getInstance(this)
        appPickerOverlay = AppPickerOverlay(this)
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        createTaskbar()
        // Preparar el menú inicio como overlay (no pantalla completa)
        createStartMenu()
        // Actualizar reloj después de crear la taskbar
        handler.postDelayed({ updateClock() }, 100)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Taskbar Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Servicio de la barra de tareas"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Taskbar")
            .setContentText("Barra de tareas activa")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun createTaskbar() {
        try {
            val inflater = LayoutInflater.from(this)
            taskbarView = inflater.inflate(R.layout.taskbar_overlay, null)

            val pinnedRecycler = taskbarView.findViewById<RecyclerView>(R.id.pinnedAppsRecyclerView)
            val systemTray = taskbarView.findViewById<LinearLayout>(R.id.systemTray)
            val startButton = taskbarView.findViewById<ImageButton>(R.id.startButton)

            // Flecha de colapsar/expandir: cuando está colapsado solo se ve la flecha.
            val collapseButton = taskbarView.findViewById<ImageButton>(R.id.btnCollapse)
            updateCollapseIcon(collapseButton)
            collapseButton.setOnClickListener {
                isCollapsed = !isCollapsed
                val visibility = if (isCollapsed) View.GONE else View.VISIBLE
                startButton.visibility = visibility
                pinnedRecycler.visibility = visibility
                systemTray.visibility = visibility
                updateCollapseIcon(collapseButton)
            }

            // Botón de cuadraditos: al pulsar abre/cierra el menú de inicio
            // en forma de overlay superpuesto (no actividad a pantalla completa).
            startButton.setOnClickListener {
                toggleStartMenu()
            }
            
            // Apps ancladas
            setupPinnedApps()
            
            // Posicionar taskbar según preferencias
            val position = preferenceManager.position
            currentPosition = position
            taskbarParams = WindowManager.LayoutParams(
                when {
                    position.contains("inferior") || position.contains("Parte inferior") -> {
                        WindowManager.LayoutParams.MATCH_PARENT
                    }
                    else -> WindowManager.LayoutParams.WRAP_CONTENT
                },
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = when {
                    position.contains("Derecha") -> Gravity.END or Gravity.TOP
                    position.contains("Izquierda") -> Gravity.START or Gravity.TOP
                    position.contains("inferior") || position.contains("Parte inferior") -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    else -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
                }
                x = 0
                y = if (position.contains("inferior") || position.contains("Parte inferior")) 0 else 0
            }
            
            // Asegurarse de que no esté ya agregada
            try {
                windowManager.removeView(taskbarView)
            } catch (e: Exception) {
                // Ignorar si no está agregada
            }
            
            windowManager.addView(taskbarView, taskbarParams)
        } catch (e: Exception) {
            android.util.Log.e("TaskbarService", "Error al crear taskbar: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupPinnedApps() {
        val recyclerView = taskbarView.findViewById<RecyclerView>(R.id.pinnedAppsRecyclerView)
        val pinnedApps = preferenceManager.getPinnedApps()
        
        if (pinnedApps.isEmpty()) {
            recyclerView.visibility = View.GONE
            return
        }
        
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val apps = pinnedApps.mapNotNull { packageName ->
            appManager.getAppInfo(packageName)
        }

        recyclerView.adapter = PinnedAppsAdapter(
            apps,
            onAppClick = { appInfo ->
                // Desde la barra principal: siempre intentar abrir en ventana flotante.
                launchApp(appInfo, freeform = true)
            },
            onAppLongClick = { appInfo ->
                showAppContextMenu(appInfo, fromPinned = true)
            }
        )
    }
    
    private fun createStartMenu() {
        // Usar un contexto con el tema de la app para inflar vistas AppCompat/Material
        val themedContext = android.view.ContextThemeWrapper(this, R.style.Theme_Taskbar)
        val inflater = LayoutInflater.from(themedContext)
        startMenuView = inflater.inflate(R.layout.start_menu_container, null)

        // Fondo oscuro: tocar cierra el menú
        startMenuView.findViewById<View>(R.id.menuBackdrop).setOnClickListener {
            hideStartMenu()
        }

        // Botón cerrar
        startMenuView.findViewById<ImageButton>(R.id.closeMenuButton).setOnClickListener {
            hideStartMenu()
        }

        // Lista de apps
        val appsRecyclerView = startMenuView.findViewById<RecyclerView>(R.id.allAppsRecyclerView)
        val apps = appManager.getAllInstalledApps(includeSystemApps = false)
        
        val isGridMode = preferenceManager.startView != "Lista"
        val layoutManager = when (preferenceManager.startView) {
            "Cuadrícula" -> GridLayoutManager(themedContext, 4)
            "Lista" -> LinearLayoutManager(themedContext)
            else -> GridLayoutManager(themedContext, 3)
        }
        appsRecyclerView.layoutManager = layoutManager
        
        val adapter = AppsAdapter(
            apps,
            onAppClick = { appInfo ->
                // Desde el menú inicio: abrir en ventana flotante.
                launchApp(appInfo, freeform = true)
                if (preferenceManager.hideAfterSelect) {
                    hideStartMenu()
                }
            },
            isGridMode = isGridMode,
            onAppLongClick = { appInfo ->
                showAppContextMenu(appInfo, fromPinned = false)
            }
        )
        appsRecyclerView.adapter = adapter
        
        // Acceso rápido (pinned apps en el menú)
        val pinnedMenuRecycler = startMenuView.findViewById<RecyclerView>(R.id.pinnedAppsMenuRecyclerView)
        val pinnedApps = preferenceManager.getPinnedApps().mapNotNull { appManager.getAppInfo(it) }
        if (pinnedApps.isEmpty()) {
            startMenuView.findViewById<android.view.View>(R.id.quickAccessSection).visibility = android.view.View.GONE
        } else {
            pinnedMenuRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            pinnedMenuRecycler.adapter = PinnedAppsAdapter(
                pinnedApps,
                onAppClick = { launchApp(it, freeform = true) },
                onAppLongClick = { showAppContextMenu(it, fromPinned = true) }
            )
        }

        // Búsqueda
        val searchView = startMenuView.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterApps(query ?: "", apps, appsRecyclerView)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText ?: "", apps, appsRecyclerView)
                return true
            }
        })
        
        startMenuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            x = 0
            y = 0
        }
    }
    
    private fun filterApps(query: String, allApps: List<AppInfo>, recyclerView: RecyclerView) {
        val filtered = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { 
                it.name.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }
        (recyclerView.adapter as? AppsAdapter)?.updateApps(filtered)
    }
    
    private fun toggleStartMenu() {
        if (isStartMenuVisible) {
            hideStartMenu()
        } else {
            showStartMenu()
        }
    }
    
    private fun showStartMenu() {
        if (!isStartMenuVisible && ::startMenuView.isInitialized && startMenuParams != null) {
            try {
                // Asegurarse de que el menú no esté ya agregado
                try {
                    windowManager.removeView(startMenuView)
                } catch (e: Exception) {
                    // Ignorar si no está agregado
                }
                windowManager.addView(startMenuView, startMenuParams)
                isStartMenuVisible = true
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("TaskbarService", "Error al mostrar menú inicio: ${e.message}")
            }
        }
    }
    
    private fun hideStartMenu() {
        if (isStartMenuVisible && ::startMenuView.isInitialized) {
            try {
                windowManager.removeView(startMenuView)
                isStartMenuVisible = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun launchApp(appInfo: AppInfo, freeform: Boolean) {
        appManager.launchApp(appInfo.packageName, freeform)
    }

    private fun updateCollapseIcon(button: ImageButton) {
        // La flecha siempre indica hacia dónde se va a mover la barra
        // cuando pulses el botón (dirección del colapso/expansión).
        val isRight = currentPosition.contains("Derecha", ignoreCase = true)
        val isLeft = currentPosition.contains("Izquierda", ignoreCase = true)
        val isBottom = currentPosition.contains("inferior", ignoreCase = true)
        val isTop = !isRight && !isLeft && !isBottom

        val resId = when {
            // Barras verticales en los laterales: flecha horizontal
            isRight && !isCollapsed -> R.drawable.arrow_right_float   // colapsar hacia el borde derecho
            isRight && isCollapsed -> R.drawable.arrow_left_float    // expandir hacia el interior
            isLeft && !isCollapsed -> R.drawable.arrow_left_float    // colapsar hacia el borde izquierdo
            isLeft && isCollapsed -> R.drawable.arrow_right_float    // expandir hacia el interior

            // Barras horizontales
            isBottom && !isCollapsed -> R.drawable.arrow_down_float  // colapsar hacia abajo
            isBottom && isCollapsed -> R.drawable.arrow_up_float     // expandir hacia arriba
            isTop && !isCollapsed -> R.drawable.arrow_up_float       // colapsar hacia arriba
            isTop && isCollapsed -> R.drawable.arrow_down_float      // expandir hacia abajo

            else -> if (isCollapsed) R.drawable.arrow_down_float else R.drawable.arrow_up_float
        }
        button.setImageResource(resId)
    }

    private fun showAppContextMenu(appInfo: AppInfo, fromPinned: Boolean) {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        options += "Nueva ventana..."
        actions += { launchApp(appInfo, freeform = true) }

        if (fromPinned) {
            options += "Desanclar de aplicaciones..."
            actions += {
                preferenceManager.removePinnedApp(appInfo.packageName)
                setupPinnedApps()
            }
        } else {
            options += "Anclar a aplicaciones..."
            actions += {
                preferenceManager.addPinnedApp(appInfo.packageName)
                setupPinnedApps()
            }

            // Solo para apps que NO están ancladas, como en Taskbar original.
            options += "Bloquear de recientes"
            actions += {
                preferenceManager.addBlockedApp(appInfo.packageName)
            }
        }

        options += "Información de la app"
        actions += { openAppInfo(appInfo.packageName) }

        options += "Desinstalar"
        actions += { uninstallApp(appInfo.packageName) }

        val dialog = AlertDialog.Builder(this)
            .setTitle(appInfo.name)
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

    private fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateClock() {
        if (!::taskbarView.isInitialized) return
        
        val clockView = taskbarView.findViewById<TextView>(R.id.clockTextView) ?: return
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        clockRunnable = object : Runnable {
            override fun run() {
                try {
                    if (::taskbarView.isInitialized) {
                        val view = taskbarView.findViewById<TextView>(R.id.clockTextView)
                        view?.text = sdf.format(Date())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(clockRunnable!!)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        clockRunnable?.let { handler.removeCallbacks(it) }
        try {
            allAppsShownReceiver?.let { unregisterReceiver(it) }
        } catch (_: Exception) {}
        try {
            if (::taskbarView.isInitialized) {
                windowManager.removeView(taskbarView)
            }
            if (::appPickerOverlay.isInitialized) {
                appPickerOverlay.hide()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Adapter para apps ancladas
class PinnedAppsAdapter(
    private var apps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<PinnedAppsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pinned_app, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.itemView.setOnClickListener { onAppClick(app) }
        holder.itemView.setOnLongClickListener {
            onAppLongClick(app)
            true
        }
    }
    
    override fun getItemCount() = apps.size
}

// Adapter para lista de apps
class AppsAdapter(
    private var apps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val isGridMode: Boolean = false,
    private val onAppLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (isGridMode) R.layout.item_app_grid else R.layout.item_app
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.name
        holder.itemView.setOnClickListener { onAppClick(app) }
        holder.itemView.setOnLongClickListener {
            onAppLongClick(app)
            true
        }
    }
    
    override fun getItemCount() = apps.size
    
    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }
}

