package com.example.data.ai

import com.example.data.database.StockEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class AiAssistant(private val apiKey: String) {

    /**
     * Sends active Stock analysis request to Gemini-3.5-Flash model.
     * Returns complete structured Persian analysis output.
     */
    suspend fun analyzeStock(stock: StockEntity): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "⚠️ کلید اختصاصی جمنای (Gemini-API) یافت نشد. برای تحلیل‌های عمیق هوشمند، کد API خود را از تنظیمات برنامه دریافت و وارد کنید."
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            val prompt = """
                شما یک دستیار هوش مالی ارشد برای بازار بورس ایران (TSE Master) هستید. 
                لطفاً اطلاعات زیر را با دقت بالا تحلیل کنید و خروجی را با زبانی ساده، جذاب و به صورت ساختار یافته به زبان فارسی ارسال کنید:
                
                نماد معاملاتی: ${stock.symbol}
                نام شرکت: ${stock.companyName}
                آخرین قیمت: ${stock.lastPrice} ریال
                سود خالص هر سهم (EPS): ${stock.eps} ریال
                سود تقسیمی پشنهادی (DPS): ${stock.dps} ریال
                نسبت P/E: ${stock.pe}
                حجم معاملات روزانه: ${stock.volume} سهم
                
                انتظارات تحلیل بازار:
                ۱. تحلیل جذاب نسبت بازده نقدی سود (Dividend Yield).
                ۲. ارزیابی ریسک نگهداری سهم در شرایط فعلی تورم ایران.
                ۳. مشخص کردن اینکه آیا سهم به عنوان عایق و سپر تورمی قوی عمل می‌کند یا ضعیف.
                ۴. خلاصه توصیه مجمع معامله‌گران به زبان ساده.
            """.trimIndent()

            val jsonPayload = """
                {
                  "contents": [{
                    "parts": [{
                      "text": "${prompt.replace("\"", "\\\"").replace("\n", "\\n")}"
                    }]
                  }]
                }
            """.trimIndent()

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                parseTextFromGeminiResponse(responseString)
            } else {
                val errString = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                "خطا در ارتباط با سرور هوش مصنوعی (کد خطا: $responseCode)\n$errString"
            }
        } catch (e: Exception) {
            "پیوند با اینترنت یا سرور جمنای برقرار نشد: ${e.message}"
        }
    }

    /**
     * Parses the JSON output of Generative Language Gemini response safely using regular expressions.
     */
    private fun parseTextFromGeminiResponse(rawJson: String): String {
        return try {
            val matcher = Pattern.compile("\"text\"\\s*:\\s*\"([^\"]+)\"").matcher(rawJson)
            val builder = StringBuilder()
            while (matcher.find()) {
                val match = matcher.group(1) ?: continue
                val clean = match
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                builder.append(clean).append("\n")
            }
            if (builder.isNotEmpty()) {
                builder.toString().trim()
            } else {
                "موردی بازگردانده نشد. لطفاً مجدداً تلاش نمایید."
            }
        } catch (e: Exception) {
            rawJson
        }
    }
}
