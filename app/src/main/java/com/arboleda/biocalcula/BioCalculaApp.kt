package com.arboleda.biocalcula

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.app.Application

/**
 * Clase Application de BioCalcula.
 *
 * Se ejecuta antes que cualquier Activity. Aquí registramos el canal de
 * notificaciones requerido desde Android 8.0 (Oreo / API 26).
 *
 * ¿Por qué canales? Google los introdujo en Android 8 para que el usuario
 * pueda controlar finamente qué tipos de alertas desea ver de cada app,
 * sin tener que silenciarla completamente.
 */
class BioCalculaApp : Application() {

    companion object {
        const val CHANNEL_ID = "biocalcula_recordatorios"
    }

    override fun onCreate() {
        super.onCreate()
        crearCanalNotificaciones()
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Recordatorios de Progreso"
            val descripcion = "Canal para alertas de pesaje semanal y metas nutricionales"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT

            val canal = NotificationChannel(CHANNEL_ID, nombre, importancia).apply {
                description = descripcion
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }
}
