package com.arboleda.biocalcula.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arboleda.biocalcula.data.db.AppDatabase
import com.arboleda.biocalcula.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel para operaciones del perfil de usuario.
 * Carga y actualiza datos biométricos, objetivo y peso.
 */
class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).usuarioDao()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> get() = _usuario

    /** Carga el usuario desde Room por su ID (guardado en SessionManager). */
    fun cargarUsuario(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _usuario.postValue(dao.buscarPorId(id))
        }
    }

    /** Actualiza los datos biométricos del usuario. */
    fun actualizarDatosBiometricos(
        id: Int,
        peso: Float,
        talla: Float,
        edad: Int,
        sexo: String,
        onExito: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.actualizarDatosBiometricos(id, peso, talla, edad, sexo)
            _usuario.postValue(dao.buscarPorId(id))
            launch(Dispatchers.Main) { onExito() }
        }
    }

    /** Actualiza solo el objetivo nutricional. */
    fun actualizarObjetivo(id: Int, objetivo: String, onExito: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.actualizarObjetivo(id, objetivo)
            _usuario.postValue(dao.buscarPorId(id))
            launch(Dispatchers.Main) { onExito() }
        }
    }

    /** Actualiza el peso (desde PerfilActivity cuando agrega un pesaje nuevo). */
    fun actualizarPeso(id: Int, nuevoPeso: Float, onExito: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.actualizarPeso(id, nuevoPeso)
            _usuario.postValue(dao.buscarPorId(id))
            launch(Dispatchers.Main) { onExito() }
        }
    }
}
