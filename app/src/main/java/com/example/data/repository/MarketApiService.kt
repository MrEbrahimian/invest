package com.example.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class StockDto(
    val symbol: String,
    val companyName: String,
    val eps: Int,
    val dps: Int,
    val lastPrice: Long,
    val volume: Long,
    val pe: Float,
    val lastUpdateTime: Long
)

interface MarketApiService {
    /**
     * API request contract to fetch active stock market stats.
     */
    @GET("api/v1/market/stocks")
    suspend fun fetchMarketData(): List<StockDto>

    /**
     * Optional endpoint to fetch detailed data for individual stock items.
     */
    @GET("api/v1/market/stocks/detail")
    suspend fun fetchStockDetail(@Query("symbol") symbol: String): StockDto
}

/**
 * Singleton Retrofit Network client designed for TSE Master.
 */
object RetrofitInstance {
    private const val BASE_URL = "https://api.tsemaster.ir/"

    val apiService: MarketApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarketApiService::class.java)
    }
}

/**
 * A mock/fallback provider of active market stats in case the API service is unavailable.
 */
class MarketApiServiceImpl : MarketApiService {
    override suspend fun fetchMarketData(): List<StockDto> {
        return listOf(
            StockDto("فزر", "پویا زرکان آق دره", 2200, 1950, 95000L, 1200000L, 4.31f, System.currentTimeMillis()),
            StockDto("بوعلی", "پتروشیمی بوعلی سینا", 1120, 980, 56000L, 850000L, 5.0f, System.currentTimeMillis()),
            StockDto("شتران", "پالایش نفت تهران", 380, 310, 15000L, 4500000L, 3.94f, System.currentTimeMillis()),
            StockDto("فولاد", "فولاد مبارکه اصفهان", 120, 95, 4800L, 8500000L, 4.0f, System.currentTimeMillis()),
            StockDto("خودرو", "ایران خودرو", -50, 0, 2600L, 12000000L, -5.2f, System.currentTimeMillis()),
            StockDto("کگل", "معدنی صنعتی گل گهر", 610, 480, 28000L, 950000L, 4.59f, System.currentTimeMillis()),
            StockDto("شپنا", "پالایش نفت اصفهان", 420, 360, 18500L, 3200000L, 4.4f, System.currentTimeMillis())
        )
    }

    override suspend fun fetchStockDetail(symbol: String): StockDto {
        return fetchMarketData().firstOrNull { it.symbol == symbol }
            ?: StockDto(symbol, "شرکت نمونه بورس", 100, 80, 10000L, 500000L, 5f, System.currentTimeMillis())
    }
}
