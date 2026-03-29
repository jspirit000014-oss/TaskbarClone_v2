package com.taskbar.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.taskbar.app.R

class AdvancedFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_advanced, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btn_back).setOnClickListener { goBack() }

        setupCheckRow(view, R.id.row_keyboard_shortcuts,
            "Activar atajos de teclado",
            "Iniciar/detener Taskbar: Win+M / Buscar+M\n" +
            "Bloquear dispositivo: Win+L / Buscar+L\n" +
            "Mostrar menú de inicio: Win / Buscar\n" +
            "(requiere Taskbar como aplicación de asistencia en Ajustes)", true)

        setupCheckRow(view, R.id.row_widget_support,
            "Activar soporte para widgets",
            "Añade un botón a la Barra de tareas que muestra los widgets cuando se pulsa", true)

        setupValueRow(view, R.id.row_dashboard_size, R.id.val_dashboard_size,
            "Tamaño de la cuadrícula del dashboard",
            listOf("2 x 2", "3 x 3", "4 x 4", "5 x 5"), "2 x 2")

        view.findViewById<LinearLayout>(R.id.row_nav_buttons).setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Botones de la barra de navegación")
                .setMessage(
                    "Selecciona qué botones agregar a la Barra de tareas:\n\n" +
                    "• Botón Atrás\n" +
                    "• Botón Inicio\n" +
                    "• Botón Recientes\n\n" +
                    "Estos botones aparecerán en la barra de tareas como accesos directos."
                )
                .setPositiveButton("Configurar") { _, _ -> showToast("Función en desarrollo") }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        setupCheckRow(view, R.id.row_third_party,
            "Permitir la integración de aplicaciones de terceros...",
            "Desactivar para mejorar la seguridad", true)

        view.findViewById<LinearLayout>(R.id.row_secondscreen).setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Instalar SecondScreen")
                .setMessage("SecondScreen permite cambiar la resolución y densidad de tu dispositivo cuando se conecta a una pantalla externa.\n\n¿Quieres abrirlo en Google Play?")
                .setPositiveButton("Abrir Play Store") { _, _ ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.farmerbb.secondscreen.free")))
                    } catch (e: Exception) {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.farmerbb.secondscreen.free")))
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        view.findViewById<LinearLayout>(R.id.row_manage_data).setOnClickListener {
            showManageDataDialog()
        }
    }

    private fun showManageDataDialog() {
        val options = arrayOf(
            "📦  Crear copia de seguridad",
            "📂  Restaurar desde copia de seguridad",
            "🗑️  Restablecer todas las preferencias"
        )
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Administrar datos de la aplicación")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showToast("Copia de seguridad creada en Descargas")
                    1 -> showToast("Selecciona un archivo de respaldo")
                    2 -> {
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("⚠️ Restablecer preferencias")
                            .setMessage("¿Estás seguro de que quieres restablecer todas las preferencias? Esta acción no se puede deshacer.")
                            .setPositiveButton("Restablecer") { _, _ ->
                                showToast("Preferencias restablecidas")
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupCheckRow(view: View, rowId: Int, title: String, subtitle: String?,
                               defaultOn: Boolean) {
        val row = view.findViewById<LinearLayout>(rowId)
        row.findViewById<TextView>(R.id.check_title).text = title
        val subtitleView = row.findViewById<TextView>(R.id.check_subtitle)
        if (subtitle != null) { subtitleView.text = subtitle; subtitleView.visibility = View.VISIBLE }
        val switch = row.findViewById<Switch>(R.id.row_switch)
        switch.isChecked = defaultOn
        row.setOnClickListener { switch.isChecked = !switch.isChecked }
        switch.setOnCheckedChangeListener { _, isChecked ->
            showToast(if (isChecked) "Activado" else "Desactivado")
        }
    }

    private fun setupValueRow(view: View, rowId: Int, valId: Int, title: String,
                               options: List<String>, default: String) {
        val row = view.findViewById<LinearLayout>(rowId)
        val valView = view.findViewById<TextView>(valId)
        valView.text = default
        row.setOnClickListener {
            val idx = options.indexOf(valView.text.toString()).takeIf { it >= 0 } ?: 0
            android.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setSingleChoiceItems(options.toTypedArray(), idx) { dialog, which ->
                    valView.text = options[which]; showToast("✓ ${options[which]}"); dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null).show()
        }
    }
}
