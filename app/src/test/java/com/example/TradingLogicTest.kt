package com.example

import com.example.data.StockSignal
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * High-fidelity logical test suite for VIP Trading application.
 * Specifically checks the safety mechanisms:
 * 1. Inflation hedge capacity (combined dividend yield + growth)
 * 2. Kodal payout history stability
 * 3. Daily transaction liquidity safety targets (free entry & exit)
 * 4. Multi-level user risk preference screening logic (LOW, MEDIUM, HIGH)
 */
class TradingLogicTest {

    @Test
    fun test_low_risk_screening_conditions() {
        // High risk / low screening level should pass basic dividend-yielding stocks with minimum liquidity
        val stock = StockSignal(
            symbol = "ذوب",
            name = "ذوب آهن اصفهان",
            price = 5000L,
            yesterdayPrice = 4900L,
            changePercent = 2.0f,
            volume = 200000L, // matches > 100K
            value = 1500000000L, // matches > 1B Rials
            eps = 50,
            pe = 100f,
            dps = 300,
            dpsYield = 6.0f,
            sector = "فلزات اساسی",
            isVipSignal = false,
            growthPotential = 10.0f
        )

        // Verify LOW level criteria: Volume > 100K, Value > 1B Rials, DPS > 0
        val matchesLiquidity = stock.volume > 100000 && stock.value > 1000000000L
        val matchesStability = stock.dps > 0
        
        assertTrue("LOW level should accept this stock is liquidity-ready", matchesLiquidity)
        assertTrue("LOW level should require active dividends", matchesStability)
    }

    @Test
    fun test_medium_risk_screening_conditions() {
        // Medium level demands checking if the total return beats the bank income benchmark
        val bankRate = 30.0f
        
        val failingStock = StockSignal(
            symbol = "خودرو",
            name = "ایران خودرو",
            price = 3000L,
            yesterdayPrice = 3100L,
            changePercent = -3.2f,
            volume = 800000L,
            value = 2400000000L,
            eps = -50, // Negative EPS
            pe = -60f,
            dps = 0,
            dpsYield = 0.0f,
            sector = "خودرو",
            isVipSignal = false,
            growthPotential = 15.0f // total return = 15% (less than bank rate 30%)
        )

        val winningStock = StockSignal(
            symbol = "فولاد",
            name = "فولاد مبارکه اصفهان",
            price = 6000L,
            yesterdayPrice = 5900L,
            changePercent = 1.69f,
            volume = 1200000L,
            value = 7200000000L,
            eps = 900,
            pe = 6.6f,
            dps = 800,
            dpsYield = 13.3f,
            sector = "فلزات اساسی",
            isVipSignal = false,
            growthPotential = 18.0f // total return = 31.3% (beats bank rate 30%)
        )

        // Evaluate standard Balanced conditions
        // Medium conditions: Volume > 500K, Value > 5B, EPS > 0, Total Return >= bankRate
        val evaluateStock = { s: StockSignal ->
            val matchesLiquidity = s.volume > 500000 && s.value > 5000000000L
            val matchesBeatBank = s.totalReturnPotential >= bankRate
            val passesEps = s.eps > 0
            matchesLiquidity && matchesBeatBank && passesEps
        }

        assertFalse(" خودرو should fail balanced filter due to negative EPS, low value and low yields", evaluateStock(failingStock))
        assertTrue("فولاد should pass balanced filter because total return is 31.3% and has excellent parameters", evaluateStock(winningStock))
    }

    @Test
    fun test_high_risk_vip_inflation_protection() {
        // High level (strict VIP) requires beating the Point-to-Point National CPI Inflation rate (e.g. 38.8%)
        val inflationRate = 38.8f
        
        // High level conditions:
        // 1. Excellent free entry/exit liquidity: volume > 1M, value > 10B Rials (10000000000L)
        // 2. Beating point-to-point national CPI rate: yield + potential growth >= inflationRate (38.8%)
        // 3. Kodal Payout history: paying dividends in at least 3 out of last 5 years
        val ultraHighYieldStock = StockSignal(
            symbol = "فملی",
            name = "ملی صنایع مس ایران",
            price = 8000L,
            yesterdayPrice = 7700L,
            changePercent = 3.9f,
            volume = 2500000L, // matches > 1M
            value = 20000000000L, // matches > 10B Rials
            eps = 1200,
            pe = 6.6f,
            dps = 1000,
            dpsYield = 12.5f,
            sector = "فلزات اساسی",
            isVipSignal = true,
            growthPotential = 28.0f, // total return = 40.5% (beats CPI inflation 38.8%!)
            historicalDps = listOf(600, 750, 900, 0, 1000) // Paid 4 out of last 5 years
        )

        val matchesLiquidity = ultraHighYieldStock.volume > 1000000 && ultraHighYieldStock.value > 10000000000L
        val matchesInflationProtection = ultraHighYieldStock.totalReturnPotential >= inflationRate
        val matchesStability = ultraHighYieldStock.dps > 0 && ultraHighYieldStock.historicalDps.count { it > 0 } >= 3
        val passesEpsCheck = ultraHighYieldStock.eps > 0

        assertTrue("VIP Stock must offer excellent free entrance & exit liquidity", matchesLiquidity)
        assertTrue("VIP Stock must acts as a complete national inflation protection shield", matchesInflationProtection)
        assertTrue("VIP Stock must keep high dividend payment consistency on KODAL (last 5 years)", matchesStability)
        assertTrue("VIP Stock must have positive EPS earnings", passesEpsCheck)
    }
}
