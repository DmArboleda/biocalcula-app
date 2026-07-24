package com.arboleda.biocalcula

/**
 * ValidadorHelper
 *
 * Objeto utilitario que centraliza la lógica de validación y reglas de negocio
 * de BioCalcula. Al ser funciones puras (sin dependencias de Android), pueden
 * ser probadas fácilmente con pruebas unitarias JUnit sin necesidad de un emulador.
 *
 * Corresponde a la lógica extraída de LoginActivity, RegisterActivity y
 * FormularioPesoActivity.
 */
object ValidadorHelper {

    // =========================================================================
    // PRUEBA 1 — Validación de correo electrónico
    // Replica la misma regex usada en LoginActivity y RegisterActivity.
    // El patrón exige: caracteres locales @ dominio . extensión (mínimo 2 letras)
    // =========================================================================
    fun validarCorreo(correo: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}\$".toRegex()
        return correo.matches(regex)
    }

    // =========================================================================
    // PRUEBA 2 — Validación de contraseña
    // Replica la restricción de >= 6 caracteres usada en LoginActivity (línea 68)
    // y RegisterActivity (línea 62).
    // =========================================================================
    fun validarContrasena(password: String): Boolean {
        return password.length >= 6
    }

    // =========================================================================
    // PRUEBA 3 — Lógica de negocio: clasificación del peso registrado
    // Un peso registrado debe ser un número positivo mayor que cero.
    // Devuelve la categoría clínica de IMC cuando también se provee la altura,
    // pero aquí validamos si el valor de peso ingresado es coherente (> 0 y < 500).
    // Esto representa la lógica central del CRUD de RegistroPeso en BioCalcula.
    // =========================================================================
    fun validarPesoRegistrado(peso: Float): Boolean {
        return peso > 0f && peso < 500f
    }

    /**
     * Calcula la categoría de IMC basada en los rangos clínicos de la OMS.
     * @param imc Índice de Masa Corporal (peso_kg / estatura_m²)
     * @return Categoría como String: "Invalido", "Bajo Peso", "Normal",
     *         "Sobrepeso" u "Obesidad".
     */
    fun calcularEstadoImc(imc: Double): String {
        return when {
            imc <= 0  -> "Invalido"
            imc < 18.5             -> "Bajo Peso"
            imc in 18.5..24.9      -> "Normal"
            imc in 25.0..29.9      -> "Sobrepeso"
            else                   -> "Obesidad"
        }
    }
}
