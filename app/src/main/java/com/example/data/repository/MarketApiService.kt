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
            StockDto("شستا", "سرمایه‌گذاری تامین اجتماعی", 230, 195, 1260L, 102900000L, 5.5f, System.currentTimeMillis()),
            StockDto("بوعلی", "پتروشیمی بوعلی سینا", 14620, 9000, 112630L, 15200000L, 7.7f, System.currentTimeMillis()),
            StockDto("فزر", "پویا زرکان آق دره", 21610, 18000, 194500L, 3400000L, 9.0f, System.currentTimeMillis()),
            StockDto("شتران", "پالایش نفت تهران", 420, 310, 2650L, 48000000L, 6.3f, System.currentTimeMillis()),
            StockDto("خودرو", "ایران خودرو", -120, 0, 2800L, 24000000L, -23.3f, System.currentTimeMillis()),
            StockDto("فولاد", "فولاد مبارکه اصفهان", 792, 480, 4120L, 54000000L, 5.2f, System.currentTimeMillis()),
            StockDto("فملی", "ملی صنایع مس ایران", 1259, 900, 6800L, 36000000L, 5.4f, System.currentTimeMillis())
        )
    }

    override suspend fun fetchStockDetail(symbol: String): StockDto {
        return fetchMarketData().firstOrNull { it.symbol == symbol }
            ?: StockDto(symbol, "شرکت نمونه بورس", 100, 80, 10000L, 500000L, 5f, System.currentTimeMillis())
    }
}
