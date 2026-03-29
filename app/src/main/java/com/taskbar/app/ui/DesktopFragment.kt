package com.taskbar.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.taskbar.app.R

class DesktopFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_desktop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btn_back).setOnClickListener { goBack() }
        view.findViewById<TextView>(R.id.btn_help).setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Ayuda - Modo escritorio")
                .setMessage(
                    "El modo escritorio permite lanzar aplicaciones en ventana en una " +
                    "pantalla externa, similar a Samsung DeX.\n\n" +
                    "Requiere:\n" +
                    "• Un adaptador de USB a HDMI o lapdock\n" +
                    "• Un dispositivo que soporte la salida de video\n" +
                    "• Android 10 o superior recomendado"
                )
                .setPositiveButton("Entendido", null)
                .show()
        }

        setupCheckRow(view, R.id.row_desktop_mode,
            "Modo escritorio",
            "Utilizar la barra de tareas para lanzar aplicaciones en ventana en una " +
            "pantalla externa, similar a Samsung DeX y otras soluciones.\n\n" +
            "Requiere un adaptador de USB a HDMI, o un lapdock, y un dispositivo " +
            "que soporte la salida de video.", true)

        view.findViewById<LinearLayout>(R.id.row_set_as_home).setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Aplicación de inicio")
                .setMessage("Esto abrirá los ajustes de aplicaciones predeterminadas del sistema. Selecciona Taskbar como aplicación de inicio.")
                .setPositiveButton("Abrir ajustes") { _, _ ->
                    try {
                        startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                    } catch (e: Exception) {
                        startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        setupValueRow(view, R.id.row_primary_launcher, R.id.val_primary_launcher,
            "Lanzador primario",
            listOf("Moto App Launcher", "Nova Launcher", "Launcher3", "Smart Launcher", "Otro"),
            "Moto App Launcher")

        setupCheckRow(view, R.id.row_lock_phone_screen,
            "Bloquear y atenuar la pantalla del teléfono mientras...",
            "Pulsa el botón de inicio para activar manualmente", false)

        view.findViewById<LinearLayout>(R.id.row_input_method_fix).setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Input method fix")
                .setMessage(
                    "Para activar:\n\n" +
                    "1. Ve a Ajustes → Sistema → Idioma y entrada\n" +
                    "2. Selecciona Taskbar como método de entrada\n" +
                    "3. Vuelve a tu método de entrada preferido después de iniciar el modo escritorio"
                )
                .setPositiveButton("Ir a Ajustes") { _, _ ->
                    startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        view.findViewById<LinearLayout>(R.id.row_extra_settings).setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Activar ajustes adicionales")
                .setMessage(
                    "Para activar ajustes adicionales del modo escritorio:\n\n" +
                    "• Conecta tu dispositivo a una pantalla externa\n" +
                    "• O usa ADB: adb shell settings put global force_desktop_mode_on_external_displays 1\n\n" +
                    "Esto puede variar según tu versión de Android y fabricante."
                )
                .setPositiveButton("Entendido", null)
                .show()
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
