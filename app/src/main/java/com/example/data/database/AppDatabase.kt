package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CustomStockDps::class,
        SymbolEntity::class,
        AdjustedPriceEntity::class,
        DividendEntity::class,
        CorporateActionEntity::class,
        QuantMetricsEntity::class,
        DiscrepancyLogEntity::class,
        MarketDataHistoryEntity::class,
        DerivedMetricsHistoryEntity::class,
        CorporateEvent::class,
        ShareStructureHistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customStockDpsDao(): CustomStockDpsDao
    abstract fun quantDao(): QuantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tse_vip_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
