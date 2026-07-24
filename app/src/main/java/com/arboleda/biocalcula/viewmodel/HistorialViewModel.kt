package com.arboleda.biocalcula.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arboleda.biocalcula.data.db.AppDatabase
import com.arboleda.biocalcula.data.model.CumplimientoDiario
import com.arboleda.biocalcula.data.model.RegistroPeso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Data class para el reporte semanal de cumplimiento.
 * Cada campo indica cuántos días (de 7) cumplió con esa meta.
 */
data class ReporteSemanal(
    val diasProteina: Int,
    val diasCarbos: Int,
    val diasGrasas: Int,
    val diasAgua: Int,
    val totalDiasEvaluados: Int
) {
    val porcentajeGeneral: Int
        get() {
            if (totalDiasEvaluados == 0) return 0
            val totalPosible = totalDiasEvaluados * 4
            val totalCumplido = diasProteina + diasCarbos + diasGrasas + diasAgua
            return (totalCumplido * 100 / totalPosible)
        }
}

/**
 * ViewModel para la pantalla de Historial.
 * Combina el historial de pesajes (RecyclerView) y el reporte semanal (BarChart).
 */
class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val registroDao = AppDatabase.getDatabase(application).registroPesoDao()
    private val cumplimientoDao = AppDatabase.getDatabase(application).cumplimientoDiarioDao()

    // Lista de pesajes — ya funcional (LiveData directo de Room)
    val todosLosRegistros: LiveData<List<RegistroPeso>> = registroDao.getAll()

    private val _reporteSemanal = MutableLiveData<ReporteSemanal?>()
    val reporteSemanal: LiveData<ReporteSemanal?> get() = _reporteSemanal

    fun insertar(registro: RegistroPeso) {
        viewModelScope.launch(Dispatchers.IO) { registroDao.insert(registro) }
    }

    fun eliminar(registro: RegistroPeso) {
        viewModelScope.launch(Dispatchers.IO) { registroDao.delete(registro) }
    }

    /**
     * Calcula el reporte semanal consultando los cumplimientos de los últimos 7 días.
     * Llama a Room en background y expone el resultado por LiveData.
     */
    fun cargarReporteSemanal() {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()

            val fechaFin = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -6)
            val fechaInicio = sdf.format(cal.time)

            val registros: List<CumplimientoDiario> =
                cumplimientoDao.obtenerEntreFechas(fechaInicio, fechaFin)

            if (registros.isEmpty()) {
                _reporteSemanal.postValue(null)
                return@launch
            }

            val reporte = ReporteSemanal(
                diasProteina         = registros.count { it.proteinaCumplida },
                diasCarbos           = registros.count { it.carbosCumplidos },
                diasGrasas           = registros.count { it.grasasCumplida },
                diasAgua             = registros.count { it.aguaCumplida },
                totalDiasEvaluados   = registros.size
            )
            _reporteSemanal.postValue(reporte)
        }
    }
}
