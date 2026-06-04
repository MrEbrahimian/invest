package com.example.data

import java.io.Serializable

data class StockSignal(
    val symbol: String,
    val name: String,
    val price: Long,
    val yesterdayPrice: Long,
    val changePercent: Float,
    val volume: Long,
    val value: Long, // in Rials
    val eps: Int,
    val pe: Float,
    val dps: Int, // Calculated/estimated dividend
    val dpsYield: Float, // dps / price * 100
    val sector: String,
    val isVipSignal: Boolean,
    val reason: String = "",
    // Advanced VIP metrics requested by user
    val growthPotential: Float = 15.0f, // Estimated annual percentage capital growth potential
    val dpsLevel: String = "سطح ۱ - فقط سود نقدی", // Level 1 versus Level 2
    val keyDateMilestones: String = "خرداد تا مرداد ← اوج مجامع نقدی", // Assembly months for Iran stocks
    val historicalDps: List<Int> = listOf(200, 250, 280, 210, 350), // last 5 years dividends in Rials
    val paymentDelayMonths: Int = 4, // Assembly payoff limit in months (e.g. 1 to 4 months)
    val masterScore: Float = 65f, // Composite Master score (0-100)
    val inflationHedgeRating: Int = 3, // Rating level from 1 to 5
    val dividendCountdownText: String = "۳۰ روز تا واریز نقدی", // Countdown statement (Days remaining or predicted payment schedule)
    
    // --- PART 3 & PART 4 - Quant Audit & Data validation additions ---
    val rawPrice: Long = price,
    val rawEps: Int = eps,
    val manualPe: Float = pe,
    val isAuditSuspended: Boolean = false,
    val needsAuditReview: Boolean = false,
    val auditStatusColor: String = "Green", // "Green", "Yellow", "Red"
    val auditMessage: String = "اطلاعات مالی معتبر",
    val differencePercent: Float = 0f,
    val lastCodalSyncTime: Long = System.currentTimeMillis()
) : Serializable {
    val priceInToman: Long get() = price / 10
    val valueInToman: Long get() = value / 10
    
    // Total Return Potential = Dividend Yield + Capital Growth Potential
    val totalReturnPotential: Float get() = dpsYield + growthPotential
}
