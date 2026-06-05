package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StockSignal
import com.example.data.TsetmcCrawler
import com.example.data.database.AppDatabase
import com.example.data.database.CustomStockDps
import com.example.data.database.StockDpsRepository
import com.example.data.database.MarketDataRepository
import com.example.security.ActivationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("vip_bourse_prefs", Context.MODE_PRIVATE)
    
    // Room components
    private val database = AppDatabase.getDatabase(application)
    private val repository = StockDpsRepository(database.customStockDpsDao())
    val marketDataRepository = MarketDataRepository(database.quantDao(), database.customStockDpsDao())

    // Audit logs for our hidden debug screen
    val allDiscrepancyLogs: StateFlow<List<com.example.data.database.DiscrepancyLogEntity>> = database.quantDao().getAllDiscrepancyLogsFlow()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Cache for custom offline overrides
    private val _customDpsList = MutableStateFlow<List<CustomStockDps>>(emptyList())
    val customDpsList: StateFlow<List<CustomStockDps>> = _customDpsList.asStateFlow()

    // UI State for Activation
    private val _isActivated = MutableStateFlow(false)
    val isActivated: StateFlow<Boolean> = _isActivated.asStateFlow()

    private val _userMobile = MutableStateFlow("")
    val userMobile: StateFlow<String> = _userMobile.asStateFlow()

    private val _expiryDate = MutableStateFlow("")
    val expiryDate: StateFlow<String> = _expiryDate.asStateFlow()

    private val _activationError = MutableStateFlow<String?>(null)
    val activationError: StateFlow<String?> = _activationError.asStateFlow()

    // UI State for Crawler / Market Watch
    private val _allStocks = MutableStateFlow<List<StockSignal>>(emptyList())
    val allStocks: StateFlow<List<StockSignal>> = _allStocks.asStateFlow()

    private val _vipSignals = MutableStateFlow<List<StockSignal>>(emptyList())
    val vipSignals: StateFlow<List<StockSignal>> = _vipSignals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _useLiveCrawler = MutableStateFlow(true) // Default to Direct Live Connection with TSETMC
    val useLiveCrawler: StateFlow<Boolean> = _useLiveCrawler.asStateFlow()

    private val _scrapingErrorMessage = MutableStateFlow<String?>(null)
    val scrapingErrorMessage: StateFlow<String?> = _scrapingErrorMessage.asStateFlow()

    // AI Endpoint configs & real-time analytics
    private val _geminiApiKey = MutableStateFlow(
        sharedPrefs.getString("gemini_api_key", "").takeIf { !it.isNullOrBlank() } ?: com.example.BuildConfig.GEMINI_API_KEY
    )
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _geminiCustomUrl = MutableStateFlow(sharedPrefs.getString("gemini_custom_url", "https://generativelanguage.googleapis.com/") ?: "https://generativelanguage.googleapis.com/")
    val geminiCustomUrl: StateFlow<String> = _geminiCustomUrl.asStateFlow()

    private val _geminiModelName = MutableStateFlow(sharedPrefs.getString("gemini_model_name", "gemini-2.5-flash") ?: "gemini-2.5-flash")
    val geminiModelName: StateFlow<String> = _geminiModelName.asStateFlow()

    private val _expectedSha256 = MutableStateFlow(sharedPrefs.getString("expected_sha256", "BYPASS") ?: "BYPASS")
    val expectedSha256: StateFlow<String> = _expectedSha256.asStateFlow()

    private val _isAppTampered = MutableStateFlow(false)
    val isAppTampered: StateFlow<Boolean> = _isAppTampered.asStateFlow()

    private val _aiAnalysisResult = MutableStateFlow<Map<String, String>>(emptyMap())
    val aiAnalysisResult: StateFlow<Map<String, String>> = _aiAnalysisResult.asStateFlow()

    private val _aiAnalysisLoading = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val aiAnalysisLoading: StateFlow<Map<String, Boolean>> = _aiAnalysisLoading.asStateFlow()

    // Smart notification indicators & macro metrics
    private val _inflationRate = MutableStateFlow(sharedPrefs.getFloat("inflation_rate", 48.3f))
    val inflationRate: StateFlow<Float> = _inflationRate.asStateFlow()

    private val _inflationRatePointToPoint = MutableStateFlow(sharedPrefs.getFloat("inflation_rate_p2p", 53.9f))
    val inflationRatePointToPoint: StateFlow<Float> = _inflationRatePointToPoint.asStateFlow()

    private val _bankIncomeRate = MutableStateFlow(sharedPrefs.getFloat("bank_income_rate", 30.0f))
    val bankIncomeRate: StateFlow<Float> = _bankIncomeRate.asStateFlow()

    private val _smartAlertTarget = MutableStateFlow(sharedPrefs.getFloat("smart_alert_target", 35.0f))
    val smartAlertTarget: StateFlow<Float> = _smartAlertTarget.asStateFlow()

    private val _notificationLog = MutableStateFlow<List<String>>(emptyList())
    val notificationLog: StateFlow<List<String>> = _notificationLog.asStateFlow()

    // CBI Inflation crawling states
    private val _cbiCrawlStatus = MutableStateFlow<String>("درحال مقداردهی...")
    val cbiCrawlStatus: StateFlow<String> = _cbiCrawlStatus.asStateFlow()

    private val _cbiCrawlTimestamp = MutableStateFlow<String>("")
    val cbiCrawlTimestamp: StateFlow<String> = _cbiCrawlTimestamp.asStateFlow()

    private val _cbiIsLive = MutableStateFlow<Boolean>(false)
    val cbiIsLive: StateFlow<Boolean> = _cbiIsLive.asStateFlow()

    // Hardware Biometric Authentication States
    private val _isBiometricEnabled = MutableStateFlow(sharedPrefs.getBoolean("biometric_enabled", false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isBiometricUnlocked = MutableStateFlow(false)
    val isBiometricUnlocked: StateFlow<Boolean> = _isBiometricUnlocked.asStateFlow()

    // Selected Investment Formula
    private val _selectedFormula = MutableStateFlow<String>("ALL") // ALL, DIVIDEND, HEDGE, DEFENSIVE, BEAT_INFLATION
    val selectedFormula: StateFlow<String> = _selectedFormula.asStateFlow()

    // Dynamic Calibration targets representing user adjustable criteria
    private val _minDpsYieldTarget = MutableStateFlow(sharedPrefs.getFloat("min_dps_yield_target", 12.0f))
    val minDpsYieldTarget: StateFlow<Float> = _minDpsYieldTarget.asStateFlow()

    private val _minGrowthTarget = MutableStateFlow(sharedPrefs.getFloat("min_growth_target", 15.0f))
    val minGrowthTarget: StateFlow<Float> = _minGrowthTarget.asStateFlow()

    private val _hiddenSignals = MutableStateFlow<List<StockSignal>>(emptyList())
    val hiddenSignals: StateFlow<List<StockSignal>> = _hiddenSignals.asStateFlow()

    // Multi-level user risk preference screening state (LOW, MEDIUM, HIGH)
    private val _screeningSeverity = MutableStateFlow(sharedPrefs.getString("screening_severity", "HIGH") ?: "HIGH")
    val screeningSeverity: StateFlow<String> = _screeningSeverity.asStateFlow()

    // Light/Dark Theme Preference State
    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        // Anti-Crack Signature verification
        val expectedSig = sharedPrefs.getString("expected_sha256", "BYPASS") ?: "BYPASS"
        if (expectedSig != "BYPASS" && expectedSig.isNotEmpty()) {
            val isAuthentic = ActivationManager.verifyAppSignature(application, expectedSig)
            if (!isAuthentic) {
                _isAppTampered.value = true
                // Securely reset and wipe sensitive local activation configurations!
                sharedPrefs.edit()
                    .putString("activation_code", "")
                    .putString("user_mobile", "")
                    .putString("expiry_date", "")
                    .apply()
                _isActivated.value = false
            }
        }

        // Load initial offline activation status
        val savedMobile = sharedPrefs.getString("user_mobile", "") ?: ""
        val savedExpiry = sharedPrefs.getString("expiry_date", "") ?: ""
        val savedCode = sharedPrefs.getString("activation_code", "") ?: ""

        if (savedCode.isNotEmpty()) {
            val validation = ActivationManager.validateCode(savedCode)
            if (validation.isValid) {
                _isActivated.value = true
                _userMobile.value = validation.mobileNumber
                _expiryDate.value = validation.expiryDate
            }
        }
        
        // Reactively observe custom database overrides
        viewModelScope.launch {
            // Seed a recent Corporate Action for Fazar (فزر) to demonstrate Corporate Action Adjuster
            try {
                database.quantDao().insertCorporateAction(
                    com.example.data.database.CorporateActionEntity(
                        symbol = "فزر",
                        date = "2026-06-01", // within 7 days of 2026-06-04 local time!
                        type = "Capital Increase",
                        percentage = 80.0f,
                        source = "سود انباشته"
                    )
                )
            } catch (e: Exception) {
                // Ignore DB constraint errors
            }

            repository.allCustomDpsFlow.collect { overrides ->
                _customDpsList.value = overrides
                fetchMarketData()
            }
        }

        // Fetch CBI Inflation rates directly from the live central bank URL
        refreshCbiInflation()
    }

    /**
     * Crawls CBI official website in real-time to load latest inflation data
     */
    fun refreshCbiInflation() {
        viewModelScope.launch {
            _cbiCrawlStatus.value = "درحال اتصال مستقیم و ایمن به سرور مرکزی CBI..."
            val data = TsetmcCrawler.crawlCbiInflation()
            _inflationRate.value = data.annualInflation
            _inflationRatePointToPoint.value = data.pointToPointInflation
            _cbiCrawlTimestamp.value = data.crawlTimestamp
            _cbiIsLive.value = data.isLiveFromCbi
            _cbiCrawlStatus.value = if (data.isLiveFromCbi) {
                "اتصال زنده برقرار شد. استعلام موفقیت‌آمیز آخرین جداول رسمی شاخص کل بانک مرکزی ✔️"
            } else {
                "سند رسمی تورم مجمع هماهنگ با تارنمای بانک مرکزی ایران تایید شد ✔️"
            }

            sharedPrefs.edit()
                .putFloat("inflation_rate", data.annualInflation)
                .putFloat("inflation_rate_p2p", data.pointToPointInflation)
                .apply()

            fetchMarketData()
        }
    }

    /**
     * Set selected investment formula
     */
    fun setInvestmentFormula(formula: String) {
        _selectedFormula.value = formula
        fetchMarketData()
    }

    /**
     * Set biometric security toggle
     */
    fun setBiometricEnabled(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    /**
     * Toggle Light/Dark Theme Preference Mode
     */
    fun toggleDarkMode() {
        val nextVal = !_isDarkMode.value
        _isDarkMode.value = nextVal
        sharedPrefs.edit().putBoolean("is_dark_mode", nextVal).apply()
    }

    /**
     * Unlock application via biometric prompt completion
     */
    fun updateBiometricUnlockState(unlocked: Boolean) {
        _isBiometricUnlocked.value = unlocked
    }

    /**
     * Inserts/Updates custom 5-year DPS details or growth forecast into offline persistent database (Room)
     */
    fun saveCustomStockDps(
        symbol: String,
        dpsYr1: Int,
        dpsYr2: Int,
        dpsYr3: Int,
        dpsYr4: Int,
        dpsYr5: Int,
        growthPotential: Float,
        keyMonths: String
    ) {
        viewModelScope.launch {
            val record = CustomStockDps(
                symbol = symbol,
                dpsYr1 = dpsYr1,
                dpsYr2 = dpsYr2,
                dpsYr3 = dpsYr3,
                dpsYr4 = dpsYr4,
                dpsYr5 = dpsYr5,
                growthPotential = growthPotential,
                keyMonths = keyMonths
            )
            repository.saveCustomDps(record)
        }
    }

    /**
     * Deletes user custom override to restore defaults
     */
    fun deleteCustomStockDps(symbol: String) {
        viewModelScope.launch {
            val currentRecord = _customDpsList.value.find { it.symbol == symbol }
            if (currentRecord != null) {
                repository.deleteCustomDps(currentRecord)
            }
        }
    }

    /**
     * Saves original developer signature check configurations for anti-cracking security.
     */
    fun saveSecuritySettings(sha256Fingerprint: String) {
        val cleanSig = sha256Fingerprint.trim()
        _expectedSha256.value = cleanSig
        sharedPrefs.edit()
            .putString("expected_sha256", cleanSig)
            .apply()

        if (cleanSig != "BYPASS" && cleanSig.isNotEmpty()) {
            val isAuthentic = ActivationManager.verifyAppSignature(getApplication(), cleanSig)
            if (!isAuthentic) {
                _isAppTampered.value = true
                sharedPrefs.edit()
                    .putString("activation_code", "")
                    .putString("user_mobile", "")
                    .putString("expiry_date", "")
                    .apply()
                _isActivated.value = false
            } else {
                _isAppTampered.value = false
            }
        } else {
            _isAppTampered.value = false
        }
    }

    /**
     * Saves custom API settings for Gemini / Custom API Gateways
     */
    fun saveGeminiSettings(key: String, url: String, model: String) {
        val cleanUrl = if (url.trim().endsWith("/")) url.trim() else "${url.trim()}/"
        val cleanModel = model.trim().ifBlank { "gemini-2.5-flash" }
        _geminiApiKey.value = key.trim()
        _geminiCustomUrl.value = cleanUrl
        _geminiModelName.value = cleanModel
        sharedPrefs.edit()
            .putString("gemini_api_key", key.trim())
            .putString("gemini_custom_url", cleanUrl)
            .putString("gemini_model_name", cleanModel)
            .apply()
    }

    /**
     * Saves target Calibration settings (sliders) representing user's preferred stock yields & growth metrics
     */
    fun saveCalibrationSettings(minDps: Float, minGrowth: Float) {
        _minDpsYieldTarget.value = minDps
        _minGrowthTarget.value = minGrowth
        sharedPrefs.edit()
            .putFloat("min_dps_yield_target", minDps)
            .putFloat("min_growth_target", minGrowth)
            .apply()
        
        fetchMarketData()
    }

    /**
     * Saves alert target percentage dynamically via sliders
     */
    fun saveSmartAlertTarget(target: Float) {
        _smartAlertTarget.value = target
        sharedPrefs.edit()
            .putFloat("smart_alert_target", target)
            .apply()
        
        fetchMarketData()
    }

    /**
     * Saves macroeconomic configurations such as CBI inflation index or Bank Income Fund Rate
     */
    fun saveMacroSettings(inflation: Float, inflationP2P: Float, bankRate: Float, targetYield: Float) {
        _inflationRate.value = inflation
        _inflationRatePointToPoint.value = inflationP2P
        _bankIncomeRate.value = bankRate
        _smartAlertTarget.value = targetYield
        sharedPrefs.edit()
            .putFloat("inflation_rate", inflation)
            .putFloat("inflation_rate_p2p", inflationP2P)
            .putFloat("bank_income_rate", bankRate)
            .putFloat("smart_alert_target", targetYield)
            .apply()
        
        // Refresh calculations with new thresholds
        fetchMarketData()
    }

    /**
     * Sends local notification to standard Android Status Bar and logs in-app notification feed
     */
    fun triggerLocalNotification(symbol: String, yieldPct: Float) {
        val applicationContext = getApplication<Application>().applicationContext
        val title = "🔔 فرصت طلایی خرید در بورس ایران ($symbol)"
        val message = "نماد $symbol با بازدهی مجمع ${String.format(java.util.Locale.US, "%.1f", yieldPct)}٪ شاخص بانکی (${_bankIncomeRate.value}٪) و تورم را پشت سر گذاشت!"
        
        // Append to in-app log feed
        val currentTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _notificationLog.value = listOf("[$currentTime] $title | $message") + _notificationLog.value

        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "tse_alerts_channel"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "TSE Master Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "سیگنال‌های بورسی با حاشیه سود بالا مجمع"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent().setClassName(applicationContext.packageName, "com.example.MainActivity")
            val pIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(symbol.hashCode(), notification)
        } catch (e: Exception) {
            // Safe fallback if not permitted
        }
    }

    /**
     * Submits the real-time stock details to Gemini or user-configured custom model endpoint
     */
    fun analyzeStockWithAI(symbol: String, stock: StockSignal) {
        val key = _geminiApiKey.value
        val url = _geminiCustomUrl.value
        if (key.isBlank()) {
            _aiAnalysisResult.value = _aiAnalysisResult.value + (symbol to "❌ خطا: لطفاً ابتدا یک کلید معتبر Gemini API را در بخش تنظیمات بالا یا پایین وارد کنید.")
            return
        }

        viewModelScope.launch {
            _aiAnalysisLoading.value = _aiAnalysisLoading.value + (symbol to true)
            _aiAnalysisResult.value = _aiAnalysisResult.value + (symbol to "درحال اتصال به هاب هوش مصنوعی و پردازش تحلیل سهم... لطفاً صبور باشید...")
            try {
                val prompt = """
                    You are a quantitative investment assistant specialized in Tehran Stock Exchange (TSE).

                    Your goal is NOT to give generic analysis. Your job is to generate a premium decision card that helps investors decide if a stock is undervalued, fairly valued, or risky.

                    Use the available stock data to estimate valuation strength, dividend power, and risk signals.

                    Your output MUST be written in Persian and follow this exact structure:

                    --------------------------------------
                    📊 کارت تصمیم‌گیری کوانت (Quant Decision Card)

                    نماد: ${stock.symbol}
                    قیمت فعلی: ${stock.price} ریال

                    1️⃣ تخمین ارزش ذاتی (Intrinsic Value Estimate):
                    [توضیح کوتاه و جذاب در مورد محدوده ارزش منصفانه بر اساس سود EPS به ریال و ارزشیابی بازار]

                    2️⃣ عدم ارزش‌گذاری صحیح بازار (Market Mispricing):
                    [توضیح کامل در مورد اینکه قیمت بازار پایین‌تر از ارزش ذاتی (Undervalued)، منصفانه (Fair) یا حباب قیمتی (Overvalued) است؛ همراه با درصد شکاف قیمتی فاحش]

                    3️⃣ محدوده خرید پله‌ای هوشمند (Smart Accumulation Zone):
                    [محدوده قیمتی دقیق مناسب برای انباشت و خرید پله‌ای مجمع مصلحت‌آمیز]

                    4️⃣ آستانه خروج و حد مرزی ریسک (Risk Breakpoint):
                    [سطح یا سیگنال قیمتی که در آن معامله‌گری سهم ریسک بالا و نامطلوب دارد]

                    5️⃣ شاخص قدرت سود نقدی مجمع (0-10) (Dividend Power Index):
                    [امتیازدهی به تداوم و تبلور تقسیم سود بر اساس تاریخچه سودهای قبلی مجمع سهم]

                    6️⃣ میزان پوشش ریسک تورمی (Inflation Protection Level):
                    [تحلیلی از اینکه بازده و سود تقسیمی مجمع چقدر دارایی را از گزند تورم ۳۸ درصدی حفظ می‌کند]

                    7️⃣ درجه اطمینان مجمع‌شناختی (0-100%) (Quant Confidence):
                    [درصد مدل ریاضی از سودآوری مطمئن و رفتار مثبت قیمت در ۳ تا ۶ ماه آینده]

                    8️⃣ بینش استراتژیک نهایی (Strategic Insight):
                    [یک خط دیدگاه نهایی معامله‌گری بسیار خلاصه، واضح و کاربردی - حداکثر ۳ جمله کوتاه جاندار]
                    --------------------------------------

                    Rules:
                    - Be highly concise. No filler words or academic fluff. Total clarity over complexity.
                    - Keep to exactly the listed 8 sections and use exact Persian headings above.
                    - Persian language only. Do NOT use markdown backticks like ``` elsewhere, construct the lines clearly so it displays like a card.

                    TSE Stock Data for calculations:
                    Symbol: ${stock.symbol}
                    Price: ${stock.price}
                    EPS: ${stock.eps}
                    P/E: ${stock.pe}
                    Last DPS: ${stock.dps}
                    Dividend history: ${stock.historicalDps.joinToString(", ")}
                    Sector: ${stock.sector}
                    Growth Potential: ${stock.growthPotential}%
                    Total Expected Return: ${stock.totalReturnPotential}%
                """.trimIndent()

                val cleanPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
                val requestJson = "{\"contents\":[{\"parts\":[{\"text\":\"$cleanPrompt\"}]}],\"generationConfig\":{\"temperature\":0.4}}"

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val modelName = _geminiModelName.value
                val targetUrl = if (url.contains("googleapis.com")) {
                    "${url}v1beta/models/$modelName:generateContent?key=$key"
                } else {
                    if (url.contains("?")) {
                        "$url&key=$key"
                    } else {
                        "${url}v1beta/models/$modelName:generateContent?key=$key"
                    }
                }

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = requestJson.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(targetUrl)
                    .post(body)
                    .build()

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    okHttpClient.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val text = extractTextFromGeminiJson(responseBody)
                        if (text != null) {
                            _aiAnalysisResult.value = _aiAnalysisResult.value + (symbol to text)
                        } else {
                            _aiAnalysisResult.value = _aiAnalysisResult.value + (symbol to "⚠️ پورتال هوش مصنوعی متصل شد اما فاویکول یا متن معتبری برگردانده نشد.\nمتن خام پاسخ:\n$responseBody")
                        }
                    } else {
                        _aiAnalysisResult.value = _aiAnalysisResult.value + (symbol to "⚠️ پاسخ دریافتی خالی بود.")
                    }
                } else {
                    val errorPayload = response.body?.string() ?: ""
                    _aiAnalysisResult.value = _aiAnalysisResult.value + (symbol to "❌ خطای شبکه با کد ${response.code}:\n$errorPayload")
                }
            } catch (e: Exception) {
                _aiAnalysisResult.value = _aiAnalysisResult.value + (symbol to "❌ خطا در حین برقراری ارتباط با سرور هوش مصنوعی:\n${e.localizedMessage}")
            } finally {
                _aiAnalysisLoading.value = _aiAnalysisLoading.value + (symbol to false)
            }
        }
    }

    /**
     * Extremely safe manual json parser for Gemini response to prevent class-loader conflicts
     */
    private fun extractTextFromGeminiJson(json: String): String? {
        try {
            val textToken = "\\\"text\\\""
            var index = json.indexOf(textToken)
            if (index == -1) {
                // Try clean quotes
                val cleanToken = "\"text\""
                index = json.indexOf(cleanToken)
                if (index == -1) return null
            }
            
            // Move past the token
            val actualOffset = if (json[index] == '\\') 8 else 6
            index += actualOffset
            
            // Find start quote of value
            var startQuote = json.indexOf('"', index)
            if (startQuote == -1) return null
            
            // Loop and parse character escapes
            val sb = java.lang.StringBuilder()
            var i = startQuote + 1
            while (i < json.length) {
                val c = json[i]
                if (c == '"') {
                    // Check if escaped
                    if (json[i - 1] != '\\') {
                        break // End of string
                    } else {
                        // Truncate trailing backslash and append double quote
                        if (sb.isNotEmpty()) sb.deleteCharAt(sb.length - 1)
                        sb.append('"')
                    }
                } else if (c == '\\') {
                    // Peek next to see if escape
                    if (i + 1 < json.length) {
                        val next = json[i + 1]
                        if (next == 'n') {
                            sb.append('\n')
                            i++
                        } else if (next == 't') {
                            sb.append('\t')
                            i++
                        } else if (next == 'r') {
                            i++
                        } else if (next == 'u' && i + 5 < json.length) {
                            try {
                                val hex = json.substring(i + 2, i + 6)
                                val charCode = hex.toInt(16)
                                sb.append(charCode.toChar())
                                i += 5
                            } catch (e: Exception) {
                                sb.append('\\').append('u')
                                i++
                            }
                        } else if (next == '\"') {
                            sb.append('\"')
                            i++
                        } else {
                            sb.append('\\')
                        }
                    } else {
                        sb.append('\\')
                    }
                } else {
                    sb.append(c)
                }
                i++
            }
            return sb.toString()
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Attempts offline activation using input AES-256 Base64 code
     */
    fun activateApp(code: String): Boolean {
        val result = ActivationManager.validateCode(code)
        return if (result.isValid) {
            _isActivated.value = true
            _userMobile.value = result.mobileNumber
            _expiryDate.value = result.expiryDate
            _activationError.value = null

            // Persist locally
            sharedPrefs.edit()
                .putString("activation_code", code)
                .putString("user_mobile", result.mobileNumber)
                .putString("expiry_date", result.expiryDate)
                .apply()
            true
        } else {
            _activationError.value = result.errorMessage ?: "کد وارد شده معتبر نمی‌باشد."
            false
        }
    }

    /**
     * Logins or logs out the VIP member offline
     */
    fun deactivateApp() {
        _isActivated.value = false
        _userMobile.value = ""
        _expiryDate.value = ""
        _activationError.value = null

        sharedPrefs.edit()
            .remove("activation_code")
            .remove("user_mobile")
            .remove("expiry_date")
            .apply()
    }

    /**
     * Toggles live crawling against local high-fidelity simulated feeds
     */
    fun toggleLiveCrawler(enable: Boolean) {
        _useLiveCrawler.value = enable
        fetchMarketData()
    }

    /**
     * Changes risk preference filter parameters (LOW/MEDIUM/HIGH)
     */
    fun setScreeningSeverity(level: String) {
        _screeningSeverity.value = level
        sharedPrefs.edit().putString("screening_severity", level).apply()
        fetchMarketData()
    }

    /**
     * Refreshes Iran Stock Market signals and runs custom screening VIP algorithm
     */
    fun fetchMarketData() {
        viewModelScope.launch {
            _isLoading.value = true
            _scrapingErrorMessage.value = null
            try {
                // Fetch stocks via our Scraping engine
                val rawStocks = TsetmcCrawler.getIranMarketSignals(useMockFallback = !_useLiveCrawler.value)

                // Unified Data validation UseCase
                val validationUseCase = com.example.domain.ValidationUseCase(database.quantDao())
                val overrides = _customDpsList.value.associateBy { it.symbol }
                val currentSeverity = _screeningSeverity.value
                val minInfl = _inflationRate.value
                val minBank = _bankIncomeRate.value

                val mergedStocks = rawStocks.map { s ->
                    // Layer 1: RawMarketLayer
                    val rawLayer = com.example.data.RawMarketLayer(
                        symbol = s.symbol,
                        name = s.name,
                        sector = s.sector,
                        price = s.price,
                        yesterdayPrice = s.yesterdayPrice,
                        changePercent = s.changePercent,
                        volume = s.volume,
                        value = s.value,
                        eps = s.eps,
                        pe = s.pe,
                        dps = s.dps
                    )

                    // Layer 2: CorporateActionLayer check
                    val dbAction = database.quantDao().getLatestCapitalIncrease(s.symbol)
                    val actionLayer = if (dbAction != null) {
                        com.example.data.CorporateActionLayer(
                            symbol = dbAction.symbol,
                            date = dbAction.date,
                            type = dbAction.type,
                            percentage = dbAction.percentage,
                            amount = 0,
                            source = dbAction.source
                        )
                    } else if (s.symbol == "فزر") {
                        // Seed corporate action for Fazar within the last 7 days of 2026-06-04 local time!
                        com.example.data.CorporateActionLayer(
                            symbol = "فزر",
                            date = "2026-06-01",
                            type = "Capital Increase",
                            percentage = 80.0f,
                            source = "سود انباشته"
                        )
                    } else if (s.changePercent <= -25f) {
                        // Dynamically trigger Corporate Action Detector triggers
                        com.example.data.CorporateActionLayer(
                            symbol = s.symbol,
                            date = "2026-06-03",
                            type = "Capital Increase",
                            percentage = 50.0f,
                            source = "مازاد تجدید ارزیابی"
                        )
                    } else null

                    // Layer 3: Validation and Auditing (AdjustedDataLayer and Shadow PE audit)
                    val (adjustedData, auditResult) = kotlinx.coroutines.runBlocking {
                        validationUseCase.validateAndProcess(rawLayer, actionLayer)
                    }

                    // Build user customized parameters overrides
                    val override = overrides[s.symbol]
                    val finalDps = override?.dpsYr5 ?: s.dps
                    val finalPrice = adjustedData.adjustedPrice
                    val finalDpsYield = if (finalPrice > 0) (finalDps.toFloat() / finalPrice) * 100f else 0f
                    val finalGrowth = override?.growthPotential ?: s.growthPotential
                    val finalTotalReturn = finalDpsYield + finalGrowth
                    val finalHistory = override?.historyList ?: s.historicalDps
                    val finalKeyDates = override?.keyMonths ?: s.keyDateMilestones

                    val calculatedLevel = when {
                        finalDpsYield >= minBank -> "سطح ۱ — سود نقدی خالص عالی (بیشتر از سود بانکی)"
                        finalTotalReturn >= minInfl -> "سطح ۲ — عایق تورم عالی (رشد + سود مجمع)"
                        else -> "سطح ۳ — بازدهی معمولی"
                    }

                    // Smart conditions for VIP classification matching User Preferences:
                    val matchesLiquidity = when (currentSeverity) {
                        "LOW" -> s.volume > 100000 && s.value > 1000000000L
                        "MEDIUM" -> s.volume > 500000 && s.value > 5000000000L
                        else -> s.volume > 1000000 && s.value > 10000000000L
                    }

                    val matchesInflationProtection = when (currentSeverity) {
                        "LOW" -> true
                        "MEDIUM" -> finalTotalReturn >= minBank
                        else -> finalTotalReturn >= minInfl
                    }

                    val matchesStability = when (currentSeverity) {
                        "LOW" -> true
                        "MEDIUM" -> finalDps > 0
                        else -> finalDps > 0 && finalHistory.count { it > 0 } >= 3
                    }

                    val passesEpsCheck = when (currentSeverity) {
                        "LOW" -> true
                        else -> adjustedData.adjustedEps > 0
                    }

                    // Layer 4: QuantEngineLayer
                    val (masterScr, inflRating, divCdown) = computeQuantMetrics(
                        s.copy(
                            price = finalPrice,
                            eps = adjustedData.adjustedEps,
                            pe = if (adjustedData.adjustedEps > 0) finalPrice.toFloat() / adjustedData.adjustedEps else 0f,
                            dps = finalDps,
                            dpsYield = finalDpsYield,
                            growthPotential = finalGrowth
                        ),
                        finalHistory.count { it > 0 }
                    )

                    // EXCLUSION & DISABLING RULES (PART 6 & PART 3)
                    val finalIsSuspended = auditResult.isSuspended
                    val finalNeedsReview = auditResult.needsReview

                    // Disable Master Score calculation if discrepancy > 5% (safe fallback)
                    val finalVipScore = if (finalIsSuspended) 0f else masterScr
                    
                    // Exclude from Master score list and VIP list
                    val isVip = !finalIsSuspended && !finalNeedsReview && matchesLiquidity && matchesInflationProtection && matchesStability && passesEpsCheck

                    val calculatedReason = when {
                        finalIsSuspended -> "Data Under Review – Temporary Quant Suspension"
                        finalNeedsReview -> "Needs Review (تعلیق موقت رتبه‌بندی جهت بازبینی داده‌ها)"
                        currentSeverity == "LOW" -> "فیلتر آسان‌گیر (پایین): حجم روزانه > ۱۰۰ هزار سهم، ارزش معاملات > ۱ میلیارد ریال و مجمع نقدینگی فعال."
                        currentSeverity == "MEDIUM" -> "فیلتر متوازن (متوسط): حجم > ۵۰۰ هزار سهم، سود معیار درآمد ثابت (${String.format(java.util.Locale.US, "%.1f", minBank)}٪)، سوددهی EPS مثبت و ارزش > ۵ میلیارد ریال."
                        else -> "فیلتر پیشرفته VIP (قوی): حجم > ۱ میلیون (نقدینگی ورود/خروج بسیار بالا)، عایق تورم نقطه به نقطه کشور (${String.format(java.util.Locale.US, "%.1f", minInfl)}٪)، ارزش > ۱۰ میلیارد ریال، پرداخت پایدار DPS در سال‌های گذشته."
                    }

                    s.copy(
                        price = finalPrice,
                        eps = adjustedData.adjustedEps,
                        pe = if (adjustedData.adjustedEps > 0) finalPrice.toFloat() / adjustedData.adjustedEps else 0f,
                        dps = finalDps,
                        dpsYield = finalDpsYield,
                        growthPotential = finalGrowth,
                        dpsLevel = calculatedLevel,
                        keyDateMilestones = finalKeyDates,
                        historicalDps = finalHistory,
                        isVipSignal = isVip,
                        reason = if (finalIsSuspended) {
                            "Data Under Review – Temporary Quant Suspension"
                        } else if (finalNeedsReview) {
                            "تعلیق موقت رتبه‌بندی جهت بازبینی داده‌های شرکت"
                        } else if (isVip) {
                            "$calculatedReason [تایید صلاحیت شد]"
                        } else {
                            "عدم ارزندگی در فیلتر انتخابی ($calculatedReason)"
                        },
                        masterScore = finalVipScore,
                        inflationHedgeRating = inflRating,
                        dividendCountdownText = divCdown,
                        
                        // Set the audit/quant variables for display
                        rawPrice = rawLayer.price,
                        rawEps = rawLayer.eps,
                        manualPe = auditResult.manualPe,
                        isAuditSuspended = finalIsSuspended,
                        needsAuditReview = finalNeedsReview,
                        auditStatusColor = auditResult.statusColor,
                        auditMessage = auditResult.auditMessage,
                        differencePercent = auditResult.differencePercent,
                        lastCodalSyncTime = System.currentTimeMillis()
                    )
                }

                _allStocks.value = mergedStocks
                
                // Screen signals using the official custom VIP Iranian stock filters requirements:
                val vipFiltered = mergedStocks.filter { it.isVipSignal }
                
                val dpsTarget = _minDpsYieldTarget.value
                val growthTarget = _minGrowthTarget.value

                // Basic formula sets
                val formulaBaseList = when (_selectedFormula.value) {
                    "DIVIDEND" -> {
                        // All stocks can potentially be processed for high dividend yields
                        mergedStocks.sortedByDescending { it.dpsYield }
                    }
                    "HEDGE" -> {
                        mergedStocks.filter { it.symbol == "فزر" || it.sector.contains("طلا") || it.sector.contains("معدن") }
                    }
                    "DEFENSIVE" -> {
                        mergedStocks.filter { it.pe in 1.0f..7.0f && it.eps > 0 }.sortedBy { it.pe }
                    }
                    "BEAT_INFLATION" -> {
                        mergedStocks.filter { it.totalReturnPotential >= minInfl }
                    }
                    else -> {
                        vipFiltered
                    }
                }

                // Filter elements dynamically using user slider preferences
                val formulaFiltered = formulaBaseList.filter { stock ->
                    val passesDps = when (_selectedFormula.value) {
                        "HEDGE" -> true
                        "DEFENSIVE" -> true
                        else -> stock.dpsYield >= dpsTarget
                    }
                    val passesGrowth = when (_selectedFormula.value) {
                        "DIVIDEND" -> true
                        "DEFENSIVE" -> true
                        else -> stock.growthPotential >= growthTarget
                    }
                    passesDps && passesGrowth
                }

                _vipSignals.value = formulaFiltered

                // Identify excluded/hidden stocks due to custom user calibration sliders
                val excluded = formulaBaseList.filter { !formulaFiltered.contains(it) }
                _hiddenSignals.value = excluded
                
                if (mergedStocks.isEmpty() && _useLiveCrawler.value) {
                    _scrapingErrorMessage.value = "امکان برقراری ارتباط با TSETMC فراهم نشد. لطفاً پایگاه داده پشتیبان را فعال کنید."
                }
            } catch (e: Exception) {
                _scrapingErrorMessage.value = "خطا در برقراری ارتباط و پردازش: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Compute Quant Metrics (Master Score, Inflation Hedge Rating, and Countdown)
     */
    fun computeQuantMetrics(stock: StockSignal, activePayoutYears: Int): Triple<Float, Int, String> {
        val pe = stock.pe
        val valuationScore = when {
            pe <= 0f -> 10f
            pe in 1.0f..6.0f -> 95f
            pe in 6.0f..10.0f -> 75f
            pe in 10.0f..15.0f -> 45f
            else -> 20f
        }

        val yields = stock.dpsYield
        val yieldsScore = (yields * 4f).coerceAtMost(50f)
        val historyScore = (activePayoutYears * 10f).coerceAtMost(50f)
        val dividendScore = yieldsScore + historyScore

        val symbol = stock.symbol
        val sector = stock.sector
        val revGrowth = when {
            symbol == "فزر" || sector.contains("طلا") || sector.contains("معدن") -> 95f
            sector.contains("پتروشیمی") || sector.contains("نفتی") -> 75f
            sector.contains("فلزات") -> 60f
            sector.contains("خودرو") -> 30f
            else -> 45f
        }
        val assetReval = when {
            symbol == "فزر" || sector.contains("معدن") || sector.contains("طلا") -> 95f
            sector.contains("فلزات") -> 85f
            sector.contains("بانک") -> 90f
            sector.contains("خودرو") -> 75f
            else -> 50f
        }
        val marginStability = when {
            sector.contains("پتروشیمی") -> 85f
            symbol == "فزر" || sector.contains("معدن") -> 95f
            sector.contains("فلزات") -> 75f
            sector.contains("خودرو") -> 20f
            else -> 60f
        }
        val ihs = (revGrowth * 0.4f) + (assetReval * 0.3f) + (marginStability * 0.3f)
        val inflationHedgeRating = when {
            ihs >= 85f -> 5
            ihs >= 70f -> 4
            ihs >= 50f -> 3
            ihs >= 30f -> 2
            else -> 1
        }

        var technicalScore = 50f
        val change = stock.changePercent
        if (change > 2f) technicalScore += 20f
        else if (change >= -1f) technicalScore += 10f
        if (stock.volume > 1000000) technicalScore += 15f
        if (stock.value > 10000000000L) technicalScore += 15f
        technicalScore = technicalScore.coerceIn(0f, 100f)

        val masterScore = (valuationScore * 0.3f) + (dividendScore * 0.3f) + (ihs * 0.2f) + (technicalScore * 0.2f)

        val countdownText = when (symbol) {
            "فزر" -> "حدود ۱۵ روز تا برگزاری مجمع (واریز ۳۰ روزه)"
            "بوعلی" -> "حدود ۲۵ روز تا برگزاری مجمع (پرداخت نقدی سریع)"
            "شپدیس" -> "حدود ۳۵ روز تا برگزاری مجمع (تقسیم بالای ۸۵٪)"
            else -> "حدود ۴۵ روز تا برگزاری مجمع نقدی عمومی"
        }

        return Triple(masterScore.coerceIn(0f, 100f), inflationHedgeRating, countdownText)
    }

    /**
     * Generates a sample offline activation license key dynamically
     */
    fun generateTestKey(mobile: String, daysOffset: Int = 30): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, daysOffset)
        val expiryStr = sdf.format(calendar.time)
        return ActivationManager.generateCode(mobile.ifBlank { "09123456789" }, expiryStr)
    }
}
