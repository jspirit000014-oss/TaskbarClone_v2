package com.taskbar.app

import android.app.Application
import com.taskbar.app.utils.CrashStore

class TaskbarApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                CrashStore.save(this, throwable)
            } catch (_: Exception) {
                // Ignorar: si falla el guardado, dejamos que el crash ocurra igual.
            } finally {
                previousHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}

