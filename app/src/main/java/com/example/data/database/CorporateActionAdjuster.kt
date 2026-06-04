package com.example.data.database

import com.example.data.RawMarketLayer
import com.example.data.CorporateActionLayer
import com.example.data.AdjustedDataLayer
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * --- PART 2 — Corporate Action Detector & Adjuster ---
 * 
 * Logic to identify corporate action events (price drops > 25%, market cap shifts, or Codal logs)
 * and calculate adjusted stock price and EPS metrics using capital ratios.
 */
class CorporateActionAdjuster {

    /**
     * Evaluates if any of the three triggering criteria are met to force a recalculation:
     * 1. Absolute daily price drop > 25% (e.g. raw.changePercent <= -25f)
     * 2. Market Cap change > 20%
     * 3. New capital increase report detected from Codal
     */
    fun shouldTriggerAdjustment(
        raw: RawMarketLayer,
        action: CorporateActionLayer?,
        marketCapChangePercent: Float = 0f
    ): Boolean {
        val priceDropOver25 = raw.changePercent <= -25f
        val marketCapShiftOver20 = abs(marketCapChangePercent) > 20f
        val hasCodalCapitalIncrease = action != null && action.type == "Capital Increase"

        return priceDropOver25 || marketCapShiftOver20 || hasCodalCapitalIncrease
    }

    /**
     * Recalculates prices and EPS based on capital adjustment ratios:
     * AdjustedPrice = RawPrice × (OldCapital / NewCapital)
     * AdjustedEPS = RawEPS × (OldCapital / NewCapital)
     */
    fun adjust(
        raw: RawMarketLayer,
        action: CorporateActionLayer?
    ): AdjustedDataLayer {
        if (action == null || action.type != "Capital Increase" || action.percentage <= 0f) {
            return AdjustedDataLayer(
                symbol = raw.symbol,
                name = raw.name,
                sector = raw.sector,
                adjustedPrice = raw.price,
                adjustedEps = raw.eps,
                rawPrice = raw.price,
                rawEps = raw.eps,
                wasAdjusted = false,
                adjustmentFactor = 1.0f,
                changePercent = raw.changePercent,
                volume = raw.volume,
                value = raw.value
            )
        }

        // OldCapital / NewCapital ratio: e.g. for an 80% capital increase:
        // NewCapital = OldCapital * (1 + 80 / 100)
        // Ratio = OldCapital / NewCapital = 1 / 1.8 = 0.5555
        val factor = 1f + (action.percentage / 100f)
        val ratio = 1f / factor

        val adjustedPrice = (raw.price * ratio).toLong()
        val adjustedEps = (raw.eps * ratio).roundToInt()

        return AdjustedDataLayer(
            symbol = raw.symbol,
            name = raw.name,
            sector = raw.sector,
            adjustedPrice = adjustedPrice,
            adjustedEps = adjustedEps,
            rawPrice = raw.price,
            rawEps = raw.eps,
            wasAdjusted = true,
            adjustmentFactor = factor,
            changePercent = raw.changePercent,
            volume = raw.volume,
            value = raw.value
        )
    }
}
