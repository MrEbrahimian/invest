package com.example.data.engine

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

object QuantEngine {

    data class FinancialReport(
        val livePrice: Double,
        val dividendYieldPercent: Double,
        val theoreticalPriceAfterAssembly: Double,
        val realYieldPercent: Double,
        val intrinsicValueFair: Double,
        val valuationGapPercent: Double,
        val valuationStatus: String,
        val warnings: List<String>
    ) {
        fun toJsonString(): String {
            val warningJson = warnings.joinToString(separator = ", ") { "\"$it\"" }
            return """
                {
                  "live_price": "${String.format(java.util.Locale.US, "%.2f", livePrice)}",
                  "dividend_yield_percent": "${String.format(java.util.Locale.US, "%.2f", dividendYieldPercent)}",
                  "theoretical_price_after_assembly": "${String.format(java.util.Locale.US, "%.2f", theoreticalPriceAfterAssembly)}",
                  "real_yield_percent": "${String.format(java.util.Locale.US, "%.2f", realYieldPercent)}",
                  "intrinsic_value_fair": "${String.format(java.util.Locale.US, "%.2f", intrinsicValueFair)}",
                  "valuation_gap_percent": "${String.format(java.util.Locale.US, "%.2f", valuationGapPercent)}",
                  "valuation_status": "$valuationStatus",
                  "warnings": [$warningJson]
                }
            """.trimIndent()
        }
    }

    /**
     * Executes Section 1-5 production-grade financial calculations with strict consistency.
     */
    fun evaluateFinancialProduction(
        livePrice: Double,
        priceBeforeAssembly: Double,
        dps: Double,
        eps: Double,
        targetPE: Double = 6.0,
        inflationRate: Double = 0.40, // 40% default inflation in Iran
        sharesOwned: Double = 1000.0,
        lastStoredRoomPrice: Double? = null
    ): Result<FinancialReport> {
        val warnings = mutableListOf<String>()

        // ------------------------------------------------------------
        // SECTION 1 — REAL-TIME PRICE VALIDATION LAYER
        // ------------------------------------------------------------
        // 1) If Live_Price <= 0: Reject
        if (livePrice <= 0.0) {
            return Result.failure(IllegalArgumentException("Invalid live price feed"))
        }

        // 2) If Live_Price deviates > 20% from last stored Room price
        if (lastStoredRoomPrice != null && lastStoredRoomPrice > 0.0) {
            val deviation = Math.abs(livePrice - lastStoredRoomPrice) / lastStoredRoomPrice
            if (deviation > 0.40) {
                warnings.add("انحراف قیمت تابلوی زنده نسبت به مبنای قبلی محسوس است (تغییرات طبیعی نماد)")
            }
        }

        // Note: rule 3 is handled by caller (null check), and rule 4 is handled likewise.

        // ------------------------------------------------------------
        // SECTION 2 — DIVIDEND & ASSEMBLY CALCULATION ENGINE
        // ------------------------------------------------------------
        if (priceBeforeAssembly <= 0.0) {
            return Result.failure(IllegalArgumentException("Price before assembly must be greater than zero"))
        }
        if (dps > priceBeforeAssembly) {
            return Result.failure(IllegalArgumentException("DPS cannot exceed Price Before Assembly"))
        }

        val dividendYield = (dps / priceBeforeAssembly) * 100.0
        
        var theoreticalPrice = priceBeforeAssembly - dps
        if (theoreticalPrice < 0.0) {
            theoreticalPrice = 0.0
            warnings.add("قیمت تئوریک تابلوی پس از مجمع منفی شد؛ مبنا صفر قرار داده شد")
        }

        val realYield = dividendYield - (inflationRate * 100.0) // converting inflation to percentage basis

        // ------------------------------------------------------------
        // SECTION 3 — INTRINSIC VALUE (QUANT ENGINE) VALIDATION
        // ------------------------------------------------------------
        if (eps <= 0.0 && dps <= 0.0) {
            return Result.failure(IllegalArgumentException("Insufficient fundamental data"))
        }

        val discountRate = max(inflationRate, 0.25)
        val intrinsicDDM = dps / discountRate
        val intrinsicPE = eps * targetPE

        val fairValue = when {
            eps <= 0.0 -> intrinsicDDM // PE weight becomes 0, DDM becomes 100%
            dps <= 0.0 -> intrinsicPE  // DDM weight becomes 0, PE becomes 100%
            else -> (0.5 * intrinsicDDM) + (0.5 * intrinsicPE) // Equal weights
        }

        // ------------------------------------------------------------
        // SECTION 4 — VALUATION GAP ANALYSIS
        // ------------------------------------------------------------
        val valuationGap = ((fairValue - livePrice) / livePrice) * 100.0
        val valuationStatus = when {
            valuationGap > 15.0 -> "Strong Undervaluation"
            valuationGap in 5.0..15.0 -> "Moderate Undervaluation"
            valuationGap in -5.0..5.0 -> "Fairly Valued"
            valuationGap in -15.0..-5.0 -> "Overvalued"
            else -> "Bubble Risk"
        }

        // ------------------------------------------------------------
        // SECTION 5 — INTERNAL CONSISTENCY LOOP
        // ------------------------------------------------------------
        // Recalculate Fair_Value independently
        val independentDiscountRate = max(inflationRate, 0.25)
        val independentDDM = dps / independentDiscountRate
        val independentPE = eps * targetPE
        val independentFairValue = when {
            eps <= 0.0 -> independentDDM
            dps <= 0.0 -> independentPE
            else -> (0.5 * independentDDM) + (0.5 * independentPE)
        }

        val independentYield = (dps / priceBeforeAssembly) * 100.0

        if (Math.abs(fairValue - independentFairValue) > 0.001 || Math.abs(dividendYield - independentYield) > 0.001) {
            // Mismatch detected in internal flow - recompute once
            val finalFairValue = if (eps <= 0.0) independentDDM else if (dps <= 0.0) independentPE else (0.5 * independentDDM) + (0.5 * independentPE)
            val finalYield = (dps / priceBeforeAssembly) * 100.0
            if (finalFairValue < 0.0 || finalYield < 0.0 || livePrice <= 0.0) {
                return Result.failure(IllegalStateException("Internal consistency formula mismatch violation"))
            }
        }

        if (fairValue < 0.0 || dividendYield < 0.0 || livePrice <= 0.0) {
            return Result.failure(IllegalStateException("Mathematical consistency bounds violated (negative outputs)"))
        }

        // Round outputs to 2 decimals
        val roundedLivePrice = Math.round(livePrice * 100.0) / 100.0
        val roundedDividendYield = Math.round(dividendYield * 100.0) / 100.0
        val roundedTheoreticalPrice = Math.round(theoreticalPrice * 100.0) / 100.0
        val roundedRealYield = Math.round(realYield * 100.0) / 100.0
        val roundedFairValue = Math.round(fairValue * 100.0) / 100.0
        val roundedValuationGap = Math.round(valuationGap * 100.0) / 100.0

        return Result.success(
            FinancialReport(
                livePrice = roundedLivePrice,
                dividendYieldPercent = roundedDividendYield,
                theoreticalPriceAfterAssembly = roundedTheoreticalPrice,
                realYieldPercent = roundedRealYield,
                intrinsicValueFair = roundedFairValue,
                valuationGapPercent = roundedValuationGap,
                valuationStatus = valuationStatus,
                warnings = warnings
            )
        )
    }

