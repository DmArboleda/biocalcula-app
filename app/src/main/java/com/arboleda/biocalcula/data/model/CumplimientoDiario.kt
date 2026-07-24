package com.arboleda.biocalcula.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para el checklist diario de cumplimiento de macronutrientes.
 *
 * Es diferente de RegistroPeso:
 * - RegistroPeso → historial de pesaje semanal (el número en kg)
 * - CumplimientoDiario → si el usuario cumplió cada meta nutricional ese día (checkboxes)
 *
 * La fecha se almacena como String en formato "yyyy-MM-dd" para facilitar
 * las queries de los últimos 7 días y el reporte semanal.
 */
@Entity(tableName = "cumplimiento_diario")
data class CumplimientoDiario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: String,                          // "2025-07-05"

    // ── Cumplimiento boolean (checklist original) ───────────────────────────
    val proteinaCumplida: Boolean = false,
    val carbosCumplidos: Boolean = false,
    val grasasCumplida: Boolean = false,
    val aguaCumplida: Boolean = false,

    // ── Consumo real ingresado desde notificación ───────────────────────────
    // null = no registrado (solo marcó checkbox); valor = ingresó cantidad real
    val proteinaGramosConsumidos: Float? = null,
    val carbosGramosConsumidos: Float? = null,
    val grasasGramosConsumidos: Float? = null,
    val aguaVasosConsumidos: Int? = null        // 1 vaso = 250 ml
)
