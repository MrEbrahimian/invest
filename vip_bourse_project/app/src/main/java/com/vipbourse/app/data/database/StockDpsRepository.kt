package com.vipbourse.app.data.database

import kotlinx.coroutines.flow.Flow

class StockDpsRepository(private val customStockDpsDao: CustomStockDpsDao) {
    val allCustomDpsFlow: Flow<List<CustomStockDps>> = customStockDpsDao.getAllCustomDpsFlow()

    suspend fun getCustomDpsBySymbol(symbol: String): CustomStockDps? {
        return customStockDpsDao.getCustomDpsBySymbol(symbol)
    }

    suspend fun saveCustomDps(customDps: CustomStockDps) {
        customStockDpsDao.insertCustomDps(customDps)
    }

    suspend fun deleteCustomDps(customDps: CustomStockDps) {
        customStockDpsDao.deleteCustomDps(customDps)
    }
}
