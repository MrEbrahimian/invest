package com.vipbourse.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_stock_dps")
data class CustomStockDps(
    @PrimaryKey val symbol: String,
    val dpsYr1: Int, // 5 years ago (Rials)
    val dpsYr2: Int, // 4 years ago
    val dpsYr3: Int, // 3 years ago
    val dpsYr4: Int, // 2 years ago
    val dpsYr5: Int, // Latest years ago / Current year forecast
    val growthPotential: Float = 15.0f,
    val keyMonths: String = "خرداد تا مرداد ← اوج مجامع نقدی سالیانه"
) {
    val historyList: List<Int> get() = listOf(dpsYr1, dpsYr2, dpsYr3, dpsYr4, dpsYr5)
}
