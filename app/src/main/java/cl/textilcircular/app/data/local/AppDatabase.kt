package cl.textilcircular.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base de datos local: fuente de verdad de la app (RNF01, RF03.1). */
@Database(
    entities = [
        RubroEntity::class,
        SociaEntity::class,
        PublicacionEntity::class,
        FotoEntity::class,
        ConversacionEntity::class,
        MensajeEntity::class,
        NovedadEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rubroDao(): RubroDao
    abstract fun sociaDao(): SociaDao
    abstract fun publicacionDao(): PublicacionDao
    abstract fun conversacionDao(): ConversacionDao
    abstract fun novedadDao(): NovedadDao

    companion object {
        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "textil_circular.db").build()
    }
}
