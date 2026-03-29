package com.taskbar.app.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.taskbar.app.utils.FreeformLauncher

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Drawable,
    val isSystemApp: Boolean = false
)

class AppManager(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager
    
    fun getAllInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        
        try {
            val flags = if (includeSystemApps) {
                PackageManager.GET_META_DATA
            } else {
                PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES
            }
            
            val installedPackages = packageManager.getInstalledPackages(flags)
            
            for (packageInfo in installedPackages) {
                try {
                    val appInfo = packageInfo.applicationInfo
                    
                    // Filtrar apps del sistema si no se incluyen
                    if (!includeSystemApps && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                        continue
                    }
                    
                    // Solo apps con launcher
                    if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) == null) {
                        continue
                    }
                    
                    val name = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = packageManager.getApplicationIcon(appInfo)
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    
                    apps.add(AppInfo(
                        packageName = packageInfo.packageName,
                        name = name,
                        icon = icon,
                        isSystemApp = isSystemApp
                    ))
                } catch (e: Exception) {
                    // Ignorar apps que no se pueden cargar
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Ordenar alfabéticamente
        return apps.sortedBy { it.name }
    }
    
    fun getAppInfo(packageName: String): AppInfo? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val name = packageManager.getApplicationLabel(appInfo).toString()
            val icon = packageManager.getApplicationIcon(appInfo)
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            
            AppInfo(
                packageName = packageName,
                name = name,
                icon = icon,
                isSystemApp = isSystemApp
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun launchApp(packageName: String, freeform: Boolean = false): Boolean {
        return try {
            // Cuando freeform está activado, delegamos en FreeformLauncher,
            // que ya maneja reflection y fallbacks sin crashear.
            if (freeform && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                FreeformLauncher.launchInFreeform(context, packageName)
                true
            } else {
                val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
