package com.taskbar.app.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.taskbar.app.R
import com.taskbar.app.utils.PreferenceManager

class RecentFragment : BaseFragment() {
    
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())
        view.findViewById<TextView>(R.id.btn_back).setOnClickListener { goBack() }

        // Acceso a datos de uso
        view.findViewById<LinearLayout>(R.id.row_usage_access).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            showToast("Activa el permiso para Taskbar")
        }

        // Selectors
        setupValueRow(view, R.id.row_update_freq, R.id.val_update_freq,
            "Frecuencia de actualización",
            listOf("0.5 segundos", "1 segundo", "2 segundos", "5 segundos"),
            preferenceManager.updateFreq) { preferenceManager.updateFreq = it }

        setupValueRow(view, R.id.row_running_apps, R.id.val_running_apps,
            "Número de aplicaciones en ejecución",
            listOf("Desde el último día", "Desde la última hora", "Desde la última semana",
                "Desde el último mes"),
            preferenceManager.runningApps) { preferenceManager.runningApps = it }

        setupValueRow(view, R.id.row_sort_order, R.id.val_sort_order,
            "Secuencia de clasificación",
            listOf("Último usado (descendente)", "Último usado (ascendente)", "Alfabético (A-Z)",
                "Alfabético (Z-A)"),
            preferenceManager.sortOrder) { preferenceManager.sortOrder = it }

        setupValueRow(view, R.id.row_max_recent, R.id.val_max_recent,
            "Número máximo de aplicaciones recientes/ancladas",
            listOf("5 aplicaciones", "10 aplicaciones", "15 aplicaciones",
                "20 aplicaciones", "Sin límite"),
            preferenceManager.maxRecent) { preferenceManager.maxRecent = it }

        // Switches
        setupCheckRow(view, R.id.row_hide_bg_icon,
            "Ocultar el icono para la aplicación en segundo plano...",
            "Podría reducir el rendimiento",
            preferenceManager.hideBgIcon) { preferenceManager.hideBgIcon = it }

        setupCheckRow(view, R.id.row_accelerate_switch,
            "Accelerar cambio de aplicación", null,
            preferenceManager.accelerateSwitch) { preferenceManager.accelerateSwitch = it }

        setupCheckRow(view, R.id.row_disable_scroll_list,
            "Deshabilitar lista de desplazamiento", null,
            preferenceManager.disableScrollList) { preferenceManager.disableScrollList = it }

        setupCheckRow(view, R.id.row_expand_empty,
            "Ampliar el área con espacio vacío", null,
            preferenceManager.expandEmpty) { preferenceManager.expandEmpty = it }

        setupCheckRow(view, R.id.row_center_icons,
            "Centrar iconos dentro de los espacios vacío", null,
            preferenceManager.centerIcons) { preferenceManager.centerIcons = it }

        setupCheckRow(view, R.id.row_show_status_clock,
            "Mostrar los iconos de estado y el reloj",
            "Solo visible cuando la Barra de tareas es horizontal",
            preferenceManager.showStatusClock) { preferenceManager.showStatusClock = it }
    }

    private fun setupValueRow(view: View, rowId: Int, valId: Int, title: String,
                               options: List<String>, currentValue: String, onSave: (String) -> Unit) {
        val row = view.findViewById<LinearLayout>(rowId)
        val valView = view.findViewById<TextView>(valId)
        valView.text = currentValue
        row.setOnClickListener {
            val idx = options.indexOf(valView.text.toString()).takeIf { it >= 0 } ?: 0
            android.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setSingleChoiceItems(options.toTypedArray(), idx) { dialog, which ->
                    valView.text = options[which]
                    onSave(options[which])
                    showToast("✓ ${options[which]}")
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun setupCheckRow(view: View, rowId: Int, title: String, subtitle: String?,
                               currentValue: Boolean, onSave: (Boolean) -> Unit) {
        val row = view.findViewById<LinearLayout>(rowId)
        row.findViewById<TextView>(R.id.check_title).text = title
        val subtitleView = row.findViewById<TextView>(R.id.check_subtitle)
        if (subtitle != null) { subtitleView.text = subtitle; subtitleView.visibility = View.VISIBLE }
        val switch = row.findViewById<Switch>(R.id.row_switch)
        switch.isChecked = currentValue
        row.setOnClickListener { switch.isChecked = !switch.isChecked }
        switch.setOnCheckedChangeListener { _, isChecked ->
            onSave(isChecked)
            showToast(if (isChecked) "Activado" else "Desactivado")
        }
    }
}
