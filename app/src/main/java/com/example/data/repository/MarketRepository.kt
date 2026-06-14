package com.example.data.repository

import com.example.data.database.StockEntity
import com.example.data.database.TseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarketRepository(
    private val apiService: MarketApiService,
    private val tseDao: TseDao
) {
    // Fetches live/mock API responses, maps them, and persists them into Room
    suspend fun refreshMarketData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dtos = try {
                apiService.fetchMarketData()
            } catch (apiEx: Exception) {
                android.util.Log.w("MarketRepository", "Real API service failed; falling back to offline provider", apiEx)
                MarketApiServiceImpl().fetchMarketData()
            }
            val entities = MarketMapper.mapDtosToEntities(dtos)
            tseDao.insertStocks(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetches persistent list of stocks directly from Room local database
    suspend fun getAllStocks(): List<StockEntity> = withContext(Dispatchers.IO) {
        tseDao.getAllStocks()
    }
}
