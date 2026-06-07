package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symbols")
data class SymbolEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val sector: String,
    val currentPrice: Long,
    val eps: Int,
    val pe: Float,
    val lastCapitalIncreaseDate: String // date of last capital increase (e.g. "2026-06-01")
)

@Entity(tableName = "adjusted_prices")
data class AdjustedPriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val date: String,
    val rawPrice: Long,
    val adjustedPrice: Long,
    val adjustmentFactor: Float
)

@Entity(tableName = "dividends")
data class DividendEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val year: String,
    val amount: Int,
    val payoutSpeedDays: Int,
    val exDividendDate: String
)

@Entity(tableName = "corporate_actions")
data class CorporateActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val date: String, // format: "yyyy-MM-dd"
    val type: String, // e.g., "Capital Increase", "Dividend"
    val percentage: Float, // e.g., 80.0 (for 80%)
    val source: String // e.g. "سود انباشته" or "مازاد تجدید ارزیابی"
)

@Entity(tableName = "quant_metrics")
data class QuantMetricsEntity(
    @PrimaryKey val symbol: String,
    val masterScore: Float,
    val valuationScore: Float,
    val dividendScore: Float,
    val inflationHedgeScore: Float, // IHS: Inflation Hedge Score (0-100)
    val technicalFlowScore: Float,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "discrepancy_logs")
data class DiscrepancyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val date: String, // format: "yyyy-MM-dd" or system date
    val manualPe: Float,
    val reportedPe: Float,
    val differencePercent: Float,
    val lastUpdated: Long = System.currentTimeMillis(),
    val status: String = "Needs Review", // "Needs Review", "Approved", "Ignored"
    val message: String
)

@Entity(tableName = "market_data_history", primaryKeys = ["symbol", "timestamp"])
data class MarketDataHistoryEntity(
    val symbol: String,
    val price: Long,
    val eps: Int,
    val pe: Float,
    val volume: Long,
    val tradingStatus: String,
    val timestamp: Long
)

@Entity(tableName = "derived_metrics_history", primaryKeys = ["symbol", "timestamp"])
data class DerivedMetricsHistoryEntity(
    val symbol: String,
    val timestamp: Long,
    val roa: Float,
    val debtRatio: Float,
    val profitMargin: Float,
    val integrityScore: Float,
    val theoreticalPrice: Double,
    val dividendScore: Float
)

@Entity(tableName = "corporate_events")
data class CorporateEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val eventType: String, // "DIVIDEND", "CAPITAL_INCREASE_RETAINED", "CAPITAL_INCREASE_CASH", "CAPITAL_INCREASE_REVALUATION"
    val eventRatio: Double,
    val cashAmount: Double,
    val sourceType: String,
    val eventTimestamp: Long,
    val reference: String
)

@Entity(tableName = "share_structure_history", primaryKeys = ["symbol", "effective_timestamp"])
data class ShareStructureHistoryEntity(
    val symbol: String,
    val shares: Long,
    val effective_timestamp: Long
)
