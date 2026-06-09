package com.example.data

data class StockSignal(
    val id: String,
    val symbol: String,
    val name: String,
    val price: Long,
    val displayPrice: Long,
    val priceInToman: Long,
    val yesterdayPrice: Long,
    val eps: Int,
    val pe: Float,
    val dps: Int,
    val capIncreasePercent: Int,
    val status: String, // "ACTIVE", "STOPPED", "SUSPENDED"
    val isAuditSuspended: Boolean,
    val lastCodalSyncTime: Long,
    val inflationHedgeRating: Int, // 1 to 5
    val volume: Long,
    val masterScore: Double,
    val dividendCountdownText: String,
    val industry: String,
    val signalType: String, // "BUY", "SELL", "WATCH"
    val description: String
)
