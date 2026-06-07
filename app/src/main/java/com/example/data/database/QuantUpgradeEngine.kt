package com.example.data.database

import kotlin.math.*

object QuantUpgradeEngine {

    /**
     * Patch 1 — Ordered Theoretical Price Calculator
     * Sorts events ascending by timestamp, and sequentially applies cash dividends and stock splits.
     * CASE 1 (Dividend first): P_new = (P_old - DPS) / (1 + r)
     * CASE 2 (Dilution first): P_new = (P_old / (1 + r)) - DPS
     */
    fun calculateOrderedTheoreticalPrice(
        pOld: Double,
        events: List<CorporateEvent>
    ): Double {
        val sortedEvents = events.sortedBy { it.eventTimestamp }
        var price = pOld
        for (event in sortedEvents) {
            if (event.eventType == "DIVIDEND") {
                price -= event.cashAmount
            } else if (event.eventType.startsWith("CAPITAL_INCREASE")) {
                price /= (1.0 + event.eventRatio)
            }
            if (price < 0.0) {
                price = 0.0
            }
        }
        return price
    }

    /**
     * Patch 2 — Market Confidence Calculator
     * Scores the reliability of prices on a 0-1 scale.
     */
    data class MarketConfidence(
        val freshnessScore: Double,
        val priceDeviationScore: Double,
        val epsConsistencyScore: Double,
        val finalScore: Double
    )

    fun calculateMarketConfidence(
        marketPrice: Double,
        theoreticalPrice: Double,
        lastUpdatedTimestampMs: Long,
        currentTimeMs: Long = System.currentTimeMillis(),
        epsConsistencyScore: Double = 1.0
    ): MarketConfidence {
        val hoursSinceUpdate = (currentTimeMs - lastUpdatedTimestampMs).toDouble() / (1000.0 * 60.0 * 60.0)
        val freshnessScore = exp(-hoursSinceUpdate / 24.0)

        val priceDeviationScore = if (theoreticalPrice > 0.0) {
            1.0 - min(1.0, abs(marketPrice - theoreticalPrice) / theoreticalPrice)
        } else {
            1.0
        }

        val finalScore = (0.6 * priceDeviationScore) + (0.4 * freshnessScore)

        return MarketConfidence(
            freshnessScore = freshnessScore,
            priceDeviationScore = priceDeviationScore,
            epsConsistencyScore = epsConsistencyScore,
            finalScore = finalScore
        )
    }

    /**
     * Patch 4 — Share Structure Update Logic
     * When capital increase is inserted, calculates the new share count.
     */
    fun calculateNewShares(
        oldShares: Long,
        eventRatio: Double
    ): Long {
        return (oldShares * (1.0 + eventRatio)).toLong()
    }

    /**
     * Inserts a corporate event and writes to share structure history if it is a capital increase.
     */
    suspend fun insertCorporateEventAndUpdateShares(
        quantDao: QuantDao,
        symbol: String,
        eventType: String,
        eventRatio: Double,
        cashAmount: Double,
        sourceType: String,
        eventTimestamp: Long,
        reference: String,
        currentShares: Long
    ) {
        val event = CorporateEvent(
            symbol = symbol,
            eventType = eventType,
            eventRatio = eventRatio,
            cashAmount = cashAmount,
            sourceType = sourceType,
            eventTimestamp = eventTimestamp,
            reference = reference
        )
        quantDao.insertCorporateEvent(event)

        if (eventType.startsWith("CAPITAL_INCREASE")) {
            val newShares = calculateNewShares(currentShares, eventRatio)
            quantDao.insertShareStructureHistory(
                ShareStructureHistoryEntity(
                    symbol = symbol,
                    shares = newShares,
                    effective_timestamp = eventTimestamp
                )
            )
        }
    }

    /**
     * Patch 5 — Safe Price Layering
     * Determines whether to display raw price or safe analytical theoretical price.
     */
    fun selectDisplayPrice(
        rawPrice: Long,
        theoreticalPrice: Double,
        confidenceScore: Double,
        isAuditSuspended: Boolean
    ): Long {
        return if (confidenceScore < 0.6 || isAuditSuspended) {
            theoreticalPrice.toLong()
        } else {
            rawPrice
        }
    }
}
