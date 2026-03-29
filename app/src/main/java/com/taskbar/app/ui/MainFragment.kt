package com.taskbar.app.ui

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.taskbar.app.R
import com.taskbar.app.service.TaskbarService
import com.taskbar.app.utils.PreferenceManager

class MainFragment : BaseFragment() {

    private lateinit var permBanner: LinearLayout
    private lateinit var permBannerText: TextView
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())
        permBanner = view.findViewById(R.id.perm_banner)
        permBannerText = view.findViewById(R.id.perm_banner_text)

        // Toggle principal - cargar estado guardado
        val switchTaskbar = view.findViewById<Switch>(R.id.switch_taskbar_enabled)
        switchTaskbar.isChecked = preferenceManager.isTaskbarEnabled
        
        switchTaskbar.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.isTaskbarEnabled = isChecked
            if (isChecked) {
                // Verificar permisos antes de activar
                if (checkCriticalPermissions()) {
                    try {
                        startTaskbarService()
                        showToast("✅ Taskbar activado")
                    } catch (e: Exception) {
                        switchTaskbar.isChecked = false
                        preferenceManager.isTaskbarEnabled = false
                        showToast("❌ Error al iniciar la taskbar: ${e.message}")
                        e.printStackTrace()
                    }
                } else {
                    switchTaskbar.isChecked = false
                    preferenceManager.isTaskbarEnabled = false
                    showToast("⚠️ Se requieren permisos para activar la taskbar")
                    showPermissionsDialog()
                }
            } else {
                try {
                    stopTaskbarService()
                    showToast("Taskbar desactivado")
                } catch (e: Exception) {
                    showToast("Error al detener la taskbar")
                    e.printStackTrace()
                }
            }
        }

        // Botón conceder todos
        view.findViewById<Button>(R.id.btn_grant_all).setOnClickListener {
            showPermissionsDialog()
        }

        // Actualizar banner de permisos
        updatePermissionBanner()

        // MENU ROWS - configurar icono y texto, luego navegar
        setupMenuRow(view, R.id.row_general, "⚙️", "Ajustes generales") {
            navigateTo(GeneralFragment())
        }
        setupMenuRow(view, R.id.row_appearance, "🎨", "Apariencia") {
            navigateTo(AppearanceFragment())
        }
        setupMenuRow(view, R.id.row_recent, "🕐", "Últimas aplicaciones") {
            navigateTo(RecentFragment())
        }
        setupMenuRow(view, R.id.row_freewindow, "🪟", "Modo ventana libre") {
            navigateTo(FreeWindowFragment())
        }
        setupMenuRow(view, R.id.row_desktop, "🖥️", "Modo escritorio") {
            navigateTo(DesktopFragment())
        }
        setupMenuRow(view, R.id.row_advanced, "🔧", "Opciones avanzadas") {
            navigateTo(AdvancedFragment())
        }
        setupMenuRow(view, R.id.row_permissions, "🔐", "Permisos") {
            navigateTo(PermissionsFragment())
        }
        setupMenuRow(view, R.id.row_quad_windows, "🪟4", "Lanzar 4 ventanas") {
            navigateTo(QuadWindowsFragment())
        }
    }

    private fun setupMenuRow(root: View, rowId: Int, icon: String, text: String, onClick: () -> Unit) {
        val row = root.findViewById<LinearLayout>(rowId)
        row.findViewById<TextView>(R.id.menu_icon).text = icon
        row.findViewById<TextView>(R.id.menu_text).text = text
        row.setOnClickListener { onClick() }
    }

    // ── PERMISOS ──
    // Crítico de verdad para que la barra se vea: SOLO superposición.
    private fun checkCriticalPermissions(): Boolean {
        val ctx = requireContext()
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Settings.canDrawOverlays(ctx) else true
        return hasOverlay
    }

    private fun updatePermissionBanner() {
        val ctx = requireContext()
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Settings.canDrawOverlays(ctx) else true
        val hasUsage = hasUsageStatsPermission(ctx)
        val hasNotif = isNotificationListenerEnabled(ctx)
        val hasA11y = isAccessibilityEnabled(ctx)

        // Solo mostramos como "crítico" el overlay; el resto son mejoras.
        val missingPerms = mutableListOf<String>()
        if (!hasOverlay) missingPerms.add("Superponer sobre otras apps (CRÍTICO)")
        if (!hasUsage) missingPerms.add("Acceso a datos de uso (para recientes)")
        if (!hasNotif) missingPerms.add("Acceso a notificaciones (para contadores)")
        if (!hasA11y) missingPerms.add("Servicio de accesibilidad (para gestos globales)")

        if (missingPerms.isEmpty()) {
            permBanner.visibility = View.GONE
        } else {
            permBanner.visibility = View.VISIBLE
            val permText = if (missingPerms.size == 1) {
                "Falta el permiso: ${missingPerms[0]}"
            } else {
                "Faltan ${missingPerms.size} permisos:\n• ${missingPerms.joinToString("\n• ")}"
            }
            permBannerText.text =
                "Taskbar puede funcionar parcialmente, pero para la mejor experiencia conviene conceder estos permisos:\n\n$permText\n\nToca el botón para concederlos paso a paso."
        }
    }

    private fun startTaskbarService() {
        try {
            val intent = Intent(requireContext(), TaskbarService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                @Suppress("DEPRECATION")
                requireContext().startService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainFragment", "Error al iniciar servicio: ${e.message}")
            e.printStackTrace()
            showToast("Error al iniciar la taskbar")
        }
    }

    private fun stopTaskbarService() {
        try {
            val intent = Intent(requireContext(), TaskbarService::class.java)
            requireContext().stopService(intent)
        } catch (e: Exception) {
            android.util.Log.e("MainFragment", "Error al detener servicio: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showPermissionsDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        val sheetView = layoutInflater.inflate(R.layout.dialog_permissions, null)
        dialog.setContentView(sheetView)

        val ctx = requireContext()

        // Estado actual de cada permiso
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Settings.canDrawOverlays(ctx) else true
        val hasUsage = hasUsageStatsPermission(ctx)
        val hasNotif = isNotificationListenerEnabled(ctx)
        val hasA11y = isAccessibilityEnabled(ctx)

        // Botones individuales
        sheetView.findViewById<Button>(R.id.btn_perm_overlay).apply {
            if (hasOverlay) {
                text = "✓ Concedido"
                isEnabled = false
                setBackgroundResource(R.drawable.btn_granted)
            } else setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${ctx.packageName}")
                        )
                    )
                }
                dialog.dismiss()
            }
        }

        sheetView.findViewById<Button>(R.id.btn_perm_usage).apply {
            if (hasUsage) {
                text = "✓ Concedido"
                isEnabled = false
                setBackgroundResource(R.drawable.btn_granted)
            } else setOnClickListener {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                dialog.dismiss()
            }
        }

        sheetView.findViewById<Button>(R.id.btn_perm_notif).apply {
            if (hasNotif) {
                text = "✓ Concedido"
                isEnabled = false
                setBackgroundResource(R.drawable.btn_granted)
            } else setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                dialog.dismiss()
            }
        }

        sheetView.findViewById<Button>(R.id.btn_perm_a11y).apply {
            if (hasA11y) {
                text = "✓ Concedido"
                isEnabled = false
                setBackgroundResource(R.drawable.btn_granted)
            } else setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                dialog.dismiss()
            }
        }

        sheetView.findViewById<Button>(R.id.btn_perm_admin).setOnClickListener {
            // Abrir el diálogo específico para activar este administrador de dispositivo
            val component = ComponentName(ctx, com.taskbar.app.receiver.TaskbarDeviceAdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Taskbar usa este permiso para poder bloquear la pantalla desde la barra de tareas."
                )
            }
            startActivity(intent)
            dialog.dismiss()
        }

        // Botón conceder TODOS (va abriendo una configuración tras otra)
        sheetView.findViewById<Button>(R.id.btn_grant_all_perms).setOnClickListener {
            when {
                !hasUsage -> startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                !hasOverlay && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${ctx.packageName}")
                        )
                    )
                }
                !hasNotif -> startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                !hasA11y -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                else -> showToast("✅ Todos los permisos ya están concedidos")
            }
            dialog.dismiss()
        }

        sheetView.findViewById<TextView>(R.id.btn_close_perms).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isNotificationListenerEnabled(ctx: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            ctx.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabledListeners.contains(ctx.packageName)
    }

    private fun isAccessibilityEnabled(ctx: Context): Boolean {
        val enabled = Settings.Secure.getInt(
            ctx.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )
        return enabled == 1
    }

    override fun onResume() {
        super.onResume()
        // Actualizar banner y switch al volver de ajustes
        updatePermissionBanner()
        view?.findViewById<Switch>(R.id.switch_taskbar_enabled)?.isChecked = preferenceManager.isTaskbarEnabled
    }
}
