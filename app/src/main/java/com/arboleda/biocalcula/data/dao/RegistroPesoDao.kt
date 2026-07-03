package com.arboleda.biocalcula.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.arboleda.biocalcula.data.model.RegistroPeso

@Dao
interface RegistroPesoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registro: RegistroPeso)

    @Query("SELECT * FROM registro_peso ORDER BY fecha_registro DESC")
    fun getAll(): LiveData<List<RegistroPeso>>

    @Update
    suspend fun update(registro: RegistroPeso)

    @Delete
    suspend fun delete(registro: RegistroPeso)

    @Query("SELECT * FROM registro_peso WHERE id_registro = :id LIMIT 1")
    fun getById(id: Int): LiveData<RegistroPeso?>

    @Query("SELECT * FROM registro_peso ORDER BY fecha_registro DESC LIMIT 1")
    suspend fun getUltimoRegistro(): RegistroPeso?
}
