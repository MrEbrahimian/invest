package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey val symbol: String,
    val companyName: String,
    val eps: Int,
    val dps: Int,
    val lastPrice: Long,
    val volume: Long,
    val pe: Float,
    val updatedAt: Long
)

@Entity(tableName = "portfolio")
data class PortfolioEntity(
    @PrimaryKey val symbol: String,
    val sharesCount: Long,
    val averageBuyPrice: Long,
    val updatedAt: Long
)

@Entity(tableName = "manual_overrides")
data class ManualOverrideEntity(
    @PrimaryKey val symbol: String,
    val customDps: Int,
    val notes: String,
    val updatedAt: Long
)

@Entity(tableName = "market_cache")
data class MarketCacheEntity(
    @PrimaryKey val cacheKey: String,
    val cacheData: String, // String representation format (JSON layout representation)
    val cachedAt: Long
)

@Entity(tableName = "dividend_calendar")
data class DividendCalendarEntity(
    @PrimaryKey val symbol: String,
    val assemblyDate: String, // Solar Hijri or Gregorian e.g., "1405-04-10"
    val dividendAmount: Int,
    val paymentDate: String // E.g., "1405-06-30" or payment period duration
)
