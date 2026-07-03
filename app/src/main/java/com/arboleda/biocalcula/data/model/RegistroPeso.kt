package com.arboleda.biocalcula.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registro_peso")
data class RegistroPeso(
    @PrimaryKey(autoGenerate = true)
    val id_registro: Int = 0,
    val peso_registrado: Float,
    val fecha_registro: String
)
