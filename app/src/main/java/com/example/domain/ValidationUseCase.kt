package com.example.domain

import com.example.data.RawMarketLayer
import com.example.data.CorporateActionLayer
import com.example.data.AdjustedDataLayer
import com.example.data.database.QuantDao
import com.example.data.database.CorporateActionAdjuster
import com.example.data.database.ShadowCalculationEngine
import com.example.data.database.DiscrepancyLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- PART 3 & PART 7 — ValidationUseCase ---
 * 
 * Coordinating Use Case that links data layers, handles correction flow,
 * stores discrepancies to Room DB, and flags appropriate alerts.
 */
class ValidationUseCase(
    private val quantDao: QuantDao,
    private val adjuster: CorporateActionAdjuster = CorporateActionAdjuster(),
    private val shadowEngine: ShadowCalculationEngine = ShadowCalculationEngine()
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Executes the verification flow for a symbol:
     * 1. Check for corporate action triggers.
     * 2. Recalculate and adjust price/EPS if needed.
     * 3. Run shadow auditing checks on the resulting PE.
     * 4. Persist any failures/discrepancies > 3% to SQLite.
     */
    suspend fun validateAndProcess(
        raw: RawMarketLayer,
        action: CorporateActionLayer?
    ): Pair<AdjustedDataLayer, ShadowCalculationEngine.AuditResult> = withContext(Dispatchers.IO) {
        
        // 1. Adjust price and EPS if a corporate action is present
        val adjustedData = adjuster.adjust(raw, action)

        // 2. Perform Shadow Auditing
        val auditResult = shadowEngine.audit(adjustedData, raw.pe)

        // 3. Write Discrepancy Log if P/E diff > 3%
        if (auditResult.needsReview) {
            val dateStr = action?.date ?: dateFormat.format(Date())
            val logEntity = DiscrepancyLogEntity(
                symbol = raw.symbol,
                date = dateStr,
                manualPe = auditResult.manualPe,
                reportedPe = auditResult.reportedPe,
                differencePercent = auditResult.differencePercent,
                status = if (auditResult.isSuspended) "Suspended (Critical)" else "Needs Review",
                message = auditResult.auditMessage
            )
            quantDao.insertDiscrepancyLog(logEntity)
        }

        Pair(adjustedData, auditResult)
    }
}