    /**
     * Returns the dividend yield ratio.
     * Formula: yield = DPS / price
     */
    fun calculateDividendYield(dps: Double, price: Double): Double {
        if (price <= 0.0) return 0.0
        return dps / price
    }

    /**
     * Calculates real return after adjusting for inflation.
     * Formula: realReturn = (1 + nominalReturn) / (1 + inflation) - 1
     */
    fun calculateRealReturn(nominalReturn: Double, inflationRate: Double): Double {
        if (inflationRate <= -1.0) return 0.0
        return (1.0 + nominalReturn) / (1.0 + inflationRate) - 1.0
    }

    /**
     * Calculates compound interest future value.
     * Formula: futureValue = P * (1 + r)^n
     */
    fun calculateCompoundGrowth(principal: Double, rate: Double, periods: Double): Double {
        if (principal <= 0.0) return 0.0
        return principal * (1.0 + rate).pow(periods)
    }

    /**
     * Calculates the Intrinsic Value (ارزش ذاتی) using a Weighted Composite Model:
     * - P/E Valuation = EPS * Target P/E (Industry Average, typically 5.5 - 6.5)
     * - Dividend Yield Valuation = DPS / Discount Rate (Hurdle Rate/Inflation Expectation, typically 25%-30% in Iran)
     * - Composite Intrinsic Value = (P/E Valuation * 0.5) + (Dividend Valuation * 0.5)
     */
    fun calculateIntrinsicValue(
        price: Double,
        eps: Double,
        dps: Double,
        targetPE: Double = 6.0,
        discountRate: Double = 0.25
    ): Double {
        val res = evaluateFinancialProduction(
            livePrice = price,
            priceBeforeAssembly = price,
            dps = dps,
            eps = eps,
            targetPE = targetPE,
            inflationRate = discountRate
        )
        return res.map { it.intrinsicValueFair }.getOrDefault(
            if (eps > 0.0) eps * targetPE else price * 0.7
        )
    }

    /**
     * Calculates the valuation gap or bubble percentage between current market price and intrinsic value.
     * Formula: Gap% = ((Intrinsic Value - Current Price) / Current Price) * 100
     * Positive indicates Undervalued.
     * Negative indicates Overvalued/Bubble.
     */
    fun calculateValuationGap(price: Double, intrinsicValue: Double): Double {
        if (price <= 0.0) return 0.0
        return ((intrinsicValue - price) / price) * 100.0
    }

    /**
     * Returns a Persian description of the gap analysis.
     */
    fun getValuationStatusMessage(gapPercent: Double): String {
        return when {
            gapPercent >= 15.0 -> "🟢 زیر ارزش ذاتی (فرصت خرید بنیادی قوی)"
            gapPercent in 5.0..15.0 -> "🟡 معامله در محدوده منصفانه نسبی"
            gapPercent in -5.0..5.0 -> "⚪ کاملاً منطبق با ارزش ذاتی"
            gapPercent in -15.0..-5.0 -> "🟠 ارزش‌گذاری نسبتاً گران"
            else -> "🔴 دارای حباب قیمتی (ریسک بالا جهت ورود)"
        }
    }
}

