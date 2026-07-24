package com.arboleda.biocalcula.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestión de sesión del usuario usando SharedPreferences.
 *
 * Persiste entre aperturas de la app:
 * - usuario_id: Int — ID del usuario logueado (-1 si no hay sesión)
 * - onboarding_completo: Boolean — si ya completó biométricos + objetivo
 *
 * Uso típico:
 *   val sm = SessionManager(context)
 *   sm.guardarSesion(usuario.id_usuario)
 *   sm.marcarOnboardingCompleto()
 *   val id = sm.obtenerUserId()  // -1 si no hay sesión
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "biocalcula_session"
        private const val KEY_USER_ID = "usuario_id"
        private const val KEY_ONBOARDING = "onboarding_completo"
        private const val SIN_SESION = -1
    }

    /** Guarda el ID del usuario tras login o registro exitoso. */
    fun guardarSesion(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    /** Retorna el ID del usuario logueado, o -1 si no hay sesión activa. */
    fun obtenerUserId(): Int = prefs.getInt(KEY_USER_ID, SIN_SESION)

    /** Verifica si hay una sesión activa (usuario logueado). */
    fun haySesion(): Boolean = obtenerUserId() != SIN_SESION

    /** Marca que el usuario completó el flujo de onboarding (biométricos + objetivo). */
    fun marcarOnboardingCompleto() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
    }

    /** Retorna true si el usuario ya completó el onboarding (tiene biométricos + objetivo). */
    fun isOnboardingCompleto(): Boolean = prefs.getBoolean(KEY_ONBOARDING, false)

    /** Cierra la sesión y limpia todos los datos guardados. */
    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
