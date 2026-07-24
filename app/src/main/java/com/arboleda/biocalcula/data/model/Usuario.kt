package com.arboleda.biocalcula.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad principal del usuario.
 * Almacena datos de autenticación + datos biométricos + objetivo nutricional.
 *
 * Los campos biométricos son nullable porque se completan en DatosbiometricosActivity
 * después del registro (flujo de onboarding).
 */
@Entity(tableName = "usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id_usuario: Int = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,

    // Datos biométricos — se llenan en Pantalla 1 (DatosbiometricosActivity)
    val peso: Float? = null,
    val talla: Float? = null,
    val edad: Int? = null,
    val sexo: String? = null,       // "Masculino" o "Femenino"

    // Objetivo nutricional — se elige en Pantalla 2 (ObjetivoActivity)
    val objetivo: String? = null    // "PERDER_GRASA", "MANTENER", "GANAR_MUSCULO"
)
