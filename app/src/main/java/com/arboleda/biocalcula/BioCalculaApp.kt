package com.arboleda.biocalcula

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.app.Application

/**
 * Clase Application de BioCalcula.
 *
 * Se ejecuta antes que cualquier Activity. Aquí registramos los dos canales
 * de notificaciones requeridos desde Android 8.0 (Oreo / API 26):
 *
 *  - CHANNEL_RUTINA: recordatorio diario de macros + agua (hora nocturna configurable)
 *  - CHANNEL_PESO:   recordatorio semanal de pesaje (intervalo configurable)
 *
 * ¿Por qué dos canales separados? El usuario puede controlarlos de forma
 * independiente desde los ajustes del sistema (silenciar uno sin afectar el otro).
 */
class BioCalculaApp : Application() {

    companion object {
        /** Canal para el recordatorio nocturno diario (macros + agua). */
        const val CHANNEL_RUTINA = "biocalcula_rutina"

        /** Canal para el recordatorio semanal de pesaje. */
        const val CHANNEL_PESO = "biocalcula_peso"

        /** @deprecated Usar CHANNEL_RUTINA o CHANNEL_PESO según el tipo. */
        const val CHANNEL_ID = CHANNEL_RUTINA
    }

    override fun onCreate() {
        super.onCreate()
        crearCanalesNotificaciones()
    }

    private fun crearCanalesNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal 1: Rutina diaria de macros y agua
            NotificationChannel(
                CHANNEL_RUTINA,
                "Rutina diaria de macros",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorio nocturno para registrar tu consumo de macros y agua del día"
                manager.createNotificationChannel(this)
            }

            // Canal 2: Pesaje semanal
            NotificationChannel(
                CHANNEL_PESO,
                "Recordatorio de peso",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorio periódico para registrar tu peso y ajustar tus macros"
                manager.createNotificationChannel(this)
            }
        }
    }
}
