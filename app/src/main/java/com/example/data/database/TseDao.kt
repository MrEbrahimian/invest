package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TseDao {

    // Stocks Queries
    @Query("SELECT * FROM stocks ORDER BY symbol ASC")
    fun getAllStocksFlow(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks")
    suspend fun getAllStocks(): List<StockEntity>

    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    suspend fun getStockBySymbol(symbol: String): StockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Query("DELETE FROM stocks WHERE symbol = :symbol")
    suspend fun deleteStockBySymbol(symbol: String)

    // Portfolio Queries
    @Query("SELECT * FROM portfolio")
    fun getPortfolioFlow(): Flow<List<PortfolioEntity>>

    @Query("SELECT * FROM portfolio")
    suspend fun getPortfolio(): List<PortfolioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: PortfolioEntity)

    @Query("DELETE FROM portfolio WHERE symbol = :symbol")
    suspend fun deletePortfolio(symbol: String)

    // Manual Overrides Queries
    @Query("SELECT * FROM manual_overrides")
    fun getManualOverridesFlow(): Flow<List<ManualOverrideEntity>>

    @Query("SELECT * FROM manual_overrides WHERE symbol = :symbol")
    suspend fun getOverrideForSymbol(symbol: String): ManualOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: ManualOverrideEntity)

    @Query("DELETE FROM manual_overrides WHERE symbol = :symbol")
    suspend fun deleteOverride(symbol: String)

    // Market Cache Queries
    @Query("SELECT * FROM market_cache WHERE cacheKey = :key")
    suspend fun getCacheByKey(key: String): MarketCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: MarketCacheEntity)

    // Dividend Calendar Queries
    @Query("SELECT * FROM dividend_calendar ORDER BY assemblyDate ASC")
    fun getDividendCalendarFlow(): Flow<List<DividendCalendarEntity>>

    @Query("SELECT * FROM dividend_calendar")
    suspend fun getDividendCalendar(): List<DividendCalendarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDividendCalendar(calendarItems: List<DividendCalendarEntity>)
}
