package com.arboleda.biocalcula.util

/**
 * Resultado del cálculo nutricional personalizado.
 * Contiene todas las metas diarias del usuario.
 */
data class MacrosPlan(
    val caloriasObjetivo: Int,
    val proteinaG: Int,
    val carbosG: Int,
    val grasasG: Int,
    val aguaMl: Int,
    val proteinaPct: Int,
    val carbosPct: Int,
    val grasasPct: Int
) {
    /** Agua en litros con 1 decimal (ej: 2.6L). */
    val aguaL: Float get() = aguaMl / 1000f
}

/**
 * Clase utilitaria para cálculos nutricionales.
 *
 * Implementa la fórmula de Mifflin-St Jeor para calcular la Tasa Metabólica Basal (TMB):
 *   Hombre: (10 × peso) + (6.25 × talla) − (5 × edad) + 5
 *   Mujer:  (10 × peso) + (6.25 × talla) − (5 × edad) − 161
 *
 * Distribución de macros según objetivo:
 *   Perder grasa:    40% proteína / 35% carbos / 25% grasas
 *   Mantener peso:   30% proteína / 45% carbos / 25% grasas
 *   Ganar músculo:   35% proteína / 45% carbos / 20% grasas
 *
 * Conversión kcal → gramos:
 *   Proteínas: 4 kcal/g | Carbohidratos: 4 kcal/g | Grasas: 9 kcal/g
 *
 * Agua: 35 ml × peso en kg
 */
object NutricionHelper {

    // Constantes de objetivo
    const val PERDER_GRASA  = "PERDER_GRASA"
    const val MANTENER      = "MANTENER"
    const val GANAR_MUSCULO = "GANAR_MUSCULO"

    // Constantes de sexo
    const val MASCULINO = "Masculino"
    const val FEMENINO  = "Femenino"

    /**
     * Calcula el plan nutricional completo para el usuario.
     *
     * @param peso   Peso en kg
     * @param talla  Talla en cm
     * @param edad   Edad en años
     * @param sexo   "Masculino" o "Femenino"
     * @param objetivo "PERDER_GRASA", "MANTENER" o "GANAR_MUSCULO"
     */
    fun calcularPlan(
        peso: Float,
        talla: Float,
        edad: Int,
        sexo: String,
        objetivo: String
    ): MacrosPlan {
        // 1. Calcular TMB con Mifflin-St Jeor
        val tmb = if (sexo == MASCULINO) {
            (10.0 * peso) + (6.25 * talla) - (5.0 * edad) + 5.0
        } else {
            (10.0 * peso) + (6.25 * talla) - (5.0 * edad) - 161.0
        }

        // 2. Ajustar calorías según objetivo
        val caloriasObjetivo = when (objetivo) {
            PERDER_GRASA  -> (tmb - 300).toInt()
            GANAR_MUSCULO -> (tmb + 300).toInt()
            else          -> tmb.toInt()  // MANTENER
        }

        // 3. Determinar distribución de macros (%)
        val (proteinaPct, carbosPct, grasasPct) = when (objetivo) {
            PERDER_GRASA  -> Triple(40, 35, 25)
            MANTENER      -> Triple(30, 45, 25)
            else          -> Triple(35, 45, 20)  // GANAR_MUSCULO
        }

        // 4. Convertir kcal → gramos
        val proteinaG = (caloriasObjetivo * proteinaPct / 100) / 4
        val carbosG   = (caloriasObjetivo * carbosPct   / 100) / 4
        val grasasG   = (caloriasObjetivo * grasasPct   / 100) / 9

        // 5. Calcular agua diaria
        val aguaMl = (peso * 35).toInt()

        return MacrosPlan(
            caloriasObjetivo = caloriasObjetivo,
            proteinaG        = proteinaG,
            carbosG          = carbosG,
            grasasG          = grasasG,
            aguaMl           = aguaMl,
            proteinaPct      = proteinaPct,
            carbosPct        = carbosPct,
            grasasPct        = grasasPct
        )
    }

    /** Retorna el emoji y etiqueta del objetivo para mostrar en UI. */
    fun etiquetaObjetivo(objetivo: String): String = when (objetivo) {
        PERDER_GRASA  -> "🔥 Perder Grasa"
        MANTENER      -> "⚖️ Mantener Peso"
        GANAR_MUSCULO -> "💪 Ganar Músculo"
        else          -> "Sin objetivo"
    }
}
