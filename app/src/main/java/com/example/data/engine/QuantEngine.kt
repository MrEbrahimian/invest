package com.example.data.engine

import kotlin.math.pow

object QuantEngine {

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
}
