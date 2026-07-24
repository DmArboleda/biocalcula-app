package com.arboleda.biocalcula.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arboleda.biocalcula.data.model.CumplimientoDiario

@Dao
interface CumplimientoDiarioDao {

    /**
     * Inserta o reemplaza el cumplimiento del día.
     * Si ya existe un registro para esa fecha, lo sobreescribe.
     * Nota: esto usa la PRIMARY KEY autoincremental, por lo que duplica por fecha.
     * Usamos la query de upsert manual para evitar duplicados.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cumplimiento: CumplimientoDiario)

    /**
     * Obtiene el registro de cumplimiento para una fecha específica (formato "yyyy-MM-dd").
     * Retorna null si el usuario no guardó nada ese día.
     */
    @Query("SELECT * FROM cumplimiento_diario WHERE fecha = :fecha LIMIT 1")
    suspend fun obtenerPorFecha(fecha: String): CumplimientoDiario?

    /**
     * Obtiene los registros de los últimos 7 días para el reporte semanal.
     * Se ordenan descendente para procesar el más reciente primero.
     */
    @Query("""
        SELECT * FROM cumplimiento_diario 
        WHERE fecha >= :fechaInicio AND fecha <= :fechaFin
        ORDER BY fecha DESC
    """)
    suspend fun obtenerEntreFechas(fechaInicio: String, fechaFin: String): List<CumplimientoDiario>

    /**
     * Elimina o actualiza el registro de hoy (para hacer upsert manual por fecha).
     */
    @Query("DELETE FROM cumplimiento_diario WHERE fecha = :fecha")
    suspend fun eliminarPorFecha(fecha: String)
}
