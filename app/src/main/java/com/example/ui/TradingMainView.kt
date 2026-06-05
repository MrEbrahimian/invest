package com.example.ui

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.data.StockSignal
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TradingMainView(viewModel: TradingViewModel) {
    val isActivated by viewModel.isActivated.collectAsState()
    val userMobile by viewModel.userMobile.collectAsState()
    val expiryDate by viewModel.expiryDate.collectAsState()
    val isAppTampered by viewModel.isAppTampered.collectAsState()

    // Enforce Right-to-Left for Persian language interface
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isAppTampered) {
                AppTamperedScreen(viewModel)
            } else {
                AnimatedContent(
                    targetState = isActivated,
                    transitionSpec = {
                        fadeIn() + slideInVertically() with fadeOut() + slideOutVertically()
                    },
                    label = "ActivationScreenTransition"
                ) { activated ->
                    if (activated) {
                        VipDashboardScreen(
                            viewModel = viewModel,
                            userMobile = userMobile,
                            expiryDate = expiryDate
                        )
                    } else {
                        ActivationRequiredScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

/**
 * Screen #1: Activation Required Screen
 * Elegant offline AES activation form with built-in developer/admin license generator helper.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationRequiredScreen(viewModel: TradingViewModel) {
    var codeInput by remember { mutableStateOf("") }
    val activationError by viewModel.activationError.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Responsive variables aligned with luxury gold sand theme
    val cardBackground = if (isDarkMode) Color(0xFF131926) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkMode) Color(0xFF202B3D) else Color(0xFFDFD9D0)
    val selectionBg = if (isDarkMode) Color(0xFF1C2436) else Color(0xFFF3EFE9)
    val textPrimary = if (isDarkMode) Color(0xFFF5F3EF) else Color(0xFF1F1B18)
    val textMuted = if (isDarkMode) Color(0xFF858E9C) else Color(0xFF6B6259)
    val goldClr = if (isDarkMode) GoldPrimary else Color(0xFFB39256)
    val goldSec = if (isDarkMode) GoldSecondary else Color(0xFF9E7E45)

    // For test generator helper
    var testMobileInput by remember { mutableStateOf("09123456789") }
    var generatedTrialKey by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))

            // Premium VIP Diamond Crown Icon Visual
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(listOf(goldClr, goldSec)),
                        CircleShape
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDarkMode) Color(0xFF0A0D14) else Color(0xFFFAF8F5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "قفل برنامه VIP",
                        tint = goldClr,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "سامانه معاملاتی ویژه بورس ایران",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = goldClr,
                textAlign = TextAlign.Center
            )

            Text(
                text = "نسخه VIP بدون نیاز به سرور (محاسبات کامل کلاینت‌ساید)",
                fontSize = 12.sp,
                color = textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Premium License Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔐 لایسنس فعالسازی آفلاین",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "به دلیل امنیت پیشرفته کلاینت‌ساید و لایسنس رمزنگاری شده AES-256، برای دسترسی به سیگنال‌های بورس ایران باید کد لایسنس دریافتی از مدیر سامانه تفکیک بازار را وارد کنید.",
                        fontSize = 13.sp,
                        color = textMuted,
                        lineHeight = 22.ssp()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs UI
            Text(
                text = "کد فعالسازی آفلاین را وارد کنید",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )

            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("کد رمزنگاری لایسنس...", color = textMuted) },
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = goldClr,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (activationError != null) {
                Text(
                    text = "⚠️ " + activationError,
                    color = StockRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.activateApp(codeInput) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = goldClr,
                    contentColor = if (isDarkMode) Color(0xFF0A0D14) else Color(0xFFFFFFFF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("تایید کدهای سخت‌افزاری و ورود", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sandbox Testing Generator Help Card (Very useful!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBackground.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, goldClr.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛠️ بخش فعال‌سازی و صدور آفلاین لایسنس",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "جهت تسهیل ورود، می‌توانید کلید لایسنس اختصاصی برای شماره موبایل خود را از بخش زیر دریافت کرده و مستقیماً کپی و استفاده کنید:",
                        fontSize = 12.sp,
                        color = textMuted,
                        lineHeight = 20.ssp()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = testMobileInput,
                        onValueChange = { testMobileInput = it },
                        label = { Text("شماره موبایل کلاینت", color = textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = goldClr,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            generatedTrialKey = viewModel.generateTestKey(testMobileInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = selectionBg),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("ساخت لایسنس رمزنگاری AES", color = goldClr, fontWeight = FontWeight.Bold)
                    }

                    if (generatedTrialKey.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDarkMode) Color(0xFF0A0D14) else Color(0xFFFAF8F5), RoundedCornerShape(8.dp))
                                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(generatedTrialKey))
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("کد لایسنس اختصاصی رمز شده:", fontSize = 11.sp, color = goldClr, fontWeight = FontWeight.Bold)
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "کپی", tint = textMuted, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = generatedTrialKey,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = textPrimary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen #2: The Iran Bourse VIP Signal Dashboard
 * Anti-leak screen with Bottom Navigation (Home, Invest, Settings & Simulator).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipDashboardScreen(
    viewModel: TradingViewModel,
    userMobile: String,
    expiryDate: String
) {
    val allStocks by viewModel.allStocks.collectAsState()
    val vipSignals by viewModel.vipSignals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val useLiveCrawler by viewModel.useLiveCrawler.collectAsState()
    val scrapingErrorMessage by viewModel.scrapingErrorMessage.collectAsState()
    val screeningSeverity by viewModel.screeningSeverity.collectAsState()

    // Collect AI & Macro Economic settings state
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val geminiCustomUrl by viewModel.geminiCustomUrl.collectAsState()
    val geminiModelName by viewModel.geminiModelName.collectAsState()
    val inflationRate by viewModel.inflationRate.collectAsState()
    val inflationRatePointToPoint by viewModel.inflationRatePointToPoint.collectAsState()
    val bankIncomeRate by viewModel.bankIncomeRate.collectAsState()
    val smartAlertTarget by viewModel.smartAlertTarget.collectAsState()
    val expectedSha256 by viewModel.expectedSha256.collectAsState()
    val notificationLog by viewModel.notificationLog.collectAsState()

    var editedApiKey by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
    var editedCustomUrl by remember(geminiCustomUrl) { mutableStateOf(geminiCustomUrl) }
    var editedModelName by remember(geminiModelName) { mutableStateOf(geminiModelName) }
    var editedSha256 by remember(expectedSha256) { mutableStateOf(expectedSha256) }
    var editedInflationRate by remember(inflationRate) { mutableStateOf(inflationRate.toString()) }
    var editedInflationRatePointToPoint by remember(inflationRatePointToPoint) { mutableStateOf(inflationRatePointToPoint.toString()) }
    var editedBankIncomeRate by remember(bankIncomeRate) { mutableStateOf(bankIncomeRate.toString()) }
    var editedSmartAlertTarget by remember(smartAlertTarget) { mutableStateOf(smartAlertTarget.toString()) }
    var isSettingsExpanded by remember { mutableStateOf(false) }
    var showQuantAuditDialog by remember { mutableStateOf(false) }

    if (showQuantAuditDialog) {
        QuantAuditDebugDialog(viewModel = viewModel) {
            showQuantAuditDialog = false
        }
    }

    var showVipOnly by remember { mutableStateOf(true) }
    var selectedMaxPayoutDelayMonths by remember { mutableStateOf(4) }

    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isBiometricUnlocked by viewModel.isBiometricUnlocked.collectAsState()

    // Calibration settings state representing user's adjustable criteria
    val minDpsYieldTarget by viewModel.minDpsYieldTarget.collectAsState()
    val minGrowthTarget by viewModel.minGrowthTarget.collectAsState()
    val hiddenSignals by viewModel.hiddenSignals.collectAsState()

    var tempDps by remember(minDpsYieldTarget) { mutableStateOf(minDpsYieldTarget) }
    var tempGrowth by remember(minGrowthTarget) { mutableStateOf(minGrowthTarget) }
    var tempAlertTarget by remember(smartAlertTarget) { mutableStateOf(smartAlertTarget) }

    // Tab state (1: Home/خانه , 2: Invest/سود هوشمند , 3: Settings & Simulator/حساب کاربری)
    var currentTab by remember { mutableStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }

    // Colors mapping to support both beautiful Light & Dark modes
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    val backgroundCol = if (isDarkMode) DarkBackground else Color(0xFFFAF8F5) // Soft premium sand-cream
    val surfaceCol = if (isDarkMode) DarkSurface else Color(0xFFFFFFFF)       // Elegant card surface
    val surfaceVariantCol = if (isDarkMode) DarkSurfaceVariant else Color(0xFFF3EFE9) // Beige sand-variant
    val textCol = if (isDarkMode) Color(0xFFF5F3EF) else Color(0xFF1F1B18)    // Soft off-white vs warm charcoal brown
    val mutedCol = if (isDarkMode) SlateTextSecondary else Color(0xFF6B6259)  // Slate-grey vs warm brown subtext
    val borderCol = if (isDarkMode) BorderColor else Color(0xFFDFD9D0)        // Warm sand border line
    val goldAccent = if (isDarkMode) GoldPrimary else Color(0xFFB39256)        // Majestic noble gold highlight
    val goldSecondaryAccent = if (isDarkMode) Color(0xFFE6C587) else Color(0xFF9E7E45) // Light gold secondary highlight

    if (isBiometricEnabled && !isBiometricUnlocked) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            showBiometricPrompt(context) { success ->
                if (success) {
                    viewModel.updateBiometricUnlockState(true)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundCol),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(goldSecondaryAccent.copy(alpha = 0.1f), CircleShape)
                        .border(2.dp, goldSecondaryAccent, CircleShape)
                        .clickable {
                            showBiometricPrompt(context) { success ->
                                if (success) {
                                    viewModel.updateBiometricUnlockState(true)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "🔒",
                        tint = goldSecondaryAccent,
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "ورود با اثر انگشت سخت‌افزاری",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textCol
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "برای نمایش سیگنال‌های محرمانه بورس، امنیت بیومتریک دستگاه را تایید کنید",
                    fontSize = 12.sp,
                    color = mutedCol,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        showBiometricPrompt(context) { success ->
                            if (success) {
                                viewModel.updateBiometricUnlockState(true)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = goldSecondaryAccent)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اسکن مجدد بورد سخت‌افزاری 🧬", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(goldAccent.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                showQuantAuditDialog = true
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = goldSecondaryAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "TSE Master",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "VIP",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen,
                                        modifier = Modifier
                                            .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(NeonGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (useLiveCrawler) "LIVE TSETMC CONNECTED" else "OFFLINE BACKUP ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = mutedCol,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "تغییر تم",
                                tint = textCol
                            )
                        }
                        IconButton(onClick = { viewModel.fetchMarketData() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "به‌روزرسانی داده‌ها",
                                tint = textCol
                            )
                        }
                        IconButton(onClick = { viewModel.deactivateApp() }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "خروج",
                                tint = StockRed
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundCol),
                    modifier = Modifier.border(1.dp, borderCol.copy(alpha = 0.5f))
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = surfaceCol,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(1.dp, borderCol, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        label = { Text("خانه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentTab == 1) goldAccent else mutedCol) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null, tint = if (currentTab == 1) goldAccent else mutedCol) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        label = { Text("سود هوشمند", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentTab == 2) goldAccent else mutedCol) },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = if (currentTab == 2) goldAccent else mutedCol) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        label = { Text("تنظیمات و شبکه داده", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentTab == 3) goldAccent else mutedCol) },
                        icon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (currentTab == 3) goldAccent else mutedCol) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(backgroundCol)
                    .drawBehind {
                        val paintText = android.graphics.Paint().apply {
                            color = if (isDarkMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                            alpha = if (isDarkMode) 10 else 6
                            textSize = 40f
                            isAntiAlias = true
                        }
                        rotate(degrees = -30f) {
                            for (x in -500..size.width.toInt() + 500 step 450) {
                                for (y in -500..size.height.toInt() + 500 step 350) {
                                    drawContext.canvas.nativeCanvas.drawText(
                                        "VIP MEM $userMobile",
                                        x.toFloat(),
                                        y.toFloat(),
                                        paintText
                                    )
                                }
                            }
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Security Banner (Always visible across dashboard panels)
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = surfaceVariantCol.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = StockGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "حفاظت ضد نشت و بیومتریک بورس من فعال است (امنیت گرید بانکی)",
                                    fontSize = 11.sp,
                                    color = mutedCol,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // TAB 1: HOME PAGE
                    if (currentTab == 1) {
                        item {
                            // User Profile Status Card
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = goldAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "کاربر VIP متصل به سرور بورس",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textCol
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(goldAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "VIP ویژه",
                                                fontSize = 10.sp,
                                                color = goldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "لایسنس سخت‌افزاری فعال روی تلفن همراه: $userMobile",
                                        fontSize = 12.sp,
                                        color = textCol
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "مدت اشتراک آفلاین/آنلاین نامحدود تا تاریخ مجمع سال بعد: $expiryDate",
                                        fontSize = 11.sp,
                                        color = mutedCol
                                    )
                                }
                            }

                            // Dynamic Live Connection Switch Card
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "تغییر منبع داده‌های بورس",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textCol
                                            )
                                            Text(
                                                text = if (useLiveCrawler) "اتصال مستقیم کراولر زنده به tsetmc.com" else "پایگاه داده آفلاین پشتیبان (تطبیق مرکزی با بورس تهران)",
                                                fontSize = 11.sp,
                                                color = mutedCol
                                            )
                                        }
                                        Switch(
                                            checked = useLiveCrawler,
                                            onCheckedChange = { viewModel.toggleLiveCrawler(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = goldAccent,
                                                checkedTrackColor = goldAccent.copy(alpha = 0.4f)
                                            )
                                        )
                                    }
                                }
                            }

                            // CBI inflation data crawler verification module
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                val cbiCrawlStatus by viewModel.cbiCrawlStatus.collectAsState()
                                val cbiCrawlTimestamp by viewModel.cbiCrawlTimestamp.collectAsState()
                                val cbiIsLive by viewModel.cbiIsLive.collectAsState()
                                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Public,
                                                contentDescription = null,
                                                tint = goldSecondaryAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "موتور کراولر متمرکز بانک مرکزی (CBI)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textCol
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (cbiIsLive) StockGreen.copy(alpha = 0.15f) else goldSecondaryAccent.copy(alpha = 0.15f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (cbiIsLive) "اتصال زنده بانک" else "انطباق لوکال",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (cbiIsLive) StockGreen else goldSecondaryAccent
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "این کراولر خودکار با تارنمای رسمی بانک مرکزی جمهوری اسلامی تعامل کرده تا آخرین آمارهای کلان تورم سالیانه و نقطه به نقطه کشور را استعلام کند و با بازده نقدی مجامع تطبیق دهد.",
                                        fontSize = 11.sp,
                                        color = mutedCol,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "آخرین وضعیت موتور بانک مرکزی: $cbiCrawlStatus",
                                        fontSize = 11.sp,
                                        color = textCol,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (cbiCrawlTimestamp.isNotEmpty()) {
                                        Text(
                                            text = "زمان استعلام خودکار مراجع: $cbiCrawlTimestamp",
                                            fontSize = 9.sp,
                                            color = mutedCol
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.refreshCbiInflation() },
                                            colors = ButtonDefaults.buttonColors(containerColor = goldSecondaryAccent),
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("استعلام آنلاین بانک 🔄", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { uriHandler.openUri("https://cbi.ir/Inflation/Inflation_FA.aspx") },
                                            colors = ButtonDefaults.buttonColors(containerColor = surfaceVariantCol),
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp), tint = goldAccent)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("رؤیت سند واقعی تورم 📰", fontSize = 10.sp, color = goldAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Notification log systems
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = StockGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "پایشگر پیام بورس و هشدار مجمع",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textCol
                                            )
                                        }
                                        Text(
                                            text = "هدف بازده: $smartAlertTarget٪",
                                            fontSize = 10.sp,
                                            color = StockRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "پایشگر مجمع در مدهای پس‌زمینه سهام آماده تقسیم سود بالای $smartAlertTarget٪ مجمع را ردیابی کرده و نوتیفیکیشن اندروید صادر می‌کند.",
                                        fontSize = 11.sp,
                                        color = mutedCol,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            val threshold = smartAlertTarget
                                            val highYieldStocks = allStocks.filter { it.totalReturnPotential >= threshold }
                                            if (highYieldStocks.isNotEmpty()) {
                                                highYieldStocks.take(1).forEach { item ->
                                                    viewModel.triggerLocalNotification(item.symbol, item.totalReturnPotential)
                                                }
                                            } else {
                                                viewModel.triggerLocalNotification("کشف طلایی مجمع بورس", 39.5f)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (notificationLog.isNotEmpty()) StockGreen.copy(alpha = 0.15f) else surfaceVariantCol),
                                        border = BorderStroke(1.dp, if (notificationLog.isNotEmpty()) StockGreen else borderCol),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = if (notificationLog.isNotEmpty()) StockGreen else textCol,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "تست پایش خودکار نوتیفیکیشن مجمع",
                                            fontSize = 11.sp,
                                            color = if (notificationLog.isNotEmpty()) StockGreen else textCol,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (notificationLog.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Divider(color = borderCol)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "📯 پیام‌های اخیر موتور پایشگر مجمع بورس:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StockGreen
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            notificationLog.take(4).forEach { log ->
                                                Text(
                                                    text = log,
                                                    fontSize = 10.sp,
                                                    color = textCol,
                                                    lineHeight = 14.sp,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(surfaceVariantCol.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                        .padding(8.dp)
                                                        .testTag("notification_bubble")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: INVEST & SCREENING PANEL
                    if (currentTab == 2) {
                        item {
                            // Quick Stats Overview Cards
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(
                                    title = "کل سهام بررسی شده",
                                    value = allStocks.size.toString(),
                                    viewModel = viewModel,
                                    valueColor = textCol,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "سیگنال‌های منطبق بر VIP",
                                    value = vipSignals.size.toString(),
                                    viewModel = viewModel,
                                    valueColor = StockGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Math Formula Strategy Selector
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                val selectedFormula by viewModel.selectedFormula.collectAsState()

                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "🎛️ انتخاب فرمول غربالگری هوشمند بازار (فرمول ریاضی)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = goldAccent,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = "با انتخاب هر فرمول، الگوریتم مربوطه فیلترهای ریاضی بورس را در لحظه تغییر می‌دهد:",
                                        fontSize = 10.sp,
                                        color = mutedCol,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )

                                    val formulas = listOf(
                                        Triple("ALL", "۱. كل غربالگری‌ها (مجموعه معیارها)", "تلاقی حجم بالای ۱م، ارزش م. بالای ۱۰میلیارد ریال و P/E مطلوب بین ۲ تا ۱۰"),
                                        Triple("DIVIDEND", "۲. سود نقدی طلایی مجمع (M3 Yield Beater)", "سهم‌هایی با مجمع سودده و بزرگتر از معیار درآمد ثابت ملى"),
                                        Triple("HEDGE", "۳. طلا و فلزات عایق تورمی (Defensive Shield)", "سهم‌هایی مانند طلا فزر و مس فملی برای فرار بنیادی از تورم"),
                                        Triple("DEFENSIVE", "۴. حاشیه امنیت فوق‌العاده (Safety Floor)", "سهم با سود بالا و پایین‌ترین ضریب قیمت به بازدهی (P/E بسیار ایمن)"),
                                        Triple("BEAT_INFLATION", "۵. بازدهی کل بزرگتر از تورم (CBI Beater)", "سهم‌هایی که بازده کل مجمع آن‌ها از تورم سالانه بانک مرکزی جلوتر است")
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        formulas.forEach { (id, title, desc) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (selectedFormula == id) goldSecondaryAccent.copy(alpha = 0.1f) else Color.Transparent)
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (selectedFormula == id) goldSecondaryAccent else borderCol,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { viewModel.setInvestmentFormula(id) }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = selectedFormula == id,
                                                    onClick = { viewModel.setInvestmentFormula(id) },
                                                    colors = RadioButtonDefaults.colors(selectedColor = goldAccent)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = title,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textCol
                                                    )
                                                    Text(
                                                        text = desc,
                                                        fontSize = 9.sp,
                                                        color = mutedCol
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Risk Sensitivity selector
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.FilterList,
                                                contentDescription = null,
                                                tint = goldAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "معیار و فیلتر چندسطحی ریسک بنیادی",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textCol
                                            )
                                        }
                                        val severityLabel = when (screeningSeverity) {
                                            "LOW" -> "آسان‌گیر (ریسک بالا)"
                                            "MEDIUM" -> "متعادل (متوسط)"
                                            else -> "سخت‌گیرانه ویژه (امنیت بالا)"
                                        }
                                        Text(
                                            text = severityLabel,
                                            fontSize = 9.sp,
                                            color = if (screeningSeverity == "HIGH") StockGreen else if (screeningSeverity == "MEDIUM") goldAccent else StockRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // LOW
                                        Button(
                                            onClick = { viewModel.setScreeningSeverity("LOW") },
                                            modifier = Modifier.weight(1f).height(36.dp).testTag("filter_low_button"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (screeningSeverity == "LOW") StockRed.copy(alpha = 0.15f) else surfaceVariantCol,
                                                contentColor = if (screeningSeverity == "LOW") StockRed else mutedCol
                                            ),
                                            border = BorderStroke(1.dp, if (screeningSeverity == "LOW") StockRed else borderCol)
                                        ) {
                                            Text("پایین", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // MEDIUM
                                        Button(
                                            onClick = { viewModel.setScreeningSeverity("MEDIUM") },
                                            modifier = Modifier.weight(1f).height(36.dp).testTag("filter_medium_button"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (screeningSeverity == "MEDIUM") goldSecondaryAccent.copy(alpha = 0.15f) else surfaceVariantCol,
                                                contentColor = if (screeningSeverity == "MEDIUM") goldSecondaryAccent else mutedCol
                                            ),
                                            border = BorderStroke(1.dp, if (screeningSeverity == "MEDIUM") goldSecondaryAccent else borderCol)
                                        ) {
                                            Text("متوسط", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // HIGH
                                        Button(
                                            onClick = { viewModel.setScreeningSeverity("HIGH") },
                                            modifier = Modifier.weight(1.2f).height(36.dp).testTag("filter_high_button"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (screeningSeverity == "HIGH") StockGreen.copy(alpha = 0.15f) else surfaceVariantCol,
                                                contentColor = if (screeningSeverity == "HIGH") StockGreen else mutedCol
                                            ),
                                            border = BorderStroke(1.dp, if (screeningSeverity == "HIGH") StockGreen else borderCol)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (screeningSeverity == "HIGH") StockGreen else mutedCol)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("سخت‌گیرانه ویژه", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // koli_calibration_sliders Card representing user preferred dividend yield and growth metrics
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = goldAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "کالیبراسیون تخصصی اهداف سود و پتانسیل رشد مجمع",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textCol
                                            )
                                        }
                                        if (hiddenSignals.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .background(goldAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${hiddenSignals.size} سهم فیلتر شده (خارج از کالیبر)",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = goldAccent
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Slider 1: Expected DPS Yield
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "حداقل سود تقسیمی سالانه مورد انتظار (DPS):",
                                            fontSize = 10.sp,
                                            color = mutedCol
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", tempDps)}٪",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldAccent
                                        )
                                    }
                                    Slider(
                                        value = tempDps,
                                        onValueChange = { tempDps = it },
                                        onValueChangeFinished = {
                                            viewModel.saveCalibrationSettings(tempDps, tempGrowth)
                                        },
                                        valueRange = 0f..50f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = goldAccent,
                                            activeTrackColor = goldAccent,
                                            inactiveTrackColor = borderCol
                                        ),
                                        modifier = Modifier.height(28.dp).padding(vertical = 2.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Slider 2: Min growth potential
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "حداقل پتانسیل رشد سالانه ارزش فنی سهم:",
                                            fontSize = 10.sp,
                                            color = mutedCol
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", tempGrowth)}٪",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldAccent
                                        )
                                    }
                                    Slider(
                                        value = tempGrowth,
                                        onValueChange = { tempGrowth = it },
                                        onValueChangeFinished = {
                                            viewModel.saveCalibrationSettings(tempDps, tempGrowth)
                                        },
                                        valueRange = 0f..100f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = goldAccent,
                                            activeTrackColor = goldAccent,
                                            inactiveTrackColor = borderCol
                                        ),
                                        modifier = Modifier.height(28.dp).padding(vertical = 2.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Slider 3: Alert Target Dividend (Smart Alert Target)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "کالیبره پایشگر پیام بورس (اطلاع‌رسانی سود بالای):",
                                            fontSize = 10.sp,
                                            color = mutedCol
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", tempAlertTarget)}٪",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldAccent
                                        )
                                    }
                                    Slider(
                                        value = tempAlertTarget,
                                        onValueChange = { tempAlertTarget = it },
                                        onValueChangeFinished = {
                                            viewModel.saveSmartAlertTarget(tempAlertTarget)
                                        },
                                        valueRange = 10f..60f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = goldAccent,
                                            activeTrackColor = goldAccent,
                                            inactiveTrackColor = borderCol
                                        ),
                                        modifier = Modifier.height(28.dp).padding(vertical = 2.dp)
                                    )
                                }
                            }

                            // Search Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = mutedCol) },
                                placeholder = { Text("جستجوی نام نماد یا سهم (فزر، بوعلی، شتران...)", fontSize = 11.sp, color = mutedCol) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = surfaceCol,
                                    focusedContainerColor = surfaceCol,
                                    unfocusedBorderColor = borderCol,
                                    focusedBorderColor = goldAccent,
                                    unfocusedTextColor = textCol,
                                    focusedTextColor = textCol
                                ),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 1
                            )

                            // Quick Filter Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(surfaceCol, RoundedCornerShape(12.dp))
                                    .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (showVipOnly) goldAccent else Color.Transparent)
                                        .clickable { showVipOnly = true }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "فقط سیگنال‌های VIP (${vipSignals.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (showVipOnly) backgroundCol else mutedCol
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!showVipOnly) goldAccent else Color.Transparent)
                                        .clickable { showVipOnly = false }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "کل بازار (${allStocks.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!showVipOnly) backgroundCol else mutedCol
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Payout Speed Filter component
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "📅 فیلتر مجامع بر اساس مهلت قانونی واریز نقدی سود سجا (نقدشوندگی سریع):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                     )
                                     Row(
                                         modifier = Modifier.fillMaxWidth(),
                                         horizontalArrangement = Arrangement.spacedBy(6.dp)
                                     ) {
                                         val delays = listOf(
                                             1 to "زیر ۱ ماه",
                                             2 to "کمتر ۲ ماه",
                                             3 to "زیر ۳ ماه",
                                             4 to "کل بازار"
                                         )
                                         delays.forEach { (months, label) ->
                                             val isSelected = selectedMaxPayoutDelayMonths == months
                                             Box(
                                                 modifier = Modifier
                                                     .weight(1f)
                                                     .clip(RoundedCornerShape(8.dp))
                                                     .background(if (isSelected) goldAccent.copy(alpha = 0.15f) else surfaceVariantCol)
                                                     .border(1.5.dp, if (isSelected) goldAccent else Color.Transparent, RoundedCornerShape(8.dp))
                                                     .clickable { selectedMaxPayoutDelayMonths = months }
                                                     .padding(vertical = 8.dp),
                                                 contentAlignment = Alignment.Center
                                             ) {
                                                 Text(
                                                     text = label,
                                                     fontSize = 10.sp,
                                                     fontWeight = FontWeight.Bold,
                                                     color = if (isSelected) goldAccent else mutedCol
                                                 )
                                             }
                                         }
                                     }
                                 }
                             }

                             Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Feed of dynamic card items
                        if (isLoading) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = goldAccent)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = "در حال اتصال به tsetmc و دریافت آخرین وضعیت سهام تهران...", fontSize = 11.sp, color = mutedCol)
                                }
                            }
                        } else {
                            val baseList = if (showVipOnly) vipSignals else allStocks
                            val delayFilteredList = baseList.filter { it.paymentDelayMonths <= selectedMaxPayoutDelayMonths }
                            val filteredList = if (searchQuery.trim().isEmpty()) {
                                delayFilteredList
                            } else {
                                delayFilteredList.filter { it.symbol.contains(searchQuery.trim()) || it.name.contains(searchQuery.trim()) || it.sector.contains(searchQuery.trim()) }
                            }

                            if (filteredList.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = mutedCol, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(text = "نمادی مطابق فیلتر شما در بورس یافت نشد.", fontSize = 12.sp, color = mutedCol)
                                    }
                                }
                            } else {
                                items(filteredList) { stock ->
                                    SignalCardView(stock = stock, userMobile = userMobile, viewModel = viewModel)
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }

                    // TAB 3: ACCOUNT, AI SETTINGS & LOCAL SIMULATION OVERRIDES
                    if (currentTab == 3) {
                        item {
                            // Hardware Fingerprint Switcher Card
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                val context = LocalContext.current
                                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🔐 تایید ورود با اثر انگشت سخت‌افزاری",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textCol
                                            )
                                            Text(
                                                text = "امنیت بیومتریک گوشی سخت‌افزار برای شناسایی امنیتی لایسنس ویژه و جلوگیری از نشت اطلاعات",
                                                fontSize = 11.sp,
                                                color = mutedCol
                                            )
                                        }
                                        Switch(
                                            checked = isBiometricEnabled,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    showBiometricPrompt(context) { success ->
                                                        if (success) {
                                                            viewModel.setBiometricEnabled(true)
                                                        }
                                                    }
                                                } else {
                                                    viewModel.setBiometricEnabled(false)
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = StockGreen,
                                                checkedTrackColor = StockGreen.copy(alpha = 0.4f)
                                            )
                                        )
                                    }
                                }
                            }

                            // Dynamic Gemini Credentials Configure Card
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { isSettingsExpanded = !isSettingsExpanded },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Settings, contentDescription = null, tint = goldAccent, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "پیکربندی هوش مصنوعی (Gemini-API) و مراجع کلان",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textCol
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isSettingsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = mutedCol
                                        )
                                    }

                                    if (isSettingsExpanded) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Divider(color = borderCol)
                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Free AI Model type picker
                                        Text(
                                            text = "انتخاب خودکار مدل تفسیری مراجع بورس (مدل‌های کاملاً فری گوگل):",
                                            fontSize = 10.sp,
                                            color = mutedCol
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(surfaceVariantCol, RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = goldAccent, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Gemini 1.5 Flash (بهره‌وری رایگان و آنی)",
                                                fontSize = 11.sp,
                                                color = textCol,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // API Key setting
                                        Text(
                                            text = "کی‌ورد توکن جمنای گوگل (Google AI Studio Gemini Key):",
                                            fontSize = 10.sp,
                                            color = mutedCol
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = editedApiKey,
                                            onValueChange = { editedApiKey = it },
                                            placeholder = { Text("AIzaSy...", fontSize = 11.sp, color = mutedCol) },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 1,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = borderCol,
                                                focusedBorderColor = goldAccent,
                                                unfocusedTextColor = textCol,
                                                focusedTextColor = textCol
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Custom base URL endpoints
                                        Text(
                                            text = "آدرس پروکسي/گیت‌وی دلخواه (Endpoint Base URL Override):",
                                            fontSize = 10.sp,
                                            color = mutedCol
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = editedCustomUrl,
                                            onValueChange = { editedCustomUrl = it },
                                            placeholder = { Text("https://generativelanguage.googleapis.com/", fontSize = 11.sp, color = mutedCol) },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 1,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = borderCol,
                                                focusedBorderColor = goldAccent,
                                                unfocusedTextColor = textCol,
                                                focusedTextColor = textCol
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Custom model ID parameter
                                        Text(
                                            text = "نام مدل هوش مصنوعی (Model ID):",
                                            fontSize = 10.sp,
                                            color = mutedCol
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = editedModelName,
                                            onValueChange = { editedModelName = it },
                                            placeholder = { Text("gemini-2.5-flash", fontSize = 11.sp, color = mutedCol) },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 1,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = borderCol,
                                                focusedBorderColor = goldAccent,
                                                unfocusedTextColor = textCol,
                                                focusedTextColor = textCol
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Variable Overrides
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("تورم سالانه کشور (٪):", fontSize = 9.sp, color = mutedCol)
                                                OutlinedTextField(
                                                    value = editedInflationRate,
                                                    onValueChange = { editedInflationRate = it },
                                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = borderCol, focusedBorderColor = goldAccent, unfocusedTextColor = textCol, focusedTextColor = textCol)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("تورم نقطه به نقطه (٪):", fontSize = 9.sp, color = mutedCol)
                                                OutlinedTextField(
                                                    value = editedInflationRatePointToPoint,
                                                    onValueChange = { editedInflationRatePointToPoint = it },
                                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = borderCol, focusedBorderColor = goldAccent, unfocusedTextColor = textCol, focusedTextColor = textCol)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("سود معیار درآمد ثابت ملى:", fontSize = 9.sp, color = mutedCol)
                                                OutlinedTextField(
                                                    value = editedBankIncomeRate,
                                                    onValueChange = { editedBankIncomeRate = it },
                                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = borderCol, focusedBorderColor = goldAccent, unfocusedTextColor = textCol, focusedTextColor = textCol)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("هدف بازده طلایی مجمع (٪):", fontSize = 9.sp, color = mutedCol)
                                                OutlinedTextField(
                                                    value = editedSmartAlertTarget,
                                                    onValueChange = { editedSmartAlertTarget = it },
                                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = borderCol, focusedBorderColor = goldAccent, unfocusedTextColor = textCol, focusedTextColor = textCol)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // SHA-256 fingerprint verification input field
                                        Text(
                                            text = "انطباق گواهی امضا ضدکرک (Digital Signature SHA-256 Fingerprint):",
                                            fontSize = 9.sp,
                                            color = mutedCol
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = editedSha256,
                                            onValueChange = { editedSha256 = it },
                                            placeholder = { Text("BYPASS (یا مقدار هگزادسیمال SHA-256 بدون کولون)", fontSize = 10.sp, color = mutedCol) },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 1,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = borderCol,
                                                focusedBorderColor = goldAccent,
                                                unfocusedTextColor = textCol,
                                                focusedTextColor = textCol
                                            )
                                        )
                                        Text(
                                            text = "برای فعال‌سازی سیستم هوشمند انطباق گواهی امضای بازار، گواهی SHA-256 فروشگاه انتشاردهنده را اینجا تنظیم کنید. برای کاربری بدون محدودیت شبیه‌ساز در فاز توسعه، مقدار را BYPASS قرار دهید.",
                                            fontSize = 8.sp,
                                            color = mutedCol,
                                            lineHeight = 12.ssp()
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                val inf = editedInflationRate.toFloatOrNull() ?: 48.3f
                                                val infP2P = editedInflationRatePointToPoint.toFloatOrNull() ?: 53.9f
                                                val bnk = editedBankIncomeRate.toFloatOrNull() ?: 30.0f
                                                val tgt = editedSmartAlertTarget.toFloatOrNull() ?: 35.0f

                                                viewModel.saveGeminiSettings(editedApiKey, editedCustomUrl, editedModelName)
                                                viewModel.saveMacroSettings(inf, infP2P, bnk, tgt)
                                                viewModel.saveSecuritySettings(editedSha256)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("ذخیره تمام تنظیمات کلان بورس", fontSize = 11.sp, color = backgroundCol, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Simulated Shares overrides
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                val customDpsList by viewModel.customDpsList.collectAsState()

                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "📂 پورتفوی فرضی بورس من (تحلیل آفلاین ذخیره Room)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol
                                    )
                                    Text(
                                        text = "سود‌های نقدی تاریخی یا پارامترهای مجمع عمومی این شرکت‌ها به صورت تفضیلی دستکاری یا سفارشی‌سازی شده‌اند:",
                                        fontSize = 10.sp,
                                        color = mutedCol,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    if (customDpsList.isEmpty()) {
                                        Text(
                                            text = "در حال حاضر هیچ شرکتی شخصی‌سازی نشده است. شما می‌توانید با ضربه زدن روی کارت هر شرکت در برگه 'سود هوشمند' و سپس دکمه 'اصلاح مجمع'، مقادیر تاریخی DPS شرکت و پتانسيل رشد آن را وارد نموده و با فرمول مجمع محلی خود در SQLite ذخیره کنید.",
                                            fontSize = 10.sp,
                                            color = mutedCol,
                                            lineHeight = 15.sp
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            customDpsList.forEach { dpsItem ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(surfaceVariantCol, RoundedCornerShape(8.dp))
                                                        .padding(8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "نماد: ${dpsItem.symbol}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = textCol
                                                        )
                                                        Text(
                                                            text = "رشد فرضی: ${dpsItem.growthPotential}٪ | مجمع: ${dpsItem.keyMonths}",
                                                            fontSize = 9.sp,
                                                            color = mutedCol
                                                        )
                                                    }
                                                    IconButton(onClick = { viewModel.deleteCustomStockDps(dpsItem.symbol) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "حذف فرضیات", tint = StockRed, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    viewModel: TradingViewModel,
    valueColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val bg = if (isDarkMode) Color(0xFF131926) else Color(0xFFFFFFFF)
    val borderCol = if (isDarkMode) Color(0xFF202B3D) else Color(0xFFDFD9D0)
    val textPrimary = if (isDarkMode) Color(0xFFF5F3EF) else Color(0xFF1F1B18)
    val textMuted = if (isDarkMode) Color(0xFF858E9C) else Color(0xFF6B6259)
    val finalValCol = valueColor ?: textPrimary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = textMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = finalValCol)
        }
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) GoldPrimary else Color.Transparent
    val textColor = if (isSelected) DarkBackground else SlateTextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignalCardView(stock: StockSignal, userMobile: String, viewModel: TradingViewModel) {
    val aiResultMap by viewModel.aiAnalysisResult.collectAsState()
    val aiLoadingMap by viewModel.aiAnalysisLoading.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    // Theme-aligned responsive variables
    val cardBackground = if (isDarkMode) Color(0xFF131926) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkMode) Color(0xFF202B3D) else Color(0xFFDFD9D0)
    val textPrimary = if (isDarkMode) Color(0xFFF5F3EF) else Color(0xFF1F1B18)
    val textMuted = if (isDarkMode) Color(0xFF858E9C) else Color(0xFF6B6259)
    val dividerColor = if (isDarkMode) Color(0xFF1C2436) else Color(0xFFF3EFE9)
    val itemBg = if (isDarkMode) Color(0xFF1C2436) else Color(0xFFFAF8F5)
    val goldClr = if (isDarkMode) GoldPrimary else Color(0xFFB39256)
    
    val currentAiResult = aiResultMap[stock.symbol]
    val currentAiLoading = aiLoadingMap[stock.symbol] ?: false

    var expanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showDemoDialog by remember { mutableStateOf(false) }
    var investmentAmountText by remember { mutableStateOf("100") }
    var showCalcDialog by remember { mutableStateOf(false) }

    // State bindings for direct manual user inputs
    var yr1 by remember(stock) { mutableStateOf(stock.historicalDps.getOrElse(0) { 100 }.toString()) }
    var yr2 by remember(stock) { mutableStateOf(stock.historicalDps.getOrElse(1) { 150 }.toString()) }
    var yr3 by remember(stock) { mutableStateOf(stock.historicalDps.getOrElse(2) { 200 }.toString()) }
    var yr4 by remember(stock) { mutableStateOf(stock.historicalDps.getOrElse(3) { 250 }.toString()) }
    var yr5 by remember(stock) { mutableStateOf(stock.historicalDps.getOrElse(4) { 300 }.toString()) }
    var growthInput by remember(stock) { mutableStateOf(stock.growthPotential.toString()) }
    var keyMonthsInput by remember(stock) { mutableStateOf(stock.keyDateMilestones) }

    val isPositive = stock.changePercent >= 0f
    val colorText = if (isPositive) StockGreen else StockRed
    val formattedPrice = formatPrice(stock.price)
    val formattedToman = formatPrice(stock.priceInToman)
    val formattedVolume = formatVolume(stock.volume)
    val formattedValueToman = formatPrice(stock.valueInToman)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .drawBehind {
                // Inside card we also generate subtle mobile watermark lines to prevent snapshot sharing
                val paintText = android.graphics.Paint().apply {
                    color = if (isDarkMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    alpha = if (isDarkMode) 8 else 4
                    textSize = 32f
                    isAntiAlias = true
                }
                rotate(degrees = -15f) {
                    drawContext.canvas.nativeCanvas.drawText("USER: $userMobile", 30f, 60f, paintText)
                    drawContext.canvas.nativeCanvas.drawText("VIP BOURSE", size.width - 200f, size.height - 30f, paintText)
                }
            },
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(
            width = if (stock.isVipSignal) 1.5.dp else 1.dp,
            color = if (stock.isVipSignal) goldClr.copy(alpha = 0.6f) else cardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (stock.isVipSignal) goldClr else dividerColor,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stock.symbol,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stock.isVipSignal) Color(0xFF0C0E14) else textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stock.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                }

                // Growth badge & Calculator icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(goldClr.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.5.dp, goldClr.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable { showCalcDialog = true }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "محاسبه مجمع",
                                tint = goldClr,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🧮 ماشین‌حساب بازدهی مجمع",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldClr
                            )
                        }
                    }

                    Text(
                        text = (if (isPositive) "+" else "") + String.format(Locale.US, "%.2f", stock.changePercent) + "%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = dividerColor.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Fundamental Stock parameters grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("قیمت پایانی", fontSize = 10.sp, color = textMuted)
                    Text("$formattedPrice ریال", fontSize = 12.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                    Text("$formattedToman تومان", fontSize = 11.sp, color = goldClr, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("حجم معاملات", fontSize = 10.sp, color = textMuted)
                    Text(formattedVolume, fontSize = 12.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                    Text("P/E: ${String.format(Locale.US, "%.1f", stock.pe)}", fontSize = 11.sp, color = textMuted)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("ارزش معاملات", fontSize = 10.sp, color = textMuted)
                    Text("$formattedValueToman ت", fontSize = 12.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                    Text("EPS: ${stock.eps}", fontSize = 11.sp, color = textMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Crucial requested DPS dividend calculations badge!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(goldClr.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = goldClr,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "پیش‌بینی سود نقدی سهم (DPS):",
                        fontSize = 11.sp,
                        color = textMuted
                    )
                }
                Text(
                    text = "${formatPrice(stock.dps.toLong())} ریال (${String.format(Locale.US, "%.1f", stock.dpsYield)}% بازدهی)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) StockGreen else Color(0xFF1B5E20)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Payment Delay Speed Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (stock.paymentDelayMonths <= 1) StockGreen.copy(alpha = 0.1f)
                        else if (stock.paymentDelayMonths <= 2) goldClr.copy(alpha = 0.1f)
                        else dividerColor.copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (stock.paymentDelayMonths <= 1) StockGreen else goldClr,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مهلت قانونی واریز نقدی (سجا/پایا):",
                        fontSize = 10.sp,
                        color = textMuted
                    )
                }
                val delayLabel = when (stock.paymentDelayMonths) {
                    1 -> "فوق سریع (زیر ۳۰ روز) ⚡"
                    2 -> "سریع (کمتر از ۶۰ روز) ⏱️"
                    3 -> "متوسط (زیر ۹۰ روز)"
                    else -> "عادی (تا ۴ ماه قانونی فرجه)"
                }
                Text(
                    text = delayLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (stock.paymentDelayMonths <= 1) StockGreen else textPrimary
                )
            }

            // Expose VIP detail explanations if expanded
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Divider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Realistic Dual Level tags & annual target potential representation requested by user
                    Text(
                        text = "⚖️ ارزیابی سطح سود مجمع (واقع‌بینانه):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Displaying the active rating level
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(goldClr.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .border(1.dp, goldClr.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = stock.dpsLevel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "حداقل سود تضمینی بانکی مرجع: ۲۲.۰٪ | پتانسیل رشد سرمایه‌گذاری سالیانه: ${stock.growthPotential}٪ | پتانسیل کل مجمع (سود + رشد): ${String.format(Locale.US, "%.1f", stock.totalReturnPotential)}٪",
                                fontSize = 10.sp,
                                color = textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📅 تاریخ‌های کلیدی و برنامه زمانی مجمع:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )
                    Text(
                        text = stock.keyDateMilestones,
                        fontSize = 11.sp,
                        color = textPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- 🌟 Premium Quant Insight Card 🌟 ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1B2232) else Color(0xFFFAF7F2)),
                        border = BorderStroke(1.dp, goldClr.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = goldClr,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "جعبه ابزار کوانت تخصصی TSE (سنجش لایسنس VIP)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Circular Gauge for Master Score
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            progress = 1f,
                                            strokeWidth = 6.dp,
                                            color = dividerColor.copy(alpha = 0.5f)
                                        )
                                        CircularProgressIndicator(
                                            progress = stock.masterScore / 100f,
                                            strokeWidth = 6.dp,
                                            color = goldClr
                                        )
                                        Text(
                                            text = "${stock.masterScore.toInt()}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = textPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "شاخص کل کوانت (Master)",
                                        fontSize = 9.sp,
                                        color = textMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                // 2. Shield Icons representing Inflation Hedge Rating
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = if (i <= stock.inflationHedgeRating) goldClr else dividerColor.copy(alpha = 0.3f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val shieldLabel = when (stock.inflationHedgeRating) {
                                        5 -> "سطح ۵: عایق ضد تورم کامل 🛡️"
                                        4 -> "سطح ۴: سپر تورمی ویژه"
                                        else -> "سطح ۳: بهینه‌سازی تورمی متوازن"
                                    }
                                    Text(
                                        text = shieldLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = goldClr,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "امتیاز عایق تورم (IHS)",
                                        fontSize = 8.sp,
                                        color = textMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = dividerColor.copy(alpha = 0.3f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // 3. Dividend Countdown section
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(goldClr.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = null,
                                    tint = goldClr,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "سرعت پرداخت نقدی مصوب کدال (سجام/پایا):",
                                        fontSize = 10.sp,
                                        color = textMuted
                                    )
                                    Text(
                                        text = stock.dividendCountdownText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // --- 🧮 Inline Stock Calculator Card ---
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = goldClr.copy(alpha = 0.05f)),
                        border = BorderStroke(1.2.dp, goldClr.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = goldClr,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ماشین‌حساب هوشمند سود نو مجمع (${stock.symbol})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "پیش‌بینی دقیق کارمزد خرید بورس، قیمت تمام‌شده مواجهه مجمع، سود نقدی تقسیمی و برآورد سهام جایزه فزر، شپدیس و ...",
                                fontSize = 9.sp,
                                color = textMuted
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = investmentAmountText,
                                onValueChange = { investmentAmountText = it },
                                label = { Text("مبلغ سرمایه‌گذاری مورد نظر (میلیون تومان)", fontSize = 11.sp, color = textMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                suffix = { Text("میلیون تومان", fontSize = 10.sp, color = textMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = {
                                    Icon(Icons.Default.Savings, contentDescription = null, tint = goldClr, modifier = Modifier.size(16.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cardBorder,
                                    focusedBorderColor = goldClr,
                                    unfocusedTextColor = textPrimary,
                                    focusedTextColor = textPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Shortcut buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "۱۰" to "۱۰ میلیون",
                                    "۵۰" to "۵۰ میلیون",
                                    "۱۰۰" to "۱۰۰ میلیون",
                                    "۵۰۰" to "۵۰۰ میلیون"
                                ).forEach { (valStr, label) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (investmentAmountText == valStr) goldClr else itemBg,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .border(1.dp, if (investmentAmountText == valStr) goldClr else cardBorder, RoundedCornerShape(6.dp))
                                            .clickable { investmentAmountText = valStr }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (investmentAmountText == valStr) Color(0xFF0C0E14) else textPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = dividerColor, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Compute values
                            val cleanNumInline = investmentAmountText.replace(Regex("[^0-9.]"), "")
                            val amountTomansDoubleVal = cleanNumInline.toDoubleOrNull() ?: 100.0
                            val actualTomanValueVal = amountTomansDoubleVal * 1_000_000.0

                            val commissionFeeVal = actualTomanValueVal * 0.003712
                            val netPurchaseAmountVal = actualTomanValueVal - commissionFeeVal
                            val sharePriceTomanVal = stock.priceInToman
                            val sharesCountVal = if (sharePriceTomanVal > 0) (netPurchaseAmountVal / sharePriceTomanVal).toLong() else 0L
                            val costBasisPerShareVal = if (sharesCountVal > 0) (actualTomanValueVal / sharesCountVal) else sharePriceTomanVal.toDouble()
                            val dpsTomanVal = stock.dps / 10.0
                            val totalCashDividendVal = sharesCountVal * dpsTomanVal

                            val capIncreasePercentVal = when (stock.symbol) {
                                "فزر" -> 80f
                                "بوعلی" -> 40f
                                "شپدیس" -> 60f
                                "فملی" -> 50f
                                "فولاد" -> 70f
                                "شتران" -> 75f
                                "وغدیر" -> 45f
                                else -> 30f
                            }
                            val bonusSharesCountVal = (sharesCountVal * (capIncreasePercentVal / 100.0)).toLong()

                            // Annual Growth and compound calculation
                            val finalCapitalValueVal = netPurchaseAmountVal * (1.0 + stock.totalReturnPotential / 100.0)
                            val netCompoundProfitVal = finalCapitalValueVal - actualTomanValueVal

                            // Details Grid
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("کارمزد خرید کارگزاری (۰.۳۷٪ قانون بورس):", fontSize = 10.sp, color = textMuted)
                                    Text("${formatPrice(commissionFeeVal.toLong())} تومان", fontSize = 11.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("تعداد کل برگ سهام قابل خرید:", fontSize = 10.sp, color = textMuted)
                                    Text("${formatPrice(sharesCountVal)} برگه سهم", fontSize = 11.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("قیمت تمام شده هر برگ سهم (با کارمزد):", fontSize = 10.sp, color = textMuted)
                                    Text("${formatPrice(costBasisPerShareVal.toLong())} تومان", fontSize = 11.sp, color = goldClr, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("قیمت جاری روی تابلو TSETMC:", fontSize = 10.sp, color = textMuted)
                                    Text("${formatPrice(sharePriceTomanVal)} تومان", fontSize = 11.sp, color = textPrimary)
                                }

                                Divider(color = dividerColor.copy(alpha = 0.5f), thickness = 1.dp)

                                Text(
                                    text = "📊 پیش‌بینی نهایی سرمایه و دریافتی‌های مجمع:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = goldClr
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StockGreen.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StockGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("عایدی خالص نقدی سود مجمع (Sajam):", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text("${formatPrice(totalCashDividendVal.toLong())} تومان", fontSize = 11.sp, color = StockGreen, fontWeight = FontWeight.Black)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(goldClr.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = goldClr, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("سهام جایزه اعطایی رایگان مجمع ($capIncreasePercentVal%):", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text("${formatPrice(bonusSharesCountVal)} برگه سهم جایزه", fontSize = 11.sp, color = goldClr, fontWeight = FontWeight.Black)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(goldClr.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .border(1.dp, goldClr, RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("ارزش کل دارایی و سودها بعد از یک سال:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                        Text("شامل اصل سرمایه + سود نهایی مرکب مجمع", fontSize = 8.sp, color = textMuted)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${formatPrice(finalCapitalValueVal.toLong())} تومان", fontSize = 13.sp, color = textPrimary, fontWeight = FontWeight.Black)
                                        Text("(+${formatPrice(netCompoundProfitVal.toLong())} عایدی مرکب)", fontSize = 10.sp, color = StockGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📈 تاریخچه ۵ سال اخیر سود تقسیمی (کدال):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )
                    
                    // Historical payout horizontal bar representing the last 5 years
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        stock.historicalDps.forEachIndexed { idx, value ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(itemBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, cardBorder, RoundedCornerShape(6.dp))
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (idx == 4) "سال جاری" else "${5 - idx} سال قبل",
                                    fontSize = 8.sp,
                                    color = textMuted
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${formatPrice(value.toLong())}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (idx == 4) (if (isDarkMode) StockGreen else Color(0xFF2E7D32)) else textPrimary
                                )
                                Text(
                                    text = "ریال",
                                    fontSize = 8.sp,
                                    color = textMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "⚡ تحلیل انطباق فیلتر سخت‌افزاری VIP:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )
                    Text(
                        text = stock.reason,
                        fontSize = 11.sp,
                        color = textPrimary,
                        lineHeight = 16.ssp(),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "صنعت: ${stock.sector}",
                        fontSize = 10.sp,
                        color = textMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🔗 استعلام صحت اطلاعات (اسناد و مراجع واقعی بورس):",
                        fontSize = 11.sp,
                        color = goldClr,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        
                        // TSETMC Link Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(itemBg, RoundedCornerShape(8.dp))
                                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    // Open the specific TSETMC search page for this symbol
                                    uriHandler.openUri("https://main.tsetmc.com/InstSearch?val=${stock.symbol}")
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Launch, contentDescription = null, tint = goldClr, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("سند تابلوی TSETMC", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        // CODAL Search Link Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(itemBg, RoundedCornerShape(8.dp))
                                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    // Open official CODAL report query with stock Farsi symbol
                                    uriHandler.openUri("https://www.codal.ir/Search.aspx?Symbol=${stock.symbol}")
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Launch, contentDescription = null, tint = goldClr, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("سند مجمع در CODAL", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = cardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        // Expandable forms for custom modifications (persisted in offline Room Database)
                        Text(
                            text = "🛠️ تصحیح دستی سودهای ۵ سال گذشته (آفلاین / کدال):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldClr
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = yr1,
                                onValueChange = { yr1 = it },
                                label = { Text("۵ سال قبل", fontSize = 8.sp, color = textMuted) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cardBorder,
                                    focusedBorderColor = goldClr,
                                    unfocusedLabelColor = textMuted,
                                    focusedLabelColor = goldClr,
                                    unfocusedTextColor = textPrimary,
                                    focusedTextColor = textPrimary
                                )
                            )
                            OutlinedTextField(
                                value = yr2,
                                onValueChange = { yr2 = it },
                                label = { Text("۴ سال قبل", fontSize = 8.sp, color = textMuted) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cardBorder,
                                    focusedBorderColor = goldClr,
                                    unfocusedLabelColor = textMuted,
                                    focusedLabelColor = goldClr,
                                    unfocusedTextColor = textPrimary,
                                    focusedTextColor = textPrimary
                                )
                            )
                            OutlinedTextField(
                                value = yr3,
                                onValueChange = { yr3 = it },
                                label = { Text("۳ سال قبل", fontSize = 8.sp, color = textMuted) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cardBorder,
                                    focusedBorderColor = goldClr,
                                    unfocusedLabelColor = textMuted,
                                    focusedLabelColor = goldClr,
                                    unfocusedTextColor = textPrimary,
                                    focusedTextColor = textPrimary
                                )
                            )
                            OutlinedTextField(
                                value = yr4,
                                onValueChange = { yr4 = it },
                                label = { Text("۲ سال قبل", fontSize = 8.sp, color = textMuted) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cardBorder,
                                    focusedBorderColor = goldClr,
                                    unfocusedLabelColor = textMuted,
                                    focusedLabelColor = goldClr,
                                    unfocusedTextColor = textPrimary,
                                    focusedTextColor = textPrimary
                                )
                            )
                            OutlinedTextField(
                                value = yr5,
                                onValueChange = { yr5 = it },
                                label = { Text("سال جاری (DPS)", fontSize = 8.sp, color = textMuted) },
                                modifier = Modifier.weight(1.2f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cardBorder,
                                    focusedBorderColor = goldClr,
                                    unfocusedLabelColor = textMuted,
                                    focusedLabelColor = goldClr,
                                    unfocusedTextColor = textPrimary,
                                    focusedTextColor = textPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = growthInput,
                                onValueChange = { growthInput = it },
                                label = { Text("پتانسیل رشد ٪", fontSize = 10.sp, color = textMuted) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cardBorder,
                                    focusedBorderColor = goldClr,
                                    unfocusedLabelColor = textMuted,
                                    focusedLabelColor = goldClr,
                                    unfocusedTextColor = textPrimary,
                                    focusedTextColor = textPrimary
                                )
                            )
                            OutlinedTextField(
                                value = keyMonthsInput,
                                onValueChange = { keyMonthsInput = it },
                                label = { Text("مناسبت مجمع سالانه", fontSize = 10.sp, color = textMuted) },
                                modifier = Modifier.weight(2f),
                                maxLines = 1,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cardBorder,
                                    focusedBorderColor = goldClr,
                                    unfocusedLabelColor = textMuted,
                                    focusedLabelColor = goldClr,
                                    unfocusedTextColor = textPrimary,
                                    focusedTextColor = textPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val valYr1 = yr1.toIntOrNull() ?: 0
                                    val valYr2 = yr2.toIntOrNull() ?: 0
                                    val valYr3 = yr3.toIntOrNull() ?: 0
                                    val valYr4 = yr4.toIntOrNull() ?: 0
                                    val valYr5 = yr5.toIntOrNull() ?: 0
                                    val valGrowth = growthInput.toFloatOrNull() ?: 15.0f

                                    viewModel.saveCustomStockDps(
                                        symbol = stock.symbol,
                                        dpsYr1 = valYr1,
                                        dpsYr2 = valYr2,
                                        dpsYr3 = valYr3,
                                        dpsYr4 = valYr4,
                                        dpsYr5 = valYr5,
                                        growthPotential = valGrowth,
                                        keyMonths = keyMonthsInput
                                    )
                                    isEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDarkMode) StockGreen else Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("ذخیره در استوریج Room", fontSize = 11.sp, color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteCustomStockDps(stock.symbol)
                                    isEditing = false
                                },
                                border = BorderStroke(1.dp, StockRed),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StockRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("بازنشانی پیش‌فرض", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.buttonColors(containerColor = goldClr),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اصلاح و شخصی‌سازی دستی سود نقدی سال‌های گذشته مجمع",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = BorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "🤖 دستیار هوشمند تحلیل سهم (TSE VIP AI):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (currentAiResult != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, goldClr.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            color = if (isDarkMode) Color(0xFF0A0D14) else Color(0xFFFAF8F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = currentAiResult,
                                    fontSize = 11.sp,
                                    color = textPrimary,
                                    lineHeight = 18.ssp()
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val context = LocalContext.current
                                    val clipboardManager = LocalClipboardManager.current
                                    Button(
                                        onClick = {
                                            val textToCopy = """
                                                📢 **سیگنال و تحلیل هوشمند بنیادی مجمع VIP**
                                                
                                                🔹 **نماد:** #${stock.symbol} (${stock.name})
                                                🔹 **صنعت بورس:** ${stock.sector}
                                                🔹 **سود مصوب نقدی (DPS):** ${stock.dps} ریال ([بازده نقدی: ${String.format(Locale.US, "%.1f", stock.dpsYield)}%])
                                                🔹 **فرجه تسویه واریز نقدی (سجا):** ${stock.paymentDelayMonths} ماهه
                                                🔹 **ضریب ایمنی مالی مجمع:** کلاس ${stock.dpsLevel}
                                                🔹 **مجموع بازدهی مورد انتظار مجمع:** ${String.format(Locale.US, "%.1f", stock.totalReturnPotential)}% (پاسخگوی تورم نقطه به نقطه کشور)
                                                
                                                📅 **رویدادها و الزامات مجمع:**
                                                ${stock.keyDateMilestones}
                                                
                                                🤖 **تحلیل دستیار هوش مصنوعی (Gemini Live API):**
                                                $currentAiResult
                                                
                                                💥 کانال اختصاصی مجمع‌یاب هوش کارآفرین بورس
                                                🛑 @MajeebVIP_Bourse
                                            """.trimIndent()
                                            clipboardManager.setText(AnnotatedString(textToCopy))
                                            android.widget.Toast.makeText(context, "گزارش زنده تلگرامی کپی شد!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D9BF0)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("کپی تلگرام 📢", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            generateStockPdfReport(context, stock, currentAiResult, isDarkMode)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("📥 دانلود PDF", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.analyzeStockWithAI(stock.symbol, stock) },
                                        colors = ButtonDefaults.buttonColors(containerColor = goldClr.copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, goldClr),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("بروزرسانی 🔄", fontSize = 9.sp, color = goldClr)
                                    }
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.analyzeStockWithAI(stock.symbol, stock) },
                            colors = ButtonDefaults.buttonColors(containerColor = goldClr),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (currentAiLoading) {
                                CircularProgressIndicator(color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), modifier = Modifier.size(16.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تحلیل و پایش آنی سهم با هوش مصنوعی (جمنای)", fontSize = 11.sp, color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { showDemoDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = goldClr.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, goldClr),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = goldClr,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🎯 مشاهده آفلاین نمونه تحلیل هوشمند هوش مصنوعی (VIP)", fontSize = 11.sp, color = goldClr, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    
    if (showDemoDialog) {
        AiAnalysisDemoDialog(
            stock = stock,
            onDismiss = { showDemoDialog = false },
            isDarkMode = isDarkMode
        )
    }

    if (showCalcDialog) {
        AlertDialog(
            onDismissRequest = { showCalcDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = goldClr,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "محاسبه‌گر پیش‌بینی مجمع و سود مرکب ${stock.symbol}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "با وارد کردن کل سرمایه مورد نظر خود به میلیون تومان، میزان کارمزد بورس، قیمت تمام‌شده واقعی، سود نقدی واریزی مجمع و سهام جایزه حاصله را فوری محاسبه و پیش‌بینی کنید.",
                        fontSize = 11.sp,
                        color = textMuted,
                        lineHeight = 16.ssp()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = investmentAmountText,
                        onValueChange = { investmentAmountText = it },
                        label = { Text("کل مبلغ سرمایه‌گذاری (میلیون تومان)", fontSize = 11.sp, color = textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        suffix = { Text("میلیون تومان", fontSize = 10.sp, color = textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = goldClr, modifier = Modifier.size(16.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = cardBorder,
                            focusedBorderColor = goldClr,
                            unfocusedTextColor = textPrimary,
                            focusedTextColor = textPrimary
                        )
                    )

                    // Shortcuts block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "۱۰" to "۱۰ م",
                            "۵۰" to "۵۰ م",
                            "۱۰۰" to "۱۰۰ م",
                            "۵۰۰" to "۵۰۰ م"
                        ).forEach { (valStr, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (investmentAmountText == valStr) goldClr else itemBg,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(1.dp, if (investmentAmountText == valStr) goldClr else cardBorder, RoundedCornerShape(6.dp))
                                    .clickable { investmentAmountText = valStr }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (investmentAmountText == valStr) Color(0xFF0C0E14) else textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = dividerColor.copy(alpha = 0.5f), thickness = 1.dp)

                    // Calculation logic conversion
                    val cleanNum = investmentAmountText.replace(Regex("[^0-9.]"), "")
                    val amountTomansDouble = cleanNum.toDoubleOrNull() ?: 100.0
                    val actualTomanValue = amountTomansDouble * 1_000_000.0

                    val commissionFee = actualTomanValue * 0.003712
                    val netPurchaseAmount = actualTomanValue - commissionFee
                    val sharePriceToman = stock.priceInToman
                    val sharesCount = if (sharePriceToman > 0) (netPurchaseAmount / sharePriceToman).toLong() else 0L
                    val costBasisPerShare = if (sharesCount > 0) (actualTomanValue / sharesCount) else sharePriceToman.toDouble()
                    val dpsToman = stock.dps / 10.0
                    val totalCashDividend = sharesCount * dpsToman

                    val capIncreasePercent = when (stock.symbol) {
                        "فزر" -> 80f
                        "بوعلی" -> 40f
                        "شپدیس" -> 60f
                        "فملی" -> 50f
                        "فولاد" -> 70f
                        "شتران" -> 75f
                        "وغدیر" -> 45f
                        else -> 30f
                    }
                    val bonusSharesCount = (sharesCount * (capIncreasePercent / 100.0)).toLong()

                    // Annual compound projection
                    val finalCapitalValue = netPurchaseAmount * (1.0 + stock.totalReturnPotential / 100.0)
                    val netCompoundProfit = finalCapitalValue - actualTomanValue

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("کارمزد کل خرید (۰.۳۷٪ قانونی بورس):", fontSize = 10.sp, color = textMuted)
                            Text("${formatPrice(commissionFee.toLong())} تومان", fontSize = 11.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تعداد کل سهام خریداری‌شده:", fontSize = 10.sp, color = textMuted)
                            Text("${formatPrice(sharesCount)} برگه سهم", fontSize = 11.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("قیمت تمام‌شده هر برگه سهم (با کارمزد):", fontSize = 10.sp, color = textMuted)
                            Text("${formatPrice(costBasisPerShare.toLong())} تومان", fontSize = 11.sp, color = goldClr, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("قیمت پایه تابلو:", fontSize = 10.sp, color = textMuted)
                            Text("${formatPrice(sharePriceToman)} تومان", fontSize = 11.sp, color = textPrimary)
                        }

                        Divider(color = dividerColor.copy(alpha = 0.3f), thickness = 1.dp)

                        Text(
                            text = "پیش‌بینی نهایی آورده و عایق تورمی مجمع:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldClr
                        )

                        // 1. Direct Cash dividend
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StockGreen.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StockGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("واریزی خالص نقدی سود سهام (سجام):", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Text("${formatPrice(totalCashDividend.toLong())} تومان", fontSize = 11.sp, color = StockGreen, fontWeight = FontWeight.Black)
                        }

                        // 2. Bonus Shares
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(goldClr.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = goldClr, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("سهام جایزه رایگان مجمع ($capIncreasePercent%):", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Text("${formatPrice(bonusSharesCount)} برگه سهم", fontSize = 11.sp, color = goldClr, fontWeight = FontWeight.Black)
                        }

                        // 3. Compound growth and final results
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(goldClr.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .border(1.dp, goldClr, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("کل دارایی‌های حاصل پس از یکسال:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text("شامل اصل سرمایه + سود نقدی + رشد ارزش", fontSize = 8.sp, color = textMuted)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${formatPrice(finalCapitalValue.toLong())} تومان", fontSize = 13.sp, color = textPrimary, fontWeight = FontWeight.Black)
                                Text("(+${formatPrice(netCompoundProfit.toLong())} سود پیش‌بینی شده)", fontSize = 10.sp, color = StockGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCalcDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = goldClr)
                ) {
                    Text("بستن ماشین‌حساب ❌", color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = cardBackground
        )
    }
}

// Helpers for number formatting
private fun formatPrice(price: Long): String {
    return try {
        val format = NumberFormat.getNumberInstance(Locale.US)
        format.format(price)
    } catch (e: Exception) {
        price.toString()
    }
}

private fun formatVolume(volume: Long): String {
    return when {
        volume >= 1000000000 -> String.format(Locale.US, "%.2f میلیارد", volume.toFloat() / 1000000000)
        volume >= 1000000 -> String.format(Locale.US, "%.1f میلیون", volume.toFloat() / 1000000)
        else -> formatPrice(volume)
    }
}

// Accessibility helper function extension for SP size class in premium Compose layout
@Stable
fun Int.ssp() = this.sp

fun showBiometricPrompt(context: android.content.Context, onResult: (Boolean) -> Unit) {
    val activity = context as? FragmentActivity
    if (activity == null) {
        onResult(true)
        return
    }

    val executor = Executors.newSingleThreadExecutor()
    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                activity.runOnUiThread {
                    onResult(false)
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                activity.runOnUiThread {
                    onResult(true)
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                activity.runOnUiThread {
                    // Do not strictly lock out on transient failed attempt, allow retry
                    onResult(false)
                }
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("ورود امن بیومتریک بورس VIP")
        .setSubtitle("اثر انگشت خود را روی سنسور قرار دهید")
        .setNegativeButtonText("لغو")
        .build()

    activity.runOnUiThread {
        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onResult(true) // Fallback for emulator and devices without hardware enrollments
        }
    }
}

@Composable
fun AiAnalysisDemoDialog(
    stock: StockSignal,
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val goldClr = Color(0xFFD4AF37)
    val cardBg = if (isDarkMode) Color(0xFF0E1324) else Color(0xFFFDFCFA)
    val textPrimary = if (isDarkMode) Color(0xFFF5F3EF) else Color(0xFF1F1B18)
    val textMuted = if (isDarkMode) Color(0xFF8E99A8) else Color(0xFF6B6259)
    val borderCol = if (isDarkMode) Color(0xFF23314A) else Color(0xFFE3DEC1)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(2.dp, goldClr, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = cardBg
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = goldClr,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تحلیل VIP هوش مصنوعی (جمنای)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldClr
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = textMuted
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF171F36) else Color(0xFFF7F5EE)),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("نماد تحلیل‌شده:", fontSize = 11.sp, color = textMuted)
                                Text(stock.symbol, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = goldClr)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("صنعت:", fontSize = 11.sp, color = textMuted)
                                Text(stock.sector, fontSize = 11.sp, color = textPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("قیمت فعلی:", fontSize = 11.sp, color = textMuted)
                                Text("${formatPrice(stock.price)} ریال", fontSize = 11.sp, color = textPrimary)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "📌 ارزیابی بازده نقدی (DPS) و سود مرکب مجمع",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )
                    Text(
                        text = "نماد ${stock.symbol} در آخرین دوره مجمع خود با سود تقسیمی مصوب ${stock.dps} ریال به بازده نقدی چشمگیر ${String.format(Locale.US, "%.2f", stock.dpsYield)}٪ رسیده است. با در نظر گرفتن تاریخچه روند ثبات تقسیم سود ۵ سال گذشته (${stock.historicalDps.joinToString("، ") { "${it} ریال" }})، این شرکت دارای تعهد مالی بالا به سهامداران (کلاس ارزیابی ${stock.dpsLevel}) است.",
                        fontSize = 11.sp,
                        color = textPrimary,
                        lineHeight = 18.ssp()
                    )
                }

                item {
                    Text(
                        text = "📊 انطباق با شاخص تورم نقطه به نقطه کشور",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )
                    Text(
                        text = "سرمایه‌گذاری روی این سهم با هدف نگهداشت تا مجمع، در مقایسه مستقیم با نرخ سپرده بانکی ۳۰٪ و تورم افسارگسیخته کشور (۴۸.۳٪) مزیت مهمی ایجاد می‌کند. پتانسیل افزایش قیمت روز نماد مجمع و انباشت سود معادل ${stock.growthPotential}٪ ارزیابی می‌شود که کل بازدهی مجمع را به عدد خیره‌کننده ${String.format(Locale.US, "%.2f", stock.totalReturnPotential)}٪ می‌رساند. این بازدهی کاملاً پاسخگوی نرخ افت ارزش پول ملی خواهد بود.",
                        fontSize = 11.sp,
                        color = textPrimary,
                        lineHeight = 18.ssp()
                    )
                }

                item {
                    Text(
                        text = "💡 استراتژی تکنیکال و سناریوهای مجمع یاب VIP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDarkMode) Color(0xFF131B30) else Color(0xFFFAF7EF), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "✔️ استراتژی ورود: ورود پله‌ای با وزن ۲۰٪ سبد از هم‌اکنون تا ۳۰ روز قبل از مجمع.",
                            fontSize = 10.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📅 رویدادهای کلیدی مجمع: ${stock.keyDateMilestones}",
                            fontSize = 10.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "❌ سناریوی حد ضرر: در صورت شکست حمایت تکنیکال قیمت پایانی روزانه زیر سطح ۱۰٪ ارزش فعلی.",
                            fontSize = 10.sp,
                            color = textPrimary
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val textToCopy = """
                                    📢 **سیگنال و تحلیل بنیادی مجمع VIP (محصول مجمع‌یاب)**
                                    
                                    🔹 **نماد تفسیری:** #${stock.symbol} (${stock.name})
                                    🔹 **صنعت بورس:** ${stock.sector}
                                    🔹 **آخرین قیمت پایانی:** ${formatPrice(stock.price)} ریال
                                    🔹 **سود خالص نقدی (DPS):** ${stock.dps} ریال ([بازده نقدی: ${String.format(Locale.US, "%.1f", stock.dpsYield)}%])
                                    🔹 **ضرب‌العجل قانونی واریز نقدی:** ${stock.paymentDelayMonths} ماهه (سجا)
                                    🔹 **کلاس امنیتی و وفاداری مجمع:** کلاس ${stock.dpsLevel}
                                    🔹 **پتانسیل کل بازدهی مجمع:** ${String.format(Locale.US, "%.1f", stock.totalReturnPotential)}% (پوشش کامل تورم نقطه به نقطه کشور)
                                    
                                    📅 **رویدادها و برنامه مجمع:**
                                    ${stock.keyDateMilestones}
                                    
                                    🤖 **تحلیل تخصصی دستیار هوش مصنوعی (Gemini-AI):**
                                    خرید پله‌ای این نماد به دلایل عایق تورمی بنیادی و سود تقسیمی بالا از کلاس مالی برتر بورس ارزیابی می‌گردد.
                                    
                                    💥 کانال اختصاصی مجمع‌یاب هوش کارآفرین بورس
                                    🛑 @MajeebVIP_Bourse
                                """.trimIndent()
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                android.widget.Toast.makeText(context, "تحلیل تلگرامی VIP در حافظه کپی شد!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D9BF0)), // Telegram Blue brand color
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("کپی فرمت تلگرام 📢", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val demoAiText = "تحلیل بنیادی نماد ${stock.symbol} به عنوان عایق تورمی بورس با کلاس تعهد و سودآوری ${stock.dpsLevel} و بازده مجمع پیش‌فرض ممتاز ارزیابی می‌گردد."
                                generateStockPdfReport(context, stock, demoAiText, isDarkMode)
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // PDF Green
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📥 گزارش PDF", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = goldClr),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("بستن و خروج", fontSize = 10.sp, color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppTamperedScreen(viewModel: TradingViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val expectedSig by viewModel.expectedSha256.collectAsState()
    val context = LocalContext.current

    val primaryColor = Color(0xFFD32F2F) // Security warning red
    val goldClr = Color(0xFFD4AF37)
    val backgroundBg = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFAF9F6)
    val cardBg = if (isDarkMode) Color(0xFF161B2E) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkMode) Color(0xFFF1F1F1) else Color(0xFF1F1B18)
    val textSecondary = if (isDarkMode) Color(0xFF8E99A8) else Color(0xFF6B6259)

    var clickCount by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.5.dp, primaryColor, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive warning icon to bypass during development
                IconButton(
                    onClick = {
                        clickCount++
                        if (clickCount >= 7) {
                            // Dev bypass: Reset expected signature back to BYPASS
                            viewModel.saveSecuritySettings("BYPASS")
                            android.widget.Toast.makeText(context, "حالت توسعه بورس فعال شد. بازنشانی اثر انگشت گواهی به BYPASS", android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "دستکاری شده",
                        tint = primaryColor,
                        modifier = Modifier.size(72.dp)
                    )
                }

                Text(
                    text = "⚠️ نقص امنیتی: اصالت برنامه تایید نشد!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "سیستم خودکار شناسایی تغییر غیرمجاز اثر انگشت APK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "امضای دیجیتال (SHA-256) روی این موبایل با اثر انگشت اصلی توسعه‌دهنده تفاوت دارد. به دلایل امنیتی تمام لایسنس‌ها باطل گردیده و پایگاه داده مسدود شد.",
                    fontSize = 11.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.ssp()
                )

                Divider(color = primaryColor.copy(alpha = 0.2f), thickness = 1.dp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryColor.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔬 جزییات امنیتی تشخیص دستکاری شده:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Text(
                        text = "• وضعیت امضای کلاینت: فاقد اصالت دیجیتال",
                        fontSize = 9.sp,
                        color = textSecondary
                    )
                    Text(
                        text = "• امضای مورد انتظار هدف: \n${expectedSig}",
                        fontSize = 9.sp,
                        color = textSecondary,
                        lineHeight = 13.ssp()
                    )
                    Text(
                        text = "• اقدام پیشگیرانه خودکار: لایسنس‌ها ابطال شد! ✔️",
                        fontSize = 9.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        // Recheck
                        viewModel.saveSecuritySettings(expectedSig)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "تلاش مجدد جهت ارزیابی امضا",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "در صورتی که برنامه نویس هستید و در محیط شبیه‌ساز تست می‌کنید، ۷ بار روی علامت هشدار قرمز بالا بزنید تا قفل به حالت BYPASS بازگردد.",
                    fontSize = 9.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.ssp()
                )
            }
        }
    }
}

// Native PDF report exporter with watermarks (Fulfills requested competitive advantage)
fun generateStockPdfReport(context: android.content.Context, stock: StockSignal, aiResult: String, isDarkMode: Boolean) {
    try {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Paint resources
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(212, 175, 55) // Gold
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val headingPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(12, 14, 20) // Deep slate
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        // 1. Draw top decorative header band
        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(12, 14, 20)
        }
        canvas.drawRect(0f, 0f, 595f, 95f, headerPaint)

        // Header Title
        titlePaint.color = android.graphics.Color.WHITE
        canvas.drawText("MajeebVIP STOCK REPORT", 35f, 42f, titlePaint)
        
        textPaint.color = android.graphics.Color.rgb(220, 220, 220)
        textPaint.textSize = 9f
        canvas.drawText("Automatic AI Valuation & Inflation Compliance Platform", 35f, 65f, textPaint)
        canvas.drawText("User Mobile License: ${stock.keyDateMilestones.hashCode().coerceAtLeast(100500)} (CONFIDENTIAL VIP)", 35f, 80f, textPaint)

        // 2. Draw watermark background diagonal across page
        val watermarkPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(16, 255, 165, 0) // Translucent theme golden-orange
            textSize = 34f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.save()
        canvas.rotate(-40f)
        canvas.drawText("مجمع‌یاب VIP - ویژه اعضای کانال بورسی", -280f, 480f, watermarkPaint)
        canvas.drawText("حق کپی محفوظ است - کلاینت ضد کرک", -240f, 560f, watermarkPaint)
        canvas.restore()

        // 3. Draw Body
        var y = 140f
        val linePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(230, 225, 215)
            strokeWidth = 1.5f
        }
        
        // Stock Header
        headingPaint.color = android.graphics.Color.rgb(180, 140, 40)
        headingPaint.textSize = 15f
        canvas.drawText("تحلیل بنیادی مجمع نماد: ${stock.symbol} / ${stock.name}", 35f, y, headingPaint)
        y += 24f

        canvas.drawText("Sector (صنعت): ${stock.sector}", 35f, y, textPaint)
        y += 18f
        
        val priceText = "آخرین قیمت پایانی: ${stock.price} ریال | تغییر روزانه: ${stock.changePercent}%"
        canvas.drawText(priceText, 35f, y, textPaint)
        y += 20f

        // Separator
        canvas.drawLine(35f, y, 560f, y, linePaint)
        y += 25f

        // DPS Valuation Row
        headingPaint.color = android.graphics.Color.rgb(12, 14, 20)
        headingPaint.textSize = 12f
        canvas.drawText("💵 جزئیات تخصصی و ارزیابی عواید نقدی (DPS Valuation):", 35f, y, headingPaint)
        y += 22f

        textPaint.textSize = 11f
        textPaint.color = android.graphics.Color.BLACK
        canvas.drawText("• پیش‌بینی سود خالص تقسیمی مجمع: ${stock.dps} ریال برای هر سهم", 35f, y, textPaint)
        y += 18f
        canvas.drawText("• بازدهی نقدی عملکرد مجمع (DPS Yield): ${String.format(java.util.Locale.US, "%.1f", stock.dpsYield)}%", 35f, y, textPaint)
        y += 18f
        
        val delayLabel = when (stock.paymentDelayMonths) {
            1 -> "فوق سریع (زیر ۳۰ روز تسویه با سجا)"
            2 -> "سریع (کمتر از ۶۰ روز تسویه با سجا)"
            3 -> "متوسط (زیر ۹۰ روز تسویه)"
            else -> "عادی (مهلت قانونی تا ۴ ماه)"
        }
        canvas.drawText("• سرعت تسویه و واریز نقدی: $delayLabel", 35f, y, textPaint)
        y += 18f
        canvas.drawText("• سطح حاشیه امنیت و تعهد مجمع: کلاس ${stock.dpsLevel}", 35f, y, textPaint)
        y += 18f
        canvas.drawText("• پتانسیل نوسان قیمتی سود انباشته: ${stock.growthPotential}% | بازدهی کل مورد انتظار مجمع: ${String.format(java.util.Locale.US, "%.1f", stock.totalReturnPotential)}%", 35f, y, textPaint)
        y += 25f

        // Separator
        canvas.drawLine(35f, y, 560f, y, linePaint)
        y += 25f

        // Key Milestones Row
        canvas.drawText("📅 رویدادها، زمان‌بندی و الزامات شرکت در مجمع:", 35f, y, headingPaint)
        y += 20f
        canvas.drawText(stock.keyDateMilestones, 35f, y, textPaint)
        y += 25f

        // Separator
        canvas.drawLine(35f, y, 560f, y, linePaint)
        y += 25f

        // AI Analysis Report Row
        headingPaint.color = android.graphics.Color.rgb(180, 140, 40)
        canvas.drawText("🤖 کارت تصمیم یار هوش مصنوعی مجمع‌یاب (Quant Decision Card):", 35f, y, headingPaint)
        y += 20f

        // Draw AI description using standard StaticLayout supporting RTL/Persian shaping and newlines natively
        val aiTextPaint = android.text.TextPaint().apply {
            color = android.graphics.Color.rgb(40, 45, 55)
            textSize = 9.5f
            isAntiAlias = true
        }
        val staticLayout = android.text.StaticLayout(
            aiResult,
            aiTextPaint,
            515,
            android.text.Layout.Alignment.ALIGN_NORMAL,
            1.2f,
            0.0f,
            false
        )
        canvas.save()
        canvas.translate(40f, y)
        staticLayout.draw(canvas)
        canvas.restore()

        // 4. Draw Footer Band
        canvas.drawRect(0f, 800f, 595f, 842f, headerPaint)
        textPaint.color = android.graphics.Color.rgb(200, 200, 200)
        textPaint.textSize = 8f
        canvas.drawText("گزارش تولید شده به وسیله موتور هوشمند مجمع‌یاب بورس تهران. تمام حقوق لایسنس کاربری محفوظ است.", 35f, 824f, textPaint)

        pdfDocument.finishPage(page)

        // Write compilation to target directory
        val file = java.io.File(
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS),
            "Report_${stock.symbol}_VIP.pdf"
        )
        val outputStream = java.io.FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()

        android.widget.Toast.makeText(context, "گزارش PDF با موفقیت صادر شد: پوشه دانلودها", android.widget.Toast.LENGTH_LONG).show()

        // Distribute via implicit System share provider fallback securely
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "ارسال سند PDF تحلیل مجمع به تلگرام:"))

    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "ایراد در صادر کردن پی‌دی‌اف: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}
