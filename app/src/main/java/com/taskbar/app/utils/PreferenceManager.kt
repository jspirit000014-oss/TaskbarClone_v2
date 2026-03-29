package com.taskbar.app.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "taskbar_prefs"
        
        // Taskbar state
        private const val KEY_TASKBAR_ENABLED = "taskbar_enabled"
        
        // Permissions
        private const val KEY_OVERLAY_PERMISSION_GRANTED = "overlay_permission_granted"
        private const val KEY_USAGE_PERMISSION_GRANTED = "usage_permission_granted"
        private const val KEY_NOTIF_PERMISSION_GRANTED = "notif_permission_granted"
        private const val KEY_A11Y_PERMISSION_GRANTED = "a11y_permission_granted"
        
        // General settings
        private const val KEY_START_VIEW = "start_view"
        private const val KEY_POSITION = "position"
        private const val KEY_SEARCHBAR = "searchbar"
        private const val KEY_SHOW_SCROLLBAR = "show_scrollbar"
        private const val KEY_HIDE_AFTER_SELECT = "hide_after_select"
        private const val KEY_AUTO_COLLAPSE = "auto_collapse"
        private const val KEY_ALT_POSITION = "alt_position"
        private const val KEY_LOCK_ROTATION = "lock_rotation"
        private const val KEY_START_ON_BOOT = "start_on_boot"
        
        // Appearance
        private const val KEY_THEME = "theme"
        private const val KEY_ICON_PACK = "icon_pack"
        private const val KEY_START_IMG = "start_img"
        private const val KEY_BG_COLOR = "bg_color"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_HIDE_BTN_COLLAPSED = "hide_btn_collapsed"
        private const val KEY_SHORTCUT_ICON = "shortcut_icon"
        private const val KEY_VISUAL_FEEDBACK = "visual_feedback"
        private const val KEY_MENU_TRANSPARENCY = "menu_transparency"
        private const val KEY_HIDE_ICON_LABELS = "hide_icon_labels"
        private const val KEY_USE_MASKS = "use_masks"
        
        // Recent apps
        private const val KEY_UPDATE_FREQ = "update_freq"
        private const val KEY_RUNNING_APPS = "running_apps"
        private const val KEY_SORT_ORDER = "sort_order"
        private const val KEY_MAX_RECENT = "max_recent"
        private const val KEY_HIDE_BG_ICON = "hide_bg_icon"
        private const val KEY_ACCELERATE_SWITCH = "accelerate_switch"
        private const val KEY_DISABLE_SCROLL_LIST = "disable_scroll_list"
        private const val KEY_EXPAND_EMPTY = "expand_empty"
        private const val KEY_CENTER_ICONS = "center_icons"
        private const val KEY_SHOW_STATUS_CLOCK = "show_status_clock"
        
        // Free window
        private const val KEY_FREEWINDOW_SUPPORT = "freewindow_support"
        private const val KEY_SAVE_WINDOW_SIZES = "save_window_sizes"
        private const val KEY_ALWAYS_NEW_WINDOW = "always_new_window"
        private const val KEY_GAMES_FULLSCREEN = "games_fullscreen"
        private const val KEY_WINDOW_SIZE = "window_size"
        
        // Desktop
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_PRIMARY_LAUNCHER = "primary_launcher"
        private const val KEY_LOCK_PHONE_SCREEN = "lock_phone_screen"
        
        // Advanced
        private const val KEY_KEYBOARD_SHORTCUTS = "keyboard_shortcuts"
        private const val KEY_WIDGET_SUPPORT = "widget_support"
        private const val KEY_DASHBOARD_SIZE = "dashboard_size"
        private const val KEY_THIRD_PARTY = "third_party"
        
        // Pinned apps
        private const val KEY_PINNED_APPS = "pinned_apps"
        private const val KEY_BLOCKED_APPS = "blocked_apps"

        // 4-window quick launch
        private const val KEY_QUAD_APP_1 = "quad_app_1"
        private const val KEY_QUAD_APP_2 = "quad_app_2"
        private const val KEY_QUAD_APP_3 = "quad_app_3"
        private const val KEY_QUAD_APP_4 = "quad_app_4"
        // Coordenadas predefinidas por cuadrante: "left,top,width,height" o "" = usar auto (2x2 extremos)
        private const val KEY_QUAD_BOUNDS_1 = "quad_bounds_1"
        private const val KEY_QUAD_BOUNDS_2 = "quad_bounds_2"
        private const val KEY_QUAD_BOUNDS_3 = "quad_bounds_3"
        private const val KEY_QUAD_BOUNDS_4 = "quad_bounds_4"

        @Volatile
        private var INSTANCE: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Taskbar state
    var isTaskbarEnabled: Boolean
        get() = prefs.getBoolean(KEY_TASKBAR_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_TASKBAR_ENABLED, value).apply()

    fun setPermissionStatus(permission: String, granted: Boolean) {
        when (permission) {
            "overlay" -> prefs.edit().putBoolean(KEY_OVERLAY_PERMISSION_GRANTED, granted).apply()
            "usage" -> prefs.edit().putBoolean(KEY_USAGE_PERMISSION_GRANTED, granted).apply()
            "notif" -> prefs.edit().putBoolean(KEY_NOTIF_PERMISSION_GRANTED, granted).apply()
            "a11y" -> prefs.edit().putBoolean(KEY_A11Y_PERMISSION_GRANTED, granted).apply()
        }
    }
    
    // Generic getters/setters
    fun getString(key: String, defaultValue: String): String = prefs.getString(key, defaultValue) ?: defaultValue
    fun setString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    
    fun getBoolean(key: String, defaultValue: Boolean): Boolean = prefs.getBoolean(key, defaultValue)
    fun setBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    
    // General settings
    var startView: String
        get() = getString(KEY_START_VIEW, "Cuadrícula")
        set(value) = setString(KEY_START_VIEW, value)
    
    var position: String
        get() = getString(KEY_POSITION, "Derecha superior (vertical)")
        set(value) = setString(KEY_POSITION, value)
    
    var searchbar: String
        get() = getString(KEY_SEARCHBAR, "Mostrar siempre la barra de búsqueda")
        set(value) = setString(KEY_SEARCHBAR, value)
    
    var showScrollbar: Boolean
        get() = getBoolean(KEY_SHOW_SCROLLBAR, false)
        set(value) = setBoolean(KEY_SHOW_SCROLLBAR, value)
    
    var hideAfterSelect: Boolean
        get() = getBoolean(KEY_HIDE_AFTER_SELECT, true)
        set(value) = setBoolean(KEY_HIDE_AFTER_SELECT, value)
    
    var autoCollapse: Boolean
        get() = getBoolean(KEY_AUTO_COLLAPSE, false)
        set(value) = setBoolean(KEY_AUTO_COLLAPSE, value)
    
    var altPosition: Boolean
        get() = getBoolean(KEY_ALT_POSITION, false)
        set(value) = setBoolean(KEY_ALT_POSITION, value)
    
    var lockRotation: Boolean
        get() = getBoolean(KEY_LOCK_ROTATION, false)
        set(value) = setBoolean(KEY_LOCK_ROTATION, value)
    
    var startOnBoot: Boolean
        get() = getBoolean(KEY_START_ON_BOOT, false)
        set(value) = setBoolean(KEY_START_ON_BOOT, value)
    
    // Appearance
    var theme: String
        get() = getString(KEY_THEME, "Sistema")
        set(value) = setString(KEY_THEME, value)
    
    var iconPack: String
        get() = getString(KEY_ICON_PACK, "Ninguna")
        set(value) = setString(KEY_ICON_PACK, value)
    
    var startImg: String
        get() = getString(KEY_START_IMG, "Predeterminado")
        set(value) = setString(KEY_START_IMG, value)
    
    var bgColor: String
        get() = getString(KEY_BG_COLOR, "#66000000")
        set(value) = setString(KEY_BG_COLOR, value)
    
    var accentColor: String
        get() = getString(KEY_ACCENT_COLOR, "#FFF0F0F0")
        set(value) = setString(KEY_ACCENT_COLOR, value)
    
    var hideBtnCollapsed: Boolean
        get() = getBoolean(KEY_HIDE_BTN_COLLAPSED, false)
        set(value) = setBoolean(KEY_HIDE_BTN_COLLAPSED, value)
    
    var shortcutIcon: Boolean
        get() = getBoolean(KEY_SHORTCUT_ICON, true)
        set(value) = setBoolean(KEY_SHORTCUT_ICON, value)
    
    var visualFeedback: Boolean
        get() = getBoolean(KEY_VISUAL_FEEDBACK, true)
        set(value) = setBoolean(KEY_VISUAL_FEEDBACK, value)
    
    var menuTransparency: Boolean
        get() = getBoolean(KEY_MENU_TRANSPARENCY, false)
        set(value) = setBoolean(KEY_MENU_TRANSPARENCY, value)
    
    var hideIconLabels: Boolean
        get() = getBoolean(KEY_HIDE_ICON_LABELS, false)
        set(value) = setBoolean(KEY_HIDE_ICON_LABELS, value)
    
    var useMasks: Boolean
        get() = getBoolean(KEY_USE_MASKS, false)
        set(value) = setBoolean(KEY_USE_MASKS, value)
    
    // Recent apps
    var updateFreq: String
        get() = getString(KEY_UPDATE_FREQ, "1 segundo")
        set(value) = setString(KEY_UPDATE_FREQ, value)
    
    var runningApps: String
        get() = getString(KEY_RUNNING_APPS, "Desde el último día")
        set(value) = setString(KEY_RUNNING_APPS, value)
    
    var sortOrder: String
        get() = getString(KEY_SORT_ORDER, "Último usado (descendente)")
        set(value) = setString(KEY_SORT_ORDER, value)
    
    var maxRecent: String
        get() = getString(KEY_MAX_RECENT, "10 aplicaciones")
        set(value) = setString(KEY_MAX_RECENT, value)
    
    var hideBgIcon: Boolean
        get() = getBoolean(KEY_HIDE_BG_ICON, false)
        set(value) = setBoolean(KEY_HIDE_BG_ICON, value)
    
    var accelerateSwitch: Boolean
        get() = getBoolean(KEY_ACCELERATE_SWITCH, false)
        set(value) = setBoolean(KEY_ACCELERATE_SWITCH, value)
    
    var disableScrollList: Boolean
        get() = getBoolean(KEY_DISABLE_SCROLL_LIST, false)
        set(value) = setBoolean(KEY_DISABLE_SCROLL_LIST, value)
    
    var expandEmpty: Boolean
        get() = getBoolean(KEY_EXPAND_EMPTY, true)
        set(value) = setBoolean(KEY_EXPAND_EMPTY, value)
    
    var centerIcons: Boolean
        get() = getBoolean(KEY_CENTER_ICONS, true)
        set(value) = setBoolean(KEY_CENTER_ICONS, value)
    
    var showStatusClock: Boolean
        get() = getBoolean(KEY_SHOW_STATUS_CLOCK, false)
        set(value) = setBoolean(KEY_SHOW_STATUS_CLOCK, value)
    
    // Free window
    var freewindowSupport: Boolean
        get() = getBoolean(KEY_FREEWINDOW_SUPPORT, true)
        set(value) = setBoolean(KEY_FREEWINDOW_SUPPORT, value)
    
    var saveWindowSizes: Boolean
        get() = getBoolean(KEY_SAVE_WINDOW_SIZES, true)
        set(value) = setBoolean(KEY_SAVE_WINDOW_SIZES, value)
    
    var alwaysNewWindow: Boolean
        get() = getBoolean(KEY_ALWAYS_NEW_WINDOW, true)
        set(value) = setBoolean(KEY_ALWAYS_NEW_WINDOW, value)
    
    var gamesFullscreen: Boolean
        get() = getBoolean(KEY_GAMES_FULLSCREEN, true)
        set(value) = setBoolean(KEY_GAMES_FULLSCREEN, value)
    
    var windowSize: String
        get() = getString(KEY_WINDOW_SIZE, "Estándar")
        set(value) = setString(KEY_WINDOW_SIZE, value)
    
    // Desktop
    var desktopMode: Boolean
        get() = getBoolean(KEY_DESKTOP_MODE, true)
        set(value) = setBoolean(KEY_DESKTOP_MODE, value)
    
    var primaryLauncher: String
        get() = getString(KEY_PRIMARY_LAUNCHER, "Moto App Launcher")
        set(value) = setString(KEY_PRIMARY_LAUNCHER, value)
    
    var lockPhoneScreen: Boolean
        get() = getBoolean(KEY_LOCK_PHONE_SCREEN, false)
        set(value) = setBoolean(KEY_LOCK_PHONE_SCREEN, value)
    
    // Advanced
    var keyboardShortcuts: Boolean
        get() = getBoolean(KEY_KEYBOARD_SHORTCUTS, true)
        set(value) = setBoolean(KEY_KEYBOARD_SHORTCUTS, value)
    
    var widgetSupport: Boolean
        get() = getBoolean(KEY_WIDGET_SUPPORT, true)
        set(value) = setBoolean(KEY_WIDGET_SUPPORT, value)
    
    var dashboardSize: String
        get() = getString(KEY_DASHBOARD_SIZE, "2 x 2")
        set(value) = setString(KEY_DASHBOARD_SIZE, value)
    
    var thirdParty: Boolean
        get() = getBoolean(KEY_THIRD_PARTY, true)
        set(value) = setBoolean(KEY_THIRD_PARTY, value)
    
    // Quick 4-window apps
    var quadApp1: String
        get() = getString(KEY_QUAD_APP_1, "")
        set(value) = setString(KEY_QUAD_APP_1, value)

    var quadApp2: String
        get() = getString(KEY_QUAD_APP_2, "")
        set(value) = setString(KEY_QUAD_APP_2, value)

    var quadApp3: String
        get() = getString(KEY_QUAD_APP_3, "")
        set(value) = setString(KEY_QUAD_APP_3, value)

    var quadApp4: String
        get() = getString(KEY_QUAD_APP_4, "")
        set(value) = setString(KEY_QUAD_APP_4, value)

    /** Coordenadas predefinidas del cuadrante (1-4): "left,top,width,height". Vacío = usar posición auto (2x2 extremos). */
    fun getQuadBounds(slot: Int): String? {
        val key = when (slot) {
            1 -> KEY_QUAD_BOUNDS_1
            2 -> KEY_QUAD_BOUNDS_2
            3 -> KEY_QUAD_BOUNDS_3
            4 -> KEY_QUAD_BOUNDS_4
            else -> return null
        }
        val s = prefs.getString(key, "") ?: ""
        return if (s.isEmpty()) null else s
    }

    fun setQuadBounds(slot: Int, value: String) {
        val key = when (slot) {
            1 -> KEY_QUAD_BOUNDS_1
            2 -> KEY_QUAD_BOUNDS_2
            3 -> KEY_QUAD_BOUNDS_3
            4 -> KEY_QUAD_BOUNDS_4
            else -> return
        }
        prefs.edit().putString(key, value).apply()
    }

    fun clearAllQuadBounds() {
        prefs.edit()
            .remove(KEY_QUAD_BOUNDS_1)
            .remove(KEY_QUAD_BOUNDS_2)
            .remove(KEY_QUAD_BOUNDS_3)
            .remove(KEY_QUAD_BOUNDS_4)
            .apply()
    }
    
    // Pinned apps
    fun getPinnedApps(): List<String> {
        val pinnedStr = prefs.getString(KEY_PINNED_APPS, "") ?: ""
        return if (pinnedStr.isEmpty()) emptyList() else pinnedStr.split(",")
    }
    
    fun setPinnedApps(apps: List<String>) {
        prefs.edit().putString(KEY_PINNED_APPS, apps.joinToString(",")).apply()
    }
    
    fun addPinnedApp(packageName: String) {
        val current = getPinnedApps().toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            setPinnedApps(current)
        }
    }
    
    fun removePinnedApp(packageName: String) {
        val current = getPinnedApps().toMutableList()
        current.remove(packageName)
        setPinnedApps(current)
    }

    // Blocked apps (para ocultar de recientes, etc.)
    fun getBlockedApps(): List<String> {
        val blockedStr = prefs.getString(KEY_BLOCKED_APPS, "") ?: ""
        return if (blockedStr.isEmpty()) emptyList() else blockedStr.split(",")
    }

    fun setBlockedApps(apps: List<String>) {
        prefs.edit().putString(KEY_BLOCKED_APPS, apps.joinToString(",")).apply()
    }

    fun addBlockedApp(packageName: String) {
        val current = getBlockedApps().toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            setBlockedApps(current)
        }
    }

    fun removeBlockedApp(packageName: String) {
        val current = getBlockedApps().toMutableList()
        current.remove(packageName)
        setBlockedApps(current)
    }
}
