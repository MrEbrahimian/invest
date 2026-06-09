package com.example.data.engine

import com.example.data.database.StockEntity

object VipStockScreener {

    data class ScreenerCandidate(
        val stock: StockEntity,
        val dividendYield: Double,
        val rankScore: Double
    )

    /**
     * Filters and ranks stocks based on high-yielding VIP configurations:
     * - Dividend Yield > 18%
     * - Daily Trading Volume > 1,000,000
     * - P/E Ratio < 8
     *
     * Returns a ranked list of candidate matches, sorted descending by composite performance score.
     */
    fun screenStocks(stocks: List<StockEntity>): List<ScreenerCandidate> {
        return stocks.mapNotNull { stock ->
            val yield = if (stock.lastPrice > 0) {
                stock.dps.toDouble() / stock.lastPrice.toDouble()
            } else {
                0.0
            }
            
            val matchesYield = yield > 0.18
            val matchesVolume = stock.volume > 1000000L
            val matchesPe = stock.pe > 0f && stock.pe < 8.0f
            
            if (matchesYield && matchesVolume && matchesPe) {
                // Composite rank score = Dividend yield weighted % + distance from P/E ceiling (lower is superior)
                val rankScore = (yield * 100.0) + (8.0 - stock.pe)
                ScreenerCandidate(
                    stock = stock,
                    dividendYield = yield,
                    rankScore = rankScore
                )
            } else {
                null
            }
        }.sortedByDescending { it.rankScore }
    }
}
