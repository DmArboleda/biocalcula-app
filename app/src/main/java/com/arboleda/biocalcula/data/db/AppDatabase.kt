package com.arboleda.biocalcula.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arboleda.biocalcula.data.dao.RegistroPesoDao
import com.arboleda.biocalcula.data.dao.UsuarioDao
import com.arboleda.biocalcula.data.model.RegistroPeso
import com.arboleda.biocalcula.data.model.Usuario

@Database(
    entities = [Usuario::class, RegistroPeso::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun registroPesoDao(): RegistroPesoDao

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
