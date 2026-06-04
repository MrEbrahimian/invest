package com.example

import com.example.data.RawMarketLayer
import com.example.data.CorporateActionLayer
import com.example.data.AdjustedDataLayer
import com.example.data.database.CorporateActionAdjuster
import com.example.data.database.ShadowCalculationEngine
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- PART 7 - Unit Tests ---
 * 
 * Comprehensive JVM Unit Tests targeting CorporateActionAdjuster and ShadowCalculationEngine
 * to safeguard financial logic, threshold triggers, and audit compliance.
 */
class QuantEngineUnitTest {

    private val adjuster = CorporateActionAdjuster()
    private val shadowEngine = ShadowCalculationEngine()

    @Test
    fun testCorporateActionAdjustment_SuccessfulSymmetricAdjustment() {
        // Inputs
        val raw = RawMarketLayer(
            symbol = "فزر",
            name = "زرشوران",
            sector = "طلا",
            price = 1800L,
            yesterdayPrice = 1800L,
            changePercent = 0.0f,
            volume = 1200000L,
            value = 10000000000L,
            eps = 180,
            pe = 10.0f,
            dps = 150
        )
        
        // Corporate Action: 80% Capital Increase
        val action = CorporateActionLayer(
            symbol = "فزر",
            date = "2026-06-01",
            type = "Capital Increase",
            percentage = 80.0f,
            amount = 0,
            source = "سود انباشته"
        )

        val adjusted = adjuster.adjust(raw, action)

        // Factor = 1.0 + (80 / 100) = 1.8
        // Ratio = 1 / 1.8 = 0.5555..
        // Expected Adjusted Price = 1800 / 1.8 = 1000
        // Expected Adjusted EPS = 180 / 1.8 = 100
        assertEquals(1000L, adjusted.adjustedPrice)
        assertEquals(100, adjusted.adjustedEps)
        assertTrue(adjusted.wasAdjusted)
    }

    @Test
    fun testCorporateActionAdjustment_NoAdjustmentIfActionIsNull() {
        val raw = RawMarketLayer(
            symbol = "فزر",
            name = "زرشوران",
            sector = "طلا",
            price = 1800L,
            yesterdayPrice = 1800L,
            changePercent = 0.0f,
            volume = 1200000L,
            value = 10000000000L,
            eps = 180,
            pe = 10.0f,
            dps = 150
        )

        val adjusted = adjuster.adjust(raw, action = null)

        // Price and EPS should remain identical
        assertEquals(1800L, adjusted.adjustedPrice)
        assertEquals(180, adjusted.adjustedEps)
        assertFalse(adjusted.wasAdjusted)
    }

    @Test
    fun testShadowCalculationEngine_PerfectCompliance() {
        val adjusted = AdjustedDataLayer(
            symbol = "فزر",
            name = "زرشوران",
            sector = "طلا",
            adjustedPrice = 1000L,
            adjustedEps = 100,
            rawPrice = 1800L,
            rawEps = 180,
            wasAdjusted = true,
            adjustmentFactor = 1.8f,
            changePercent = 0f,
            volume = 1200000L,
            value = 10000000000L
        )

        // Manual PE = 1000 / 100 = 10.0
        // Reported PE is also 10.0 (Perfect matching)
        val result = shadowEngine.audit(adjusted, reportedPe = 10.0f)

        assertFalse(result.needsReview)
        assertFalse(result.isSuspended)
        assertEquals("Green", result.statusColor)
        assertEquals(10.0f, result.manualPe, 0.01f)
        assertEquals(0.0f, result.differencePercent, 0.01f)
    }

    @Test
    fun testShadowCalculationEngine_NeedsReviewWarning() {
        val adjusted = AdjustedDataLayer(
            symbol = "فولاد",
            name = "فولاد مبارکه",
            sector = "فلزات",
            adjustedPrice = 1200L,
            adjustedEps = 200,
            rawPrice = 1200L,
            rawEps = 200,
            wasAdjusted = false,
            adjustmentFactor = 1f,
            changePercent = 0f,
            volume = 1500000L,
            value = 1800000000L
        )

        // Manual PE = 1200 / 200 = 6.0
        // Reported PE is 5.8 (Difference of ~3.4% -> warning threshold)
        val result = shadowEngine.audit(adjusted, reportedPe = 5.8f)

        assertTrue(result.needsReview)
        assertFalse(result.isSuspended)
        assertEquals("Yellow", result.statusColor)
        assertEquals(3.44f, result.differencePercent, 0.1f)
    }

    @Test
    fun testShadowCalculationEngine_CriticalSuspension() {
        val adjusted = AdjustedDataLayer(
            symbol = "خودرو",
            name = "ایران خودرو",
            sector = "خودرو",
            adjustedPrice = 3000L,
            adjustedEps = 150,
            rawPrice = 3000L,
            rawEps = 150,
            wasAdjusted = false,
            adjustmentFactor = 1f,
            changePercent = 0f,
            volume = 4000000L,
            value = 12000000000L
        )

        // Manual PE = 3000 / 150 = 20.0
        // Reported PE on TSETMC is 15.0 (Difference of 33.3% -> well over 5% suspension threshold)
        val result = shadowEngine.audit(adjusted, reportedPe = 15.0f)

        assertTrue(result.needsReview)
        assertTrue(result.isSuspended)
        assertEquals("Red", result.statusColor)
        assertEquals(33.33f, result.differencePercent, 0.1f)
    }
}
