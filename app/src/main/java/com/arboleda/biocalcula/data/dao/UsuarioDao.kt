package com.arboleda.biocalcula.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.arboleda.biocalcula.data.model.Usuario

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertarUsuario(usuario: Usuario): Long

    @Update
    suspend fun actualizarUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuario WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun login(correo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM usuario WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id_usuario = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Usuario?

    /**
     * Actualiza los datos biométricos del usuario.
     * Se llama desde DatosbiometricosActivity al guardar el formulario.
     */
    @Query("""
        UPDATE usuario 
        SET peso = :peso, talla = :talla, edad = :edad, sexo = :sexo 
        WHERE id_usuario = :id
    """)
    suspend fun actualizarDatosBiometricos(id: Int, peso: Float, talla: Float, edad: Int, sexo: String)

    /**
     * Actualiza solo el peso (usado desde PerfilActivity cuando el usuario registra un nuevo peso).
     */
    @Query("UPDATE usuario SET peso = :peso WHERE id_usuario = :id")
    suspend fun actualizarPeso(id: Int, peso: Float)

    /**
     * Guarda el objetivo elegido en ObjetivoActivity.
     */
    @Query("UPDATE usuario SET objetivo = :objetivo WHERE id_usuario = :id")
    suspend fun actualizarObjetivo(id: Int, objetivo: String)
}
