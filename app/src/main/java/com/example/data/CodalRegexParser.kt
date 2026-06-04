package com.example.data

import android.util.Log

/**
 * --- PART 5 — Codal Parsing Strategy (Scalable Parser) ---
 * 
 * High-performance regex-based document parsing engine targeting Codal alerts.
 * Strictly limits ingestion to:
 * 1. Annual Financial Statement (12M)
 * 2. AGM Invitation
 * 3. AGM Result
 * 
 * Avoids heavy semantic NLP, using deterministic regular expressions to capture raw financial variables.
 */
class CodalRegexParser {

    data class ParsedDocument(
        val docType: String,
        val isValidDoc: Boolean,
        val extractedDps: Int? = null,
        val extractedCapitalIncreasePercent: Float? = null,
        val extractedNetProfit: Long? = null, // in Rials or millions
        val extractedRegisteredCapital: Long? = null
    )

    /**
     * Entry point to parse Iranian financial text feeds from Codal publishes.
     */
    fun parse(docType: String, textContent: String): ParsedDocument {
        // Validate standard document type constraint
        val isSupported = when (docType) {
            "Annual Financial Statement (12M)", "AGM Invitation", "AGM Result" -> true
            else -> false
        }

        if (!isSupported) {
            return ParsedDocument(docType = docType, isValidDoc = false)
        }

        // Clean Persian non-standard symbols and spacing to isolate raw digits
        val cleanContent = textContent
            .replace(" ", "")
            .replace("‌", "") // remove ZWNJ (half-spaces)
            .replace("،", "")
            .replace(",", "")

        // 1. DPS extraction (e.g. سود نقدی مصوب مجمع به مبلغ ۵۰۰ ریال)
        val dpsRegex = Regex("(?:سودنقدی|سودتقسیمی|سودمصوب|DPS|سودهرسهم):?(\\d+)", RegexOption.IGNORE_CASE)
        val dpsMatch = dpsRegex.find(cleanContent)
        val dps = dpsMatch?.groupValues?.get(1)?.toIntOrNull()

        // 2. Capital Increase % (e.g. پیشنهاد افزایش سرمایه به میزان ۸۰ درصد)
        val capIncRegex = Regex("(?:افزایشسرمایه|سرمایهجدید|افزایشسرمایهبهمیزان):?(\\d+)(?:٪|درصد)", RegexOption.IGNORE_CASE)
        val capIncMatch = capIncRegex.find(cleanContent)
        val capInc = capIncMatch?.groupValues?.get(1)?.toFloatOrNull()

        // 3. Net Profit (e.g. سود خالص دوره مالی منتهی به ۲۹ اسفند)
        val netProfitRegex = Regex("(?:سودخالص|سودخالصسالیانه|سودعملیاتی|soodekhales):?(\\d+)", RegexOption.IGNORE_CASE)
        val netProfitMatch = netProfitRegex.find(cleanContent)
        val netProfit = netProfitMatch?.groupValues?.get(1)?.toLongOrNull()

        // 4. Registered Capital (e.g. سرمایه ثبت شده شرکت معادل ۱۰۰۰۰۰ میلیون ریال)
        val regCapRegex = Regex("(?:سرمایهثبتشده|سرمایهفعلی|کلسرمایه):?(\\d+)", RegexOption.IGNORE_CASE)
        val regCapMatch = regCapRegex.find(cleanContent)
        val regCap = regCapMatch?.groupValues?.get(1)?.toLongOrNull()

        Log.d("CodalParser", "Parsed Codal Alert [$docType] -> DPS: $dps, CapInc%: $capInc, NetProfit: $netProfit, RegisteredCapital: $regCap")

        return ParsedDocument(
            docType = docType,
            isValidDoc = true,
            extractedDps = dps,
            extractedCapitalIncreasePercent = capInc,
            extractedNetProfit = netProfit,
            extractedRegisteredCapital = regCap
        )
    }
}
