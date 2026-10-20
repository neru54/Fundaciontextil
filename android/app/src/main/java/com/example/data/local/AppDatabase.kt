package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AnnouncementDao
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.PartnerDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.ChatEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.PartnerEntity
import com.example.data.local.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        AnnouncementEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        PartnerEntity::class,
        OrderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun chatDao(): ChatDao
    abstract fun partnerDao(): PartnerDao
    abstract fun orderDao(): OrderDao

    companion object {
        private const val DB_NAME = "textil_circular.db"

        /** v1 → v2: socias, pedidos y motivo de postulación, sin perder datos. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `partners` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `rut` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `comuna` TEXT NOT NULL,
                        `workshopName` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `passwordSalt` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_partners_email` ON `partners` (`email`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_partners_rut` ON `partners` (`rut`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `orders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `buyerName` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `comuna` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `total` INTEGER NOT NULL,
                        `itemCount` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE `announcements` ADD COLUMN `applicationMotivation` TEXT")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
