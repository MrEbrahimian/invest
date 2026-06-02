package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomStockDpsDao {
    @Query("SELECT * FROM custom_stock_dps")
    fun getAllCustomDpsFlow(): Flow<List<CustomStockDps>>

    @Query("SELECT * FROM custom_stock_dps WHERE symbol = :symbol LIMIT 1")
    suspend fun getCustomDpsBySymbol(symbol: String): CustomStockDps?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomDps(customDps: CustomStockDps)

    @Update
    suspend fun updateCustomDps(customDps: CustomStockDps)

    @Delete
    suspend fun deleteCustomDps(customDps: CustomStockDps)
}
