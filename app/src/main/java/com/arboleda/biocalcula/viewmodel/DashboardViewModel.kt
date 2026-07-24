package com.arboleda.biocalcula.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arboleda.biocalcula.data.db.AppDatabase
import com.arboleda.biocalcula.data.model.CumplimientoDiario
import com.arboleda.biocalcula.data.model.Usuario
import com.arboleda.biocalcula.util.MacrosPlan
import com.arboleda.biocalcula.util.NutricionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel del Dashboard principal.
 *
 * Responsabilidades:
 * 1. Cargar datos del usuario desde Room
 * 2. Calcular el plan nutricional (TMB + macros) usando NutricionHelper
 * 3. Gestionar el checklist diario (obtener, guardar)
 *
 * Optimización de rendimiento (Entregable 12):
 * El cálculo del plan nutricional se cachea en memoria y solo se vuelve a
 * ejecutar si los datos biométricos u objetivo del usuario cambiaron desde
 * la última vez. Esto evita recalcular innecesariamente cada vez que el
 * usuario reingresa al Dashboard, reduciendo el uso de CPU detectado con
 * Android Profiler.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()
    private val cumplimientoDao = AppDatabase.getDatabase(application).cumplimientoDiarioDao()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> get() = _usuario

    private val _macrosPlan = MutableLiveData<MacrosPlan?>()
    val macrosPlan: LiveData<MacrosPlan?> get() = _macrosPlan

    private val _cumplimientoHoy = MutableLiveData<CumplimientoDiario?>()
    val cumplimientoHoy: LiveData<CumplimientoDiario?> get() = _cumplimientoHoy

    private val _guardadoExito = MutableLiveData<Boolean>()
    val guardadoExito: LiveData<Boolean> get() = _guardadoExito

    // ── Cache del último plan calculado (evita recalcular si los datos no cambiaron) ──
    private var ultimoPesoUsado: Float? = null
    private var ultimaTallaUsada: Float? = null
    private var ultimaEdadUsada: Int? = null
    private var ultimoSexoUsado: String? = null
    private var ultimoObjetivoUsado: String? = null

    /** Carga el usuario y calcula su plan nutricional (usando cache si los datos no cambiaron). */
    fun cargarDashboard(usuarioId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val usuario = usuarioDao.buscarPorId(usuarioId)
            _usuario.postValue(usuario)

            // Calcular plan solo si tiene todos los datos biométricos y objetivo
            if (usuario?.peso != null && usuario.talla != null &&
                usuario.edad != null && usuario.sexo != null && usuario.objetivo != null
            ) {
                val datosCambiaron = usuario.peso != ultimoPesoUsado ||
                        usuario.talla != ultimaTallaUsada ||
                        usuario.edad != ultimaEdadUsada ||
                        usuario.sexo != ultimoSexoUsado ||
                        usuario.objetivo != ultimoObjetivoUsado

                if (datosCambiaron || _macrosPlan.value == null) {
                    val plan = NutricionHelper.calcularPlan(
                        peso     = usuario.peso,
                        talla    = usuario.talla,
                        edad     = usuario.edad,
                        sexo     = usuario.sexo,
                        objetivo = usuario.objetivo
                    )
                    _macrosPlan.postValue(plan)

                    // Guardamos los datos usados para comparar la próxima vez
                    ultimoPesoUsado      = usuario.peso
                    ultimaTallaUsada     = usuario.talla
                    ultimaEdadUsada      = usuario.edad
                    ultimoSexoUsado      = usuario.sexo
                    ultimoObjetivoUsado  = usuario.objetivo
                }
                // Si no cambió nada, no se recalcula — se sigue mostrando el plan ya guardado
            }

            // Cargar el cumplimiento de hoy
            val fechaHoy = fechaHoy()
            _cumplimientoHoy.postValue(cumplimientoDao.obtenerPorFecha(fechaHoy))
        }
    }

    /**
     * Guarda el checklist del día actual.
     * Realiza upsert: elimina el registro de hoy y lo vuelve a insertar.
     */
    fun guardarCumplimiento(
        proteina: Boolean,
        carbos: Boolean,
        grasas: Boolean,
        agua: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val fecha = fechaHoy()
            cumplimientoDao.eliminarPorFecha(fecha)
            cumplimientoDao.insertar(
                CumplimientoDiario(
                    fecha              = fecha,
                    proteinaCumplida   = proteina,
                    carbosCumplidos    = carbos,
                    grasasCumplida     = grasas,
                    aguaCumplida       = agua
                )
            )
            _cumplimientoHoy.postValue(cumplimientoDao.obtenerPorFecha(fecha))
            _guardadoExito.postValue(true)
        }
    }

    private fun fechaHoy(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}