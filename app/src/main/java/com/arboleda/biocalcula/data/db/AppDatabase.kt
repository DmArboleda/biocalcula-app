package com.arboleda.biocalcula.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arboleda.biocalcula.data.dao.CumplimientoDiarioDao
import com.arboleda.biocalcula.data.dao.RegistroPesoDao
import com.arboleda.biocalcula.data.dao.UsuarioDao
import com.arboleda.biocalcula.data.model.CumplimientoDiario
import com.arboleda.biocalcula.data.model.RegistroPeso
import com.arboleda.biocalcula.data.model.Usuario

/**
 * Base de datos principal de BioCalcula.
 *
 * Versión 3: Se agregan campos biométricos a Usuario y nueva tabla cumplimiento_diario.
 * fallbackToDestructiveMigration() limpia la BD al cambiar versión (aceptable en dev).
 *
 * Tablas:
 * - usuario: datos de cuenta + biométricos + objetivo
 * - registro_peso: historial de pesajes semanales (con fecha)
 * - cumplimiento_diario: checklist diario de macros (proteínas, carbos, grasas, agua)
 */
@Database(
    entities = [Usuario::class, RegistroPeso::class, CumplimientoDiario::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun registroPesoDao(): RegistroPesoDao
    abstract fun cumplimientoDiarioDao(): CumplimientoDiarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "biocalcula_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
