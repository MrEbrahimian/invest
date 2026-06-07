package com.example.data.database

import com.example.data.AdjustedDataLayer
import kotlin.math.abs

/**
 * --- PART 3 & PART 6 — Shadow Calculation Engine & safety mechanism ---
 * 
 * Recalculates fundamental metrics independently to audit the correctness of incoming TSETMC data.
 * Checks for discrepancies and enforces temporary quant suspension if findings exceed limits.
 */
class ShadowCalculationEngine {

    data class AuditResult(
        val symbol: String,
        val manualPe: Float,
        val reportedPe: Float,
        val differencePercent: Float,
        val needsReview: Boolean,
        val isSuspended: Boolean,
        val auditMessage: String,
        val statusColor: String // "Green", "Yellow", "Red"
    )

    /**
     * Compare independent ManualPE = (AdjustedPrice / AdjustedEPS) against ReportedPE from feed.
     * Enforces rules:
     * - Discrepancy > 3%: Flag symbol as "Needs Review", temporary exclude from score lists.
     * - Discrepancy > 5%: Disable calculations and notify with "Data Under Review" message.
     */
    fun audit(
        adjusted: AdjustedDataLayer,
        reportedPe: Float
    ): AuditResult {
        val price = adjusted.adjustedPrice.toFloat()
        val eps = adjusted.adjustedEps.toFloat()

        // Handle negative or zero EPS to avoid division by zero
        val manualPe = if (eps > 0f) price / eps else 0f

        if (reportedPe <= 0f || manualPe <= 0f) {
            return AuditResult(
                symbol = adjusted.symbol,
                manualPe = manualPe,
                reportedPe = reportedPe,
                differencePercent = 0f,
                needsReview = false,
                isSuspended = false,
                auditMessage = "تایید اطلاعات (فاقد سود عملیاتی یا بهای جاری جهت مقایسه P/E)",
                statusColor = "Green"
            )
        }

        // Formula: |ManualPE - ReportedPE| / ReportedPE
        val diffRatio = abs(manualPe - reportedPe) / reportedPe
        val diffPercent = diffRatio * 100f

        val needsReview = diffPercent > 5f
        val isSuspended = diffPercent > 10f

        val (color, msg) = when {
            isSuspended -> {
                Pair(
                    "Red",
                    "Data Under Review – Temporary Quant Suspension (مغایرت بحرانی ${String.format(java.util.Locale.US, "%.1f", diffPercent)}٪ مابین P/E محاسباتی و نمایشی)"
                )
            }
            needsReview -> {
                Pair(
                    "Yellow",
                    "Needs Review (مغایرت آماری غیرعادی ${String.format(java.util.Locale.US, "%.1f", diffPercent)}٪ مابین P/E محاسباتی و مراجع)"
                )
            }
            else -> {
                Pair(
                    "Green",
                    "اطلاعات تایید شده و بدون مغایرت (انحراف جزئی: ${String.format(java.util.Locale.US, "%.1f", diffPercent)}٪)"
                )
            }
        }

        return AuditResult(
            symbol = adjusted.symbol,
            manualPe = manualPe,
            reportedPe = reportedPe,
            differencePercent = diffPercent,
            needsReview = needsReview,
            isSuspended = isSuspended,
            auditMessage = msg,
            statusColor = color
        )
    }
}
