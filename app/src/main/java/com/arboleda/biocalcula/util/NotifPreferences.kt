package com.arboleda.biocalcula.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Preferencias de notificaciones de BioCalcula.
 *
 * Almacena configuración de los dos recordatorios:
 *  - Recordatorio de rutina diaria (macros + agua) — hora nocturna
 *  - Recordatorio de pesaje semanal                — intervalo en días
 *
 * Uso típico:
 *   val prefs = NotifPreferences(context)
 *   prefs.rutinaActiva = true
 *   prefs.rutinaHora = 21
 *   prefs.pesoActivo = true
 *   prefs.pesoIntervalosDias = 7
 */
class NotifPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "biocalcula_notif"

        // Claves — rutina diaria
        private const val KEY_RUTINA_ACTIVA  = "rutina_activa"
        private const val KEY_RUTINA_HORA    = "rutina_hora"
        private const val KEY_RUTINA_MINUTO  = "rutina_minuto"

        // Claves — pesaje semanal
        private const val KEY_PESO_ACTIVO    = "peso_activo"
        private const val KEY_PESO_DIAS      = "peso_dias"

        // Valores por defecto
        const val DEFAULT_HORA    = 21   // 9 PM
        const val DEFAULT_MINUTO  = 0
        const val DEFAULT_DIAS    = 7    // semanal
    }

    // ── Recordatorio rutina diaria ──────────────────────────────────────────

    /** Si el recordatorio nocturno de rutina está activado. */
    var rutinaActiva: Boolean
        get() = prefs.getBoolean(KEY_RUTINA_ACTIVA, false)
        set(value) = prefs.edit().putBoolean(KEY_RUTINA_ACTIVA, value).apply()

    /** Hora (0–23) en que se envía el recordatorio diario. */
    var rutinaHora: Int
        get() = prefs.getInt(KEY_RUTINA_HORA, DEFAULT_HORA)
        set(value) = prefs.edit().putInt(KEY_RUTINA_HORA, value).apply()

    /** Minuto (0–59) del recordatorio diario. */
    var rutinaMinuto: Int
        get() = prefs.getInt(KEY_RUTINA_MINUTO, DEFAULT_MINUTO)
        set(value) = prefs.edit().putInt(KEY_RUTINA_MINUTO, value).apply()

    /** Devuelve la hora formateada "HH:MM" */
    fun horaFormateada(): String =
        "%02d:%02d".format(rutinaHora, rutinaMinuto)

    // ── Recordatorio pesaje semanal ─────────────────────────────────────────

    /** Si el recordatorio de pesaje está activado. */
    var pesoActivo: Boolean
        get() = prefs.getBoolean(KEY_PESO_ACTIVO, false)
        set(value) = prefs.edit().putBoolean(KEY_PESO_ACTIVO, value).apply()

    /** Intervalo en días entre recordatorios de peso (ej: 7 = semanal). */
    var pesoIntervalosDias: Int
        get() = prefs.getInt(KEY_PESO_DIAS, DEFAULT_DIAS)
        set(value) = prefs.edit().putInt(KEY_PESO_DIAS, value).apply()
}
