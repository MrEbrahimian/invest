package com.example.data

import java.io.Serializable

// --- PART 1 — Data Architecture Upgrade ---

/**
 * Layer 1: RawMarketLayer
 * Direct, unadjusted market feeds. Never accessed directly by the Quant Engine.
 */
data class RawMarketLayer(
    val symbol: String,
    val name: String,
    val sector: String,
    val price: Long,
    val yesterdayPrice: Long,
    val changePercent: Float,
    val volume: Long,
    val value: Long,
    val eps: Int,
    val pe: Float,
    val dps: Int
) : Serializable

/**
 * Layer 2: CorporateActionLayer
 * Representation of eventsParsed or registered from Codal documents.
 */
data class CorporateActionLayer(
    val symbol: String,
    val date: String, // format: "yyyy-MM-dd"
    val type: String, // "Capital Increase" or "Dividend"
    val percentage: Float, // percentage of capital increase e.g. 80.0
    val amount: Int = 0, // dividend payment per share in Rials
    val source: String = "", // e.g., "سود انباشته"
    val oldCapital: Long = 1000,
    val newCapital: Long = 1800
) : Serializable

/**
 * Layer 3: AdjustedDataLayer
 * Clean, adjusted, and validated pricing and ratio outputs.
 */
data class AdjustedDataLayer(
    val symbol: String,
    val name: String,
    val sector: String,
    val adjustedPrice: Long,
    val adjustedEps: Int,
    val rawPrice: Long,
    val rawEps: Int,
    val wasAdjusted: Boolean,
    val adjustmentFactor: Float,
    val changePercent: Float,
    val volume: Long,
    val value: Long
) : Serializable

/**
 * Layer 4: QuantEngineLayer
 * Processed investment scores, rankings, signals, visual configurations.
 */
data class QuantEngineLayer(
    val symbol: String,
    val masterScore: Float,
    val valuationScore: Float,
    val dividendScore: Float,
    val inflationHedgeScore: Float,
    val inflationHedgeRating: Int, // e.g., rating from 1 to 5
    val technicalFlowScore: Float,
    val isSuspended: Boolean,
    val restrictionMessage: String = "",
    val countdownText: String = "",
    val statusColor: String = "Green" // Green, Yellow, Red
) : Serializable
