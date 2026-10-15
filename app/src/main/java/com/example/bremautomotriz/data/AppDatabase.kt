package com.example.bremautomotriz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bremautomotriz.data.dao.ClienteDao
import com.example.bremautomotriz.data.dao.EvidenciaOtDao
import com.example.bremautomotriz.data.dao.LineaOtDao
import com.example.bremautomotriz.data.dao.OrdenTrabajoDao
import com.example.bremautomotriz.data.dao.UsuarioDao
import com.example.bremautomotriz.data.dao.VehiculoDao
import com.example.bremautomotriz.data.entity.Cliente
import com.example.bremautomotriz.data.entity.EvidenciaOt
import com.example.bremautomotriz.data.entity.LineaOt
import com.example.bremautomotriz.data.entity.OrdenTrabajo
import com.example.bremautomotriz.data.entity.Usuario
import com.example.bremautomotriz.data.entity.Vehiculo

@Database(
    entities = [
        Usuario::class,
        Cliente::class,
        Vehiculo::class,
        OrdenTrabajo::class,
        LineaOt::class,
        EvidenciaOt::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun ordenTrabajoDao(): OrdenTrabajoDao
    abstract fun lineaOtDao(): LineaOtDao
    abstract fun evidenciaOtDao(): EvidenciaOtDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brem.db"
                ).build().also { instancia = it }
            }
        }
    }
}