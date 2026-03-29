package com.taskbar.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Servicio mínimo para que Android muestre la app
 * en el panel de "Acceso a notificaciones".
 * Más adelante se puede ampliar para contar notificaciones, etc.
 */
class TaskbarNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Por ahora no hacemos nada; solo necesitamos estar registrados.
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Igual que arriba.
    }
}

