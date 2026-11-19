package com.example.bremautomotriz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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

        // v1 -> v2: se elimina la columna "neto". El IVA es solo informativo y el
        // total de la boleta es la suma de lo cobrado. Se reconstruyen las tablas
        // relacionadas para no perder líneas ni fotos de las órdenes existentes.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE `tmp_ordenes_trabajo` AS SELECT * FROM `ordenes_trabajo`")
                db.execSQL("CREATE TABLE `tmp_lineas_ot` AS SELECT * FROM `lineas_ot`")
                db.execSQL("CREATE TABLE `tmp_evidencias_ot` AS SELECT * FROM `evidencias_ot`")

                db.execSQL("DROP TABLE `lineas_ot`")
                db.execSQL("DROP TABLE `evidencias_ot`")
                db.execSQL("DROP TABLE `ordenes_trabajo`")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `ordenes_trabajo` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `vehiculoId` INTEGER NOT NULL,
                        `patente` TEXT NOT NULL,
                        `nombreCliente` TEXT NOT NULL,
                        `fechaIngreso` INTEGER NOT NULL,
                        `fechaRegistro` INTEGER NOT NULL,
                        `kilometraje` INTEGER NOT NULL,
                        `observacionIngreso` TEXT NOT NULL,
                        `observacionEntrega` TEXT NOT NULL,
                        `estado` TEXT NOT NULL,
                        `iva` INTEGER NOT NULL,
                        `total` INTEGER NOT NULL,
                        FOREIGN KEY(`vehiculoId`) REFERENCES `vehiculos`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ordenes_trabajo_vehiculoId` ON `ordenes_trabajo` (`vehiculoId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `lineas_ot` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `otId` INTEGER NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `costo` INTEGER NOT NULL,
                        `factor` REAL NOT NULL,
                        `cobrado` INTEGER NOT NULL,
                        FOREIGN KEY(`otId`) REFERENCES `ordenes_trabajo`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lineas_ot_otId` ON `lineas_ot` (`otId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `evidencias_ot` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `otId` INTEGER NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `uri` TEXT NOT NULL,
                        `fecha` INTEGER NOT NULL,
                        FOREIGN KEY(`otId`) REFERENCES `ordenes_trabajo`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_evidencias_ot_otId` ON `evidencias_ot` (`otId`)")

                db.execSQL(
                    """
                    INSERT INTO `ordenes_trabajo`
                        (`id`, `vehiculoId`, `patente`, `nombreCliente`, `fechaIngreso`,
                         `fechaRegistro`, `kilometraje`, `observacionIngreso`,
                         `observacionEntrega`, `estado`, `iva`, `total`)
                    SELECT `id`, `vehiculoId`, `patente`, `nombreCliente`, `fechaIngreso`,
                           `fechaRegistro`, `kilometraje`, `observacionIngreso`,
                           `observacionEntrega`, `estado`, `iva`, `total`
                    FROM `tmp_ordenes_trabajo`
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `lineas_ot`
                        (`id`, `otId`, `tipo`, `descripcion`, `costo`, `factor`, `cobrado`)
                    SELECT `id`, `otId`, `tipo`, `descripcion`, `costo`, `factor`, `cobrado`
                    FROM `tmp_lineas_ot`
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `evidencias_ot` (`id`, `otId`, `tipo`, `uri`, `fecha`)
                    SELECT `id`, `otId`, `tipo`, `uri`, `fecha` FROM `tmp_evidencias_ot`
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE `tmp_ordenes_trabajo`")
                db.execSQL("DROP TABLE `tmp_lineas_ot`")
                db.execSQL("DROP TABLE `tmp_evidencias_ot`")
            }
        }

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brem.db"
                ).addMigrations(MIGRATION_1_2).build().also { instancia = it }
            }
        }
    }
}
