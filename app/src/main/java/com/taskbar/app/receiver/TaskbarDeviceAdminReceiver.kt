package com.taskbar.app.receiver

import android.app.admin.DeviceAdminReceiver

/**
 * Receiver mínimo para que la app pueda aparecer
 * en la lista de "Administradores del dispositivo".
 * Las políticas reales (bloqueo de pantalla, etc.) se pueden añadir después.
 */
class TaskbarDeviceAdminReceiver : DeviceAdminReceiver()

