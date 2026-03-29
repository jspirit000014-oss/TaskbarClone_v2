package com.taskbar.app.ui

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taskbar.app.R
import com.taskbar.app.utils.AppManager
import com.taskbar.app.utils.FreeformLauncher
import com.taskbar.app.utils.PreferenceManager
import com.taskbar.app.utils.ShizukuHelper

class QuadWindowsFragment : BaseFragment() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var appManager: AppManager

    private lateinit var slot1: View
    private lateinit var slot2: View
    private lateinit var slot3: View
    private lateinit var slot4: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quad_windows, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())
        appManager = AppManager(requireContext())

        slot1 = view.findViewById(R.id.slot1)
        slot2 = view.findViewById(R.id.slot2)
        slot3 = view.findViewById(R.id.slot3)
        slot4 = view.findViewById(R.id.slot4)

        slot1.setOnClickListener { pickAppForSlot(1) }
        slot2.setOnClickListener { pickAppForSlot(2) }
        slot3.setOnClickListener { pickAppForSlot(3) }
        slot4.setOnClickListener { pickAppForSlot(4) }

        listOf(1, 2, 3, 4).forEach { slot ->
            val slotView = when (slot) {
                1 -> slot1
                2 -> slot2
                3 -> slot3
                else -> slot4
            }
            slotView.findViewById<Button>(R.id.slot_btn_predefine).setOnClickListener {
                saveBoundsForSlot(slot)
            }
        }

        view.findViewById<Button>(R.id.btn_launch_quad).setOnClickListener {
            launchQuad()
        }

        val btnResetBounds = view.findViewById<Button>(R.id.btn_reset_quad_bounds)
        if (btnResetBounds != null) {
            btnResetBounds.setOnClickListener {
                preferenceManager.clearAllQuadBounds()
                showToast("Posiciones restablecidas a 2×2 por defecto")
            }
        }

        // Botón Shizuku
        val btnShizuku = view.findViewById<Button>(R.id.btn_shizuku_permission)
        if (btnShizuku != null) {
            updateShizukuButton(btnShizuku)
            btnShizuku.setOnClickListener {
                when {
                    !ShizukuHelper.isAvailable() ->
                        showToast("Shizuku no está activo. Ábrelo y toca Iniciar.")
                    !ShizukuHelper.hasPermission() -> {
                        ShizukuHelper.requestPermission()
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isAdded) updateShizukuButton(btnShizuku)
                        }, 1500L)
                    }
                    else ->
                        showToast("Shizuku activo ✓ — Las ventanas se posicionarán exactamente.")
                }
            }
        }

        updateSlots()
    }

    private fun updateShizukuButton(btn: Button) {
        when {
            !ShizukuHelper.isAvailable() -> {
                btn.text = "Shizuku: no activo"
                btn.alpha = 0.5f
            }
            !ShizukuHelper.hasPermission() -> {
                btn.text = "Conceder permiso a Shizuku"
                btn.alpha = 1f
            }
            else -> {
                btn.text = "✓ Shizuku activo"
                btn.alpha = 1f
            }
        }
    }

    /** Guarda la posición predefinida del cuadrante (2x2 a extremos, bien dimensionado). */
    private fun saveBoundsForSlot(slot: Int) {
        val bounds = FreeformLauncher.getScreenBounds(requireContext().applicationContext)
        val left0 = bounds.left
        val top0 = bounds.top
        val width = bounds.width()
        val height = bounds.height()
        val halfW = width / 2
        val halfH = height / 2
        val w1 = halfW
        val w2 = width - halfW
        val h1 = halfH
        val h2 = height - halfH
        val (left, top, w, h) = when (slot) {
            1 -> Quad(left0, top0, w1, h1)
            2 -> Quad(left0 + halfW, top0, w2, h1)
            3 -> Quad(left0, top0 + halfH, w1, h2)
            else -> Quad(left0 + halfW, top0 + halfH, w2, h2)
        }
        preferenceManager.setQuadBounds(slot, "$left,$top,$w,$h")
        showToast("Posición del cuadrante $slot guardada")
    }

    private data class Quad(val left: Int, val top: Int, val width: Int, val height: Int)

    private fun updateSlots() {
        fun setSlot(slotView: View, title: String, pkg: String) {
            slotView.findViewById<TextView>(R.id.slot_title).text = title
            val subtitle = slotView.findViewById<TextView>(R.id.slot_subtitle)
            val iconView = slotView.findViewById<ImageView>(R.id.slot_icon)
            if (pkg.isEmpty()) {
                subtitle.text = "Toca para elegir"
                iconView.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_add)
                )
                iconView.alpha = 0.5f
            } else {
                val app = appManager.getAppInfo(pkg)
                subtitle.text = app?.name ?: pkg
                iconView.setImageDrawable(app?.icon)
                iconView.alpha = 1f
            }
        }

        setSlot(slot1, "1 · Sup. izq.", preferenceManager.quadApp1)
        setSlot(slot2, "2 · Sup. der.", preferenceManager.quadApp2)
        setSlot(slot3, "3 · Inf. izq.", preferenceManager.quadApp3)
        setSlot(slot4, "4 · Inf. der.", preferenceManager.quadApp4)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pickerDialog?.dismiss()
        pickerDialog = null
    }

    private fun pickAppForSlot(slot: Int) {
        val apps = appManager.getAllInstalledApps(includeSystemApps = false)
        if (apps.isEmpty()) {
            showToast("No se encontraron aplicaciones instaladas")
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_quad_app_picker, null)
        dialogView.findViewById<TextView>(R.id.dialog_title).text =
            "Elige la app para el cuadrante $slot"
        val rv = dialogView.findViewById<RecyclerView>(R.id.rv_apps)
        rv.layoutManager = GridLayoutManager(requireContext(), 4)
        rv.adapter = QuadAppAdapter(apps) { app ->
            when (slot) {
                1 -> preferenceManager.quadApp1 = app.packageName
                2 -> preferenceManager.quadApp2 = app.packageName
                3 -> preferenceManager.quadApp3 = app.packageName
                4 -> preferenceManager.quadApp4 = app.packageName
            }
            updateSlots()
            pickerDialog?.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            pickerDialog?.dismiss()
        }

        pickerDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        pickerDialog?.show()
    }

    private var pickerDialog: AlertDialog? = null

    private inner class QuadAppAdapter(
        private val apps: List<com.taskbar.app.utils.AppInfo>,
        private val onPick: (com.taskbar.app.utils.AppInfo) -> Unit
    ) : RecyclerView.Adapter<QuadAppAdapter.VH>() {
        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.ivAppIcon)
            val name: TextView = view.findViewById(R.id.tvAppName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_icon_quad, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = apps[position]
            holder.icon.setImageDrawable(app.icon)
            holder.name.text = app.name
            holder.itemView.setOnClickListener { onPick(app) }
        }

        override fun getItemCount() = apps.size
    }

    private fun launchQuad() {
        if (!preferenceManager.freewindowSupport) {
            showToast("Activa primero \"Soporte de ventana libre\" en ajustes.")
            return
        }

        val pkgs = listOf(
            preferenceManager.quadApp1,
            preferenceManager.quadApp2,
            preferenceManager.quadApp3,
            preferenceManager.quadApp4
        )

        if (pkgs.all { it.isEmpty() }) {
            showToast("Configura al menos una app antes de lanzar")
            return
        }

        // Diálogo para mejor resultado en Android 13+
        AlertDialog.Builder(requireContext())
            .setTitle("Lanzar 4 ventanas")
            .setMessage("Para que las apps abran exactamente en sus cuadrantes, se recomienda cerrarlas primero.\n\n¿Cómo quieres lanzar?")
            .setPositiveButton("Cerrar y lanzar") { _, _ ->
                val appCtx = requireContext().applicationContext
                pkgs.filter { it.isNotEmpty() }.forEach {
                    FreeformLauncher.forceStopApp(appCtx, it)
                }
                showToast("Cerrando apps...")
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded) doLaunchQuad(pkgs)
                }, 900L)
            }
            .setNeutralButton("Lanzar directo") { _, _ ->
                doLaunchQuad(pkgs)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun doLaunchQuad(pkgs: List<String>) {
        FreeformLauncher.clearSavedLaunchParams(requireContext(), pkgs)
        FreeformLauncher.tryEnableFreeformSupport(requireContext())

        val bounds = FreeformLauncher.getScreenBoundsUsable(requireContext().applicationContext)
        val left0 = bounds.left
        val top0 = bounds.top
        val width = bounds.width()
        val height = bounds.height()
        val halfW = width / 2
        val halfH = height / 2

        fun boundsForSlot(slot: Int): Quad {
            val saved = preferenceManager.getQuadBounds(slot)
            if (!saved.isNullOrBlank()) {
                val parts = saved.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (parts.size == 4) return Quad(parts[0], parts[1], parts[2], parts[3])
            }
            return when (slot) {
                1 -> Quad(left0, top0, halfW, halfH)
                2 -> Quad(left0 + halfW, top0, width - halfW, halfH)
                3 -> Quad(left0, top0 + halfH, halfW, height - halfH)
                else -> Quad(left0 + halfW, top0 + halfH, width - halfW, height - halfH)
            }
        }

        val q1 = boundsForSlot(1)
        val q2 = boundsForSlot(2)
        val q3 = boundsForSlot(3)
        val q4 = boundsForSlot(4)

        val rects = listOf(
            Rect(q1.left, q1.top, q1.left + q1.width, q1.top + q1.height),
            Rect(q2.left, q2.top, q2.left + q2.width, q2.top + q2.height),
            Rect(q3.left, q3.top, q3.left + q3.width, q3.top + q3.height),
            Rect(q4.left, q4.top, q4.left + q4.width, q4.top + q4.height)
        )

        val handler = Handler(Looper.getMainLooper())
        val appContext = requireContext().applicationContext
        var delay = 0L

        // Lanzar con 700ms entre apps para que Android respete los bounds
        listOf(
            Triple(pkgs[0], q1, rects[0]),
            Triple(pkgs[1], q2, rects[1]),
            Triple(pkgs[2], q3, rects[2]),
            Triple(pkgs[3], q4, rects[3])
        ).forEach { (pkg, q, _) ->
            if (pkg.isNotEmpty()) {
                handler.postDelayed({
                    FreeformLauncher.launchInFreeformWithBounds(
                        appContext, pkg,
                        left = q.left, top = q.top,
                        width = q.width, height = q.height
                    )
                }, delay)
                delay += 700L
            }
        }

        // Reposicionar en múltiples intentos tras lanzar
        listOf(500L, 1200L, 2000L, 3200L).forEach { offset ->
            handler.postDelayed({
                FreeformLauncher.repositionQuadrantWindows(appContext, pkgs, rects)
            }, delay + offset)
        }

        showToast("Lanzando 4 apps en cuadrantes...")
    }
}

