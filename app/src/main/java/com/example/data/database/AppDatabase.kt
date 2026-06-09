package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        StockEntity::class,
        PortfolioEntity::class,
        ManualOverrideEntity::class,
        MarketCacheEntity::class,
        DividendCalendarEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tseDao(): TseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Core helper to ensure robust upgrades to version 6 by safely preparing new structures,
         * creating necessary tables if they are missing, and cleanly purging old schemas.
         */
        private fun migrateToVersion6(db: SupportSQLiteDatabase) {
            // 1. Drop tables that changed schema to prevent validation errors on startup
            db.execSQL("DROP TABLE IF EXISTS `stocks`")
            db.execSQL("DROP TABLE IF EXISTS `market_cache`")
            db.execSQL("DROP TABLE IF EXISTS `dividend_calendar`")

            // 2. Recreate schema-changed tables with safe, exact schemas matching Room Entity definitions
            db.execSQL("CREATE TABLE `stocks` (`symbol` TEXT NOT NULL, `companyName` TEXT NOT NULL, `eps` INTEGER NOT NULL, `dps` INTEGER NOT NULL, `lastPrice` INTEGER NOT NULL, `volume` INTEGER NOT NULL, `pe` REAL NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`symbol`))")
            db.execSQL("CREATE TABLE `market_cache` (`cacheKey` TEXT NOT NULL, `cacheData` TEXT NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`cacheKey`))")
            db.execSQL("CREATE TABLE `dividend_calendar` (`symbol` TEXT NOT NULL, `assemblyDate` TEXT NOT NULL, `dividendAmount` INTEGER NOT NULL, `paymentDate` TEXT NOT NULL, PRIMARY KEY(`symbol`))")

            // 3. Keep user tables safe and preserve details (using CREATE TABLE IF NOT EXISTS)
            db.execSQL("CREATE TABLE IF NOT EXISTS `portfolio` (`symbol` TEXT NOT NULL, `sharesCount` INTEGER NOT NULL, `averageBuyPrice` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`symbol`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `manual_overrides` (`symbol` TEXT NOT NULL, `customDps` INTEGER NOT NULL, `notes` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`symbol`))")

            // 4. Safely drop legacy tables from obsolete database versions if present
            db.execSQL("DROP TABLE IF EXISTS `dividends`")
            db.execSQL("DROP TABLE IF EXISTS `corporate_actions`")
            db.execSQL("DROP TABLE IF EXISTS `quant_metrics`")
            db.execSQL("DROP TABLE IF EXISTS `discrepancy_logs`")
            db.execSQL("DROP TABLE IF EXISTS `market_data_history`")
            db.execSQL("DROP TABLE IF EXISTS `derived_metrics_history`")
            db.execSQL("DROP TABLE IF EXISTS `corporate_events`")
            db.execSQL("DROP TABLE IF EXISTS `share_structure_history`")
        }

        val MIGRATION_1_6 = object : Migration(1, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateToVersion6(db)
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateToVersion6(db)
            }
        }

        fun buildDatabase(context: Context): AppDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tse_vip_database"
        )
        .addMigrations(
            MIGRATION_1_6,
            MIGRATION_5_6
        )
        .fallbackToDestructiveMigrationFrom(1)
        .build()

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
