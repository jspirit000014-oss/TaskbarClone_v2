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

class GeneralFragment : BaseFragment() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_general, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())
        view.findViewById<TextView>(R.id.btn_back).setOnClickListener { goBack() }

        // ── Opciones con selector ──
        setupValueRow(view, R.id.row_start_view, R.id.val_start_view,
            "Vista del menú de inicio",
            listOf("Cuadrícula", "Lista", "Vista compacta"),
            preferenceManager.startView) { preferenceManager.startView = it }

        setupValueRow(view, R.id.row_position, R.id.val_position,
            "Posición en pantalla",
            listOf(
                "Derecha superior (vertical)",
                "Izquierda superior (vertical)",
                "Parte inferior (horizontal)",
                "Parte superior (horizontal)"
            ),
            preferenceManager.position) { preferenceManager.position = it }

        setupValueRow(view, R.id.row_searchbar, R.id.val_searchbar,
            "Visibilidad de la barra de búsqueda",
            listOf(
                "Mostrar siempre la barra de búsqueda",
                "Mostrar al desplazar",
                "Ocultar siempre"
            ),
            preferenceManager.searchbar) { preferenceManager.searchbar = it }

        view.findViewById<LinearLayout>(R.id.row_app_config).setOnClickListener {
            showToast("0 aplicaciones configuradas")
        }

        // ── Switches ──
        setupCheckRow(view, R.id.row_show_scrollbar,
            "Mostrar la barra de desplazamiento en el menú", null,
            preferenceManager.showScrollbar) { preferenceManager.showScrollbar = it }

        setupCheckRow(view, R.id.row_hide_after_select,
            "Ocultar la barra de tareas después de seleccionar...", null,
            preferenceManager.hideAfterSelect) { preferenceManager.hideAfterSelect = it }

        setupCheckRow(view, R.id.row_auto_collapse,
            "Colapsar automáticamente la Barra de tareas",
            "Cuando mostrar el teclado en pantalla",
            preferenceManager.autoCollapse) { preferenceManager.autoCollapse = it }

        setupCheckRow(view, R.id.row_alt_position,
            "Posición alternativa para el botón colapsado", null,
            preferenceManager.altPosition) { preferenceManager.altPosition = it }

        setupCheckRow(view, R.id.row_lock_rotation,
            "Anclar Barra de tareas cuando la pantalla gira", null,
            preferenceManager.lockRotation) { preferenceManager.lockRotation = it }

        setupCheckRow(view, R.id.row_start_on_boot,
            "Iniciar en el arranque", null,
            preferenceManager.startOnBoot) { preferenceManager.startOnBoot = it }

        view.findViewById<LinearLayout>(R.id.row_notif_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            })
        }
    }

    private fun setupValueRow(view: View, rowId: Int, valId: Int, title: String,
                              options: List<String>, currentValue: String, onSave: (String) -> Unit) {
        val row = view.findViewById<LinearLayout>(rowId)
        val valView = view.findViewById<TextView>(valId)
        valView.text = currentValue

        row.setOnClickListener {
            showSingleChoiceDialog(title, options, valView.text.toString()) { chosen ->
                valView.text = chosen
                onSave(chosen)
                showToast("✓ $chosen")
            }
        }
    }

    private fun setupCheckRow(view: View, rowId: Int, title: String, subtitle: String?,
                              currentValue: Boolean, onSave: (Boolean) -> Unit) {
        val row = view.findViewById<LinearLayout>(rowId)
        val titleView = row.findViewById<TextView>(R.id.check_title)
        val subtitleView = row.findViewById<TextView>(R.id.check_subtitle)
        val switch = row.findViewById<Switch>(R.id.row_switch)

        titleView.text = title
        if (subtitle != null) {
            subtitleView.text = subtitle
            subtitleView.visibility = View.VISIBLE
        }
        switch.isChecked = currentValue

        row.setOnClickListener { switch.isChecked = !switch.isChecked }
        switch.setOnCheckedChangeListener { _, isChecked ->
            onSave(isChecked)
            showToast(if (isChecked) "Activado" else "Desactivado")
        }
    }

    private fun showSingleChoiceDialog(title: String, options: List<String>,
                                       current: String, onSelect: (String) -> Unit) {
        val checkedItem = options.indexOf(current).takeIf { it >= 0 } ?: 0
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setSingleChoiceItems(options.toTypedArray(), checkedItem) { dialog, which ->
                onSelect(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
            .apply {
                window?.decorView?.setBackgroundColor(0xFF2D2D2D.toInt())
            }
    }
}
