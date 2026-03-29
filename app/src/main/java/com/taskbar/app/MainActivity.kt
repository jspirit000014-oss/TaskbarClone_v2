package com.taskbar.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.taskbar.app.ui.MainFragment
import com.taskbar.app.utils.CrashStore

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Antes de hacer nada, comprobar permiso de superposición.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            // Lanza la pantalla del sistema para que el usuario lo active.
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainFragment())
                .commit()
        }

        // Si la app crasheó antes, mostrar el stacktrace aquí (sin logcat).
        CrashStore.consume(this)?.let { crash ->
            AlertDialog.Builder(this)
                .setTitle("Crash detectado")
                .setMessage(crash.take(4000))
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
