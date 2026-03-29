package com.taskbar.app.utils

import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter

object CrashStore {
    private const val PREFS = "taskbar_crash_store"
    private const val KEY_LAST_CRASH = "last_crash"

    fun save(context: Context, throwable: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_CRASH, sw.toString())
            .apply()
    }

    fun consume(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val crash = prefs.getString(KEY_LAST_CRASH, null) ?: return null
        prefs.edit().remove(KEY_LAST_CRASH).apply()
        return crash
    }
}

