package com.arboleda.biocalcula.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.arboleda.biocalcula.data.model.Usuario

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertarUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuario WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun login(correo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM usuario WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?
}
