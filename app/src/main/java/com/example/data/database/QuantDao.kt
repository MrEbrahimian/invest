package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuantDao {
    // 1. Symbols
    @Query("SELECT * FROM symbols")
    fun getAllSymbolsFlow(): Flow<List<SymbolEntity>>

    @Query("SELECT * FROM symbols WHERE symbol = :symbol LIMIT 1")
    suspend fun getSymbol(symbol: String): SymbolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymbol(symbol: SymbolEntity)

    // 2. Adjusted Prices
    @Query("SELECT * FROM adjusted_prices WHERE symbol = :symbol ORDER BY date DESC")
    suspend fun getAdjustedPricesForSymbol(symbol: String): List<AdjustedPriceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustedPrice(price: AdjustedPriceEntity)

    // 3. Dividends
    @Query("SELECT * FROM dividends WHERE symbol = :symbol ORDER BY year DESC")
    suspend fun getDividendsForSymbol(symbol: String): List<DividendEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDividend(dividend: DividendEntity)

    // 4. Corporate Actions
    @Query("SELECT * FROM corporate_actions WHERE symbol = :symbol ORDER BY date DESC")
    suspend fun getCorporateActionsForSymbol(symbol: String): List<CorporateActionEntity>

    @Query("SELECT * FROM corporate_actions WHERE symbol = :symbol AND type = 'Capital Increase' ORDER BY date DESC LIMIT 1")
    suspend fun getLatestCapitalIncrease(symbol: String): CorporateActionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorporateAction(action: CorporateActionEntity)

    // 5. Quant Metrics
    @Query("SELECT * FROM quant_metrics WHERE symbol = :symbol LIMIT 1")
    suspend fun getQuantMetricsForSymbol(symbol: String): QuantMetricsEntity?

    @Query("SELECT * FROM quant_metrics")
    fun getAllQuantMetricsFlow(): Flow<List<QuantMetricsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuantMetrics(metrics: QuantMetricsEntity)

    // 6. Discrepancy Logs
    @Query("SELECT * FROM discrepancy_logs ORDER BY lastUpdated DESC")
    fun getAllDiscrepancyLogsFlow(): Flow<List<DiscrepancyLogEntity>>

    @Query("SELECT * FROM discrepancy_logs WHERE symbol = :symbol ORDER BY lastUpdated DESC")
    suspend fun getDiscrepancyLogsForSymbol(symbol: String): List<DiscrepancyLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscrepancyLog(log: DiscrepancyLogEntity)

    @Query("DELETE FROM discrepancy_logs")
    suspend fun deleteAllDiscrepancyLogs()
}
