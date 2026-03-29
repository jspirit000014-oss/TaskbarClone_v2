package com.taskbar.app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.taskbar.app.R

class AppearanceFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_appearance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btn_back).setOnClickListener { goBack() }

        // Opciones con selector
        setupValueRow(view, R.id.row_theme, R.id.val_theme, "Tema",
            listOf("Sistema", "Claro", "Oscuro", "Naranja"), "Sistema")

        setupValueRow(view, R.id.row_icon_pack, R.id.val_icon_pack, "Paquete de iconos",
            listOf("Ninguna", "Adaptive Pack", "Material You", "Round Icons"), "Ninguna")

        setupValueRow(view, R.id.row_start_img, R.id.val_start_img, "Imagen del botón de inicio",
            listOf("Predeterminado", "Personalizado", "Sin imagen"), "Predeterminado")

        // Color de fondo - abre color picker
        view.findViewById<LinearLayout>(R.id.row_bg_color).setOnClickListener {
            showColorPickerDialog("Color de fondo", "#66000000") { color ->
                view.findViewById<TextView>(R.id.val_bg_color).text = color
                try {
                    view.findViewById<View>(R.id.preview_bg_color)
                        .setBackgroundColor(Color.parseColor(color.replace("#66", "#AA")))
                } catch (e: Exception) { /* color inválido */ }
            }
        }

        // Color de acento
        view.findViewById<LinearLayout>(R.id.row_accent_color).setOnClickListener {
            showColorPickerDialog("Color de acento", "#FFF0F0F0") { color ->
                view.findViewById<TextView>(R.id.val_accent_color).text = color
                try {
                    view.findViewById<View>(R.id.preview_accent_color)
                        .setBackgroundColor(Color.parseColor(color))
                } catch (e: Exception) { /* color inválido */ }
            }
        }

        // Switches
        setupCheckRow(view, R.id.row_hide_btn_collapsed,
            "Ocultar botón cuando la Barra de tareas se colapsa...", null, false)

        setupCheckRow(view, R.id.row_shortcut_icon,
            "Show shortcut icon for pinned apps", null, true)

        setupCheckRow(view, R.id.row_visual_feedback,
            "Retroalimentación visual al seleccionar los iconos...", null, true)

        setupCheckRow(view, R.id.row_menu_transparency,
            "Transparencia del menú de inicio", null, false)

        setupCheckRow(view, R.id.row_hide_icon_labels,
            "Ocultar etiquetas de iconos de aplicaciones", null, false)

        setupCheckRow(view, R.id.row_use_masks,
            "Utilizar máscaras para los iconos no tematizados...",
            "Puede reducir el rendimiento cuando un paquete de iconos está configurado", false)

        view.findViewById<LinearLayout>(R.id.row_reset_colors).setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Restablecer colores")
                .setMessage("¿Quieres restablecer todos los colores a los valores iniciales?")
                .setPositiveButton("Restablecer") { _, _ ->
                    view.findViewById<TextView>(R.id.val_bg_color).text = "#66000000"
                    view.findViewById<TextView>(R.id.val_accent_color).text = "#FFF0F0F0"
                    showToast("Colores restablecidos")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun setupValueRow(view: View, rowId: Int, valId: Int, title: String,
                               options: List<String>, default: String) {
        val row = view.findViewById<LinearLayout>(rowId)
        val valView = view.findViewById<TextView>(valId)
        valView.text = default
        row.setOnClickListener {
            showSingleChoiceDialog(title, options, valView.text.toString()) { chosen ->
                valView.text = chosen
                showToast("✓ $chosen")
            }
        }
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

    private fun showColorPickerDialog(title: String, currentColor: String,
                                       onSelect: (String) -> Unit) {
        val colors = listOf(
            "#66000000" to "Fondo semitransparente",
            "#FF000000" to "Negro",
            "#FF1A1A2E" to "Azul oscuro",
            "#FF2D2D2D" to "Gris oscuro",
            "#FFE67E22" to "Naranja",
            "#FF3C3C8F" to "Azul Taskbar",
            "#FFF0F0F0" to "Blanco suave",
            "#FFFFFFFF" to "Blanco"
        )
        val items = colors.map { it.second }.toTypedArray()
        val current = colors.indexOfFirst { it.first == currentColor }.takeIf { it >= 0 } ?: 0

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setSingleChoiceItems(items, current) { dialog, which ->
                onSelect(colors[which].first)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
    }
}
