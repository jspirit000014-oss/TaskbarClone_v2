package com.taskbar.app.ui

import android.app.AppOpsManager
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
import com.taskbar.app.R

class PermissionsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_permissions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btn_back).setOnClickListener { goBack() }

        val ctx = requireContext()

        // Verificar estado de cada permiso
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Settings.canDrawOverlays(ctx) else true
        val hasUsage = hasUsageStatsPermission(ctx)
        val hasNotif = isNotificationListenerEnabled(ctx)
        val hasA11y = isAccessibilityEnabled(ctx)

        // Actualizar estado visual de cada permiso
        updatePermissionStatus(view, R.id.perm_overlay_status, hasOverlay, "Superponer sobre otras apps")
        updatePermissionStatus(view, R.id.perm_usage_status, hasUsage, "Acceso a datos de uso")
        updatePermissionStatus(view, R.id.perm_notif_status, hasNotif, "Acceso a notificaciones")
        updatePermissionStatus(view, R.id.perm_a11y_status, hasA11y, "Servicio de accesibilidad")

        // Botones para conceder permisos
        view.findViewById<Button>(R.id.btn_perm_overlay).apply {
            if (hasOverlay) {
                text = "✓ Concedido"
                isEnabled = false
                setBackgroundResource(R.drawable.btn_granted)
            } else {
                setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${ctx.packageName}")
                            )
                        )
                    }
                }
            }
        }

        view.findViewById<Button>(R.id.btn_perm_usage).apply {
            if (hasUsage) {
                text = "✓ Concedido"
                isEnabled = false
                setBackgroundResource(R.drawable.btn_granted)
            } else {
                setOnClickListener {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            }
        }

        view.findViewById<Button>(R.id.btn_perm_notif).apply {
            if (hasNotif) {
                text = "✓ Concedido"
                isEnabled = false
                setBackgroundResource(R.drawable.btn_granted)
            } else {
                setOnClickListener {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }
        }

        view.findViewById<Button>(R.id.btn_perm_a11y).apply {
            if (hasA11y) {
                text = "✓ Concedido"
                isEnabled = false
                setBackgroundResource(R.drawable.btn_granted)
            } else {
                setOnClickListener {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            }
        }

        // Botón conceder todos
        view.findViewById<Button>(R.id.btn_grant_all_perms).setOnClickListener {
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
        }
    }

    private fun updatePermissionStatus(view: View, statusId: Int, granted: Boolean, permName: String) {
        val statusView = view.findViewById<TextView>(statusId)
        if (granted) {
            statusView.text = "✓ Concedido"
            statusView.setTextColor(resources.getColor(R.color.green, null))
        } else {
            statusView.text = "✗ Pendiente"
            statusView.setTextColor(resources.getColor(R.color.orange2, null))
        }
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
        // Actualizar estado al volver de ajustes
        view?.let { onViewCreated(it, null) }
    }
}
