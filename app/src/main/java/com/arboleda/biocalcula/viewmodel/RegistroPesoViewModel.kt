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

    private val registroDao = AppDatabase.getDatabase(application).registroPesoDao()

    fun getRegistroPesoById(id: Int): LiveData<RegistroPeso?> {
        return registroDao.getById(id)
    }

    fun insertar(registro: RegistroPeso) {
        viewModelScope.launch(Dispatchers.IO) {
            registroDao.insert(registro)
        }
    }

    fun actualizar(registro: RegistroPeso) {
        viewModelScope.launch(Dispatchers.IO) {
            registroDao.update(registro)
        }
    }
}
