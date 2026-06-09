package com.example.data.engine

import com.example.data.database.DividendCalendarEntity

class DividendCalendarSystem(
    private val calendarItems: List<DividendCalendarEntity>
) {
    data class CalendarAlert(
        val symbol: String,
        val messageFa: String,
        val daysRemaining: Int
    )

    data class AnnualAnalysis(
        val totalExpectedDividend: Long,
        val averageAmount: Double,
        val highestYieldingSymbol: String,
        val activeCompaniesCount: Int
    )

    /**
     * Gets the full list of tracked dividends sorted by assembly date.
     */
    fun getCalendar(): List<DividendCalendarEntity> {
        return calendarItems.sortedBy { it.assemblyDate }
    }

    /**
     * Identifies soon-to-assemble stocks.
     * Generates clear Persian alert messages for system monitoring.
     */
    fun getUpcomingAlerts(currentDateStr: String = "1405-03-15"): List<CalendarAlert> {
        return calendarItems.mapNotNull { item ->
            val remainingDays = calculateDayDifference(currentDateStr, item.assemblyDate)
            if (remainingDays in 1..60) { // Notify if the assembly is within 60 days
                CalendarAlert(
                    symbol = item.symbol,
                    messageFa = "نماد ${item.symbol} در تاریخ ${item.assemblyDate} مجمع برگزاری می‌کند. پرداخت سود تقسیمی مصوب (${item.dividendAmount} ریال) از تاریخ ${item.paymentDate} از طریق سجام آغاز خواهد شد.",
                    daysRemaining = remainingDays
                )
            } else {
                null
            }
        }.sortedBy { it.daysRemaining }
    }

    /**
     * Aggregates and performs comprehensive analysis on the annual dividend portfolio.
     */
    fun analyzeAnnualDividends(): AnnualAnalysis {
        if (calendarItems.isEmpty()) return AnnualAnalysis(0L, 0.0, "-", 0)

        val totalDiv = calendarItems.sumOf { it.dividendAmount.toLong() }
        val avgDiv = calendarItems.map { it.dividendAmount }.average()
        val highest = calendarItems.maxByOrNull { it.dividendAmount }?.symbol ?: "-"

        return AnnualAnalysis(
            totalExpectedDividend = totalDiv,
            averageAmount = avgDiv,
            highestYieldingSymbol = highest,
            activeCompaniesCount = calendarItems.size
        )
    }

    // Solar Hijri simple date comparison helper
    private fun calculateDayDifference(start: String, end: String): Int {
        return try {
            val startParts = start.split("-").map { it.toInt() }
            val endParts = end.split("-").map { it.toInt() }
            val startDays = startParts[0] * 365 + startParts[1] * 30 + startParts[2]
            val endDays = endParts[0] * 365 + endParts[1] * 30 + endParts[2]
            endDays - startDays
        } catch (e: Exception) {
            30 // Safe default if parse error occurs
        }
    }
}
