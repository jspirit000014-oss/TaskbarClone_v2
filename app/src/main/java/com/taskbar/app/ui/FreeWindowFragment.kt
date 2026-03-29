package com.taskbar.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.taskbar.app.R
import com.taskbar.app.utils.PreferenceManager

class FreeWindowFragment : BaseFragment() {
    
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_freewindow, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())
        view.findViewById<TextView>(R.id.btn_back).setOnClickListener { goBack() }
        view.findViewById<TextView>(R.id.btn_help).setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Ayuda - Modo ventana libre")
                .setMessage(
                    "El modo ventana libre permite abrir aplicaciones en ventanas flotantes " +
                    "superpuestas sobre otras aplicaciones.\n\n" +
                    "Requiere Android 8.0 (Oreo) o superior.\n\n" +
                    "⚠️ Esta característica es experimental y puede causar problemas " +
                    "con la multitarea estándar de pantalla dividida."
                )
                .setPositiveButton("Entendido", null)
                .show()
        }

        setupCheckRow(view, R.id.row_freewindow_support,
            "Soporte de ventana libre",
            "Permite que la barra de tareas inicie aplicaciones en modo ventana libre " +
            "para una experiencia más parecida a la del escritorio.\n\n" +
            "Esta característica es experimental y puede causar problemas con la " +
            "multitarea estándar de pantalla dividida.",
            preferenceManager.freewindowSupport) { preferenceManager.freewindowSupport = it }

        setupCheckRow(view, R.id.row_save_window_sizes,
            "Siempre guarda los tamaños de las ventanas...",
            "Cuando se abre las nuevas ventanas desde el menú contextual",
            preferenceManager.saveWindowSizes) { preferenceManager.saveWindowSizes = it }

        setupCheckRow(view, R.id.row_always_new_window,
            "Siempre abrir aplicaciones en nuevas ventanas",
            "Obliga a una aplicación a abrir una nueva ventana cada vez que es " +
            "seleccionada en el menú de inicio o en aplicaciones recientes " +
            "(si una aplicación permite múltiples ventanas)",
            preferenceManager.alwaysNewWindow) { preferenceManager.alwaysNewWindow = it }

        setupCheckRow(view, R.id.row_games_fullscreen,
            "Lanzar juegos en pantalla completa",
            "Si una aplicación se declara como un juego, no se le permite " +
            "ejecutarse como ventana libre.",
            preferenceManager.gamesFullscreen) { preferenceManager.gamesFullscreen = it }

        setupValueRow(view, R.id.row_window_size, R.id.val_window_size,
            "Tamaño predeterminado de ventana",
            listOf("Estándar", "Pequeño", "Grande", "Pantalla completa"),
            preferenceManager.windowSize) { preferenceManager.windowSize = it }
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
                .setNegativeButton("Cancelar", null).show()
        }
    }
}
