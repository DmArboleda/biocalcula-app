package com.arboleda.biocalcula.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.arboleda.biocalcula.data.db.AppDatabase
import com.arboleda.biocalcula.data.model.RegistroPeso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegistroPesoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).registroPesoDao()

    val todosLosRegistros: LiveData<List<RegistroPeso>> = dao.getAll()

    fun insertar(registro: RegistroPeso) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(registro)
        }
    }

    fun actualizar(registro: RegistroPeso) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(registro)
        }
    }

    fun eliminar(registro: RegistroPeso) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(registro)
        }
    }

    fun getRegistroPesoById(id: Int): LiveData<RegistroPeso?> = dao.getById(id)
}
