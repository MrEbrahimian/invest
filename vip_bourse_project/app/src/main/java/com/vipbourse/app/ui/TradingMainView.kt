package com.vipbourse.app.ui

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
import com.vipbourse.app.data.StockSignal
import com.vipbourse.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TradingMainView(viewModel: TradingViewModel) {
    val isActivated by viewModel.isActivated.collectAsState()
    val userMobile by viewModel.userMobile.collectAsState()
    val expiryDate by viewModel.expiryDate.collectAsState()

    // Enforce Right-to-Left for Persian language interface
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
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
            Spacer(modifier = Modifier.height(48.dp))

            // Premium VIP Diamond Crown Icon Visual
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(listOf(GoldPrimary, GoldSecondary)),
                        CircleShape
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "قفل برنامه VIP",
                        tint = GoldPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "سامانه معاملاتی ویژه بورس ایران",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "نسخه VIP بدون نیاز به سرور (محاسبات کامل کلاینت‌ساید)",
                fontSize = 12.sp,
                color = SlateTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Premium License Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔐 لایسنس فعالسازی آفلاین",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "به دلیل امنیت پیشرفته کلاینت‌ساید و لایسنس رمزنگاری شده AES-256، برای دسترسی به سیگنال‌های بورس ایران باید کد لایسنس دریافتی از مدیر سامانه تفکیک بازار را وارد کنید.",
                        fontSize = 13.sp,
                        color = SlateTextSecondary,
                        lineHeight = 22.ssp()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs UI
            Text(
                text = "کد فعالسازی آفلاین را وارد کنید",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PureWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )

            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("کد رمزنگاری لایسنس...", color = SlateTextSecondary) },
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (activationError != null) {
                Text(
                    text = "⚠️ " + activationError,
                    color = StockRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
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
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("تایید کدهای سخت‌افزاری و ورود", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Sandbox Testing Generator Help Card (Very useful!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛠️ بخش تست و شبیه‌ساز آفلاین لایسنس",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "برای تسریع آزمون، می‌توانید لایسنس اختصاصی برای شماره موبایل خود را در بخش زیر شبیه‌سازی کرده و مستقیماً کپی و استفاده کنید:",
                        fontSize = 12.sp,
                        color = SlateTextSecondary,
                        lineHeight = 20.ssp()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = testMobileInput,
                        onValueChange = { testMobileInput = it },
                        label = { Text("شماره موبایل کلاینت", color = SlateTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            generatedTrialKey = viewModel.generateTestKey(testMobileInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("ساخت لایسنس رمزنگاری AES", color = GoldPrimary)
                    }

                    if (generatedTrialKey.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(8.dp))
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
                                    Text("کد لایسنس اختصاصی رمز شده:", fontSize = 11.sp, color = GoldPrimary)
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "کپی", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = generatedTrialKey,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = PureWhite,
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
 * Anti-leak screen (uses secure drawing behind and rotated mobile number watermarks).
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
    val inflationRate by viewModel.inflationRate.collectAsState()
    val inflationRatePointToPoint by viewModel.inflationRatePointToPoint.collectAsState()
    val bankIncomeRate by viewModel.bankIncomeRate.collectAsState()
    val smartAlertTarget by viewModel.smartAlertTarget.collectAsState()
    val notificationLog by viewModel.notificationLog.collectAsState()

    var editedApiKey by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
    var editedCustomUrl by remember(geminiCustomUrl) { mutableStateOf(geminiCustomUrl) }
    var editedInflationRate by remember(inflationRate) { mutableStateOf(inflationRate.toString()) }
    var editedInflationRatePointToPoint by remember(inflationRatePointToPoint) { mutableStateOf(inflationRatePointToPoint.toString()) }
    var editedBankIncomeRate by remember(bankIncomeRate) { mutableStateOf(bankIncomeRate.toString()) }
    var editedSmartAlertTarget by remember(smartAlertTarget) { mutableStateOf(smartAlertTarget.toString()) }
    var isSettingsExpanded by remember { mutableStateOf(false) }

    var showVipOnly by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(GoldPrimary, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = GoldSecondary,
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
                                    color = Color.White
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
                                // Glowing small active indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(NeonGreen, CircleShape)
                                        .background(NeonGreen.copy(alpha = 0.4f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (useLiveCrawler) "LIVE SCRAPING ACTIVE" else "SIMULATOR SENSORS ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextSecondary,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchMarketData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "به‌روزرسانی داده‌ها",
                            tint = PureWhite
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground),
                modifier = Modifier.border(0.dp, DarkSurfaceVariant)
            )
        }
    ) { innerPadding ->
        // Background structure with mobile watermark rotated behind elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    // Draw a persistent security digital watermark containing user's phone to identify leaks
                    val paintText = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        alpha = 14 // Extremely faint opacity
                        textSize = 48f
                        isAntiAlias = true
                    }
                    rotate(degrees = -30f) {
                        for (x in -500..size.width.toInt() + 500 step 400) {
                            for (y in -500..size.height.toInt() + 500 step 300) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    "VIP MEMBER $userMobile",
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
                // Security Warning / Config Top Bar
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Anti screenshot banner active
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = StockGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حفاظت ضد نشت فعال است (امکان اسکرین‌شات گرفتن وجود ندارد)",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Client settings controls: Live TSETMC connection vs fast simulator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "تغییر منبع داده‌های بورس",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = if (useLiveCrawler) "اتصال مستقیم کراولر به old.tsetmc.com" else "شبیه‌ساز فوق پیشرفته دیده بان بازار تهرانی",
                                        fontSize = 11.sp,
                                        color = SlateTextSecondary
                                    )
                                }
                                Switch(
                                    checked = useLiveCrawler,
                                    onCheckedChange = { viewModel.toggleLiveCrawler(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GoldPrimary,
                                        checkedTrackColor = GoldPrimary.copy(alpha = 0.4f)
                                    )
                                )
                            }
                            
                    scrapingErrorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = StockRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ADVANCED DYNAMIC CONFIGURATION & CBI INDEX TRACKER CARD
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, if (isSettingsExpanded) GoldPrimary else BorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { isSettingsExpanded = !isSettingsExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنظیمات هوش مصنوعی (Gemini) و نرخ‌های کلان اقتصادی",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                        Icon(
                            imageVector = if (isSettingsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = SlateTextSecondary
                        )
                    }

                    if (isSettingsExpanded) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(14.dp))

                        // API KEY INPUT
                        Text(
                            text = "کلید اختصاصی جمنای (Google AI Studio Gemini Key):",
                            fontSize = 10.sp,
                            color = SlateTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editedApiKey,
                            onValueChange = { editedApiKey = it },
                            placeholder = { Text("AIzaSy...", fontSize = 11.sp, color = SlateTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BorderColor,
                                focusedBorderColor = GoldPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // ALT ENDPOINT BASE URL
                        Text(
                            text = "آدرس پروکسی / گیت‌وی اختیاری (Endpoint Base URL):",
                            fontSize = 10.sp,
                            color = SlateTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editedCustomUrl,
                            onValueChange = { editedCustomUrl = it },
                            placeholder = { Text("https://generativelanguage.googleapis.com/", fontSize = 11.sp, color = SlateTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BorderColor,
                                focusedBorderColor = GoldPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تورم سالانه مرکز آمار (٪):",
                                    fontSize = 9.sp,
                                    color = SlateTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editedInflationRate,
                                    onValueChange = { editedInflationRate = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = BorderColor,
                                        focusedBorderColor = GoldPrimary
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تورم نقطه به نقطه کشور (٪):",
                                    fontSize = 9.sp,
                                    color = SlateTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editedInflationRatePointToPoint,
                                    onValueChange = { editedInflationRatePointToPoint = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = BorderColor,
                                        focusedBorderColor = GoldPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "سود معیار صندوق درآمد ثابت (٪):",
                                    fontSize = 9.sp,
                                    color = SlateTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editedBankIncomeRate,
                                    onValueChange = { editedBankIncomeRate = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = BorderColor,
                                        focusedBorderColor = GoldPrimary
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "حداقل بازده هدف خرید طلایی (٪):",
                                    fontSize = 9.sp,
                                    color = SlateTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editedSmartAlertTarget,
                                    onValueChange = { editedSmartAlertTarget = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = BorderColor,
                                        focusedBorderColor = GoldPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val inflationVal = editedInflationRate.toFloatOrNull() ?: 38.8f
                                val inflationP2PVal = editedInflationRatePointToPoint.toFloatOrNull() ?: 31.0f
                                val bankVal = editedBankIncomeRate.toFloatOrNull() ?: 30.0f
                                val targetVal = editedSmartAlertTarget.toFloatOrNull() ?: 35.0f

                                viewModel.saveGeminiSettings(editedApiKey, editedCustomUrl)
                                viewModel.saveMacroSettings(inflationVal, inflationP2PVal, bankVal, targetVal)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ذخیره تنظیمات و بازنویسی محاسبات VIP", fontSize = 11.sp, color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // PUSH NOTIFICATION SIMULATOR & COMPREHENSIVE GOLD FEEDS CARD
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, if (notificationLog.isNotEmpty()) StockGreen.copy(alpha = 0.5f) else BorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (notificationLog.isNotEmpty()) StockGreen else SlateTextSecondary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سیستم هشدار نوتیفیکیشن مجمع عالی (سود > $smartAlertTarget٪)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                        Text(
                            text = "نرخ تورم مرجع ($inflationRate٪)",
                            fontSize = 9.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "این موتور به صورت پس‌زمینه سهام آماده مجمع با پتانسیل کل سود تقسیمی سالیانه به علاوه درصد رشد بالای $smartAlertTarget٪ (بیشتر از نرخ سود درآمد ثابت و تورم جاری) را شناسایی کرده و نوتیفیکیشن اندروید ارسال می‌کند.",
                        fontSize = 10.sp,
                        color = SlateTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action button to scan & simulate notifications
                    Button(
                        onClick = {
                            val threshold = smartAlertTarget
                            val highYieldStocks = allStocks.filter { it.totalReturnPotential >= threshold }
                            if (highYieldStocks.isNotEmpty()) {
                                highYieldStocks.take(2).forEach { item ->
                                    viewModel.triggerLocalNotification(item.symbol, item.totalReturnPotential)
                                }
                            } else {
                                viewModel.triggerLocalNotification("تحلیل طلایی ممتاز مجمع", 39.5f)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (notificationLog.isNotEmpty()) StockGreen.copy(alpha = 0.15f) else DarkSurfaceVariant),
                        border = BorderStroke(1.dp, if (notificationLog.isNotEmpty()) StockGreen else BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (notificationLog.isNotEmpty()) StockGreen else PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تست ارسال نوتیفیکیشن و پایش آنلاین سهام طلایی",
                            fontSize = 11.sp,
                            color = if (notificationLog.isNotEmpty()) StockGreen else PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (notificationLog.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📯 هشدارهای صادر شده اخیر خرید مجمع عالی:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StockGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            notificationLog.take(4).forEach { log ->
                                Text(
                                    text = log,
                                    fontSize = 9.sp,
                                    color = PureWhite,
                                    lineHeight = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                        // Dynamic accessibility & key metrics test tag
                                        .testTag("notification_bubble")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Membership Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "لایسنس موبایل: $userMobile",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "نامحدود",
                                        fontSize = 11.sp,
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "اعتبار اشتراک لایسنس سخت‌افزاری تا تاریخ: $expiryDate",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Filter formula badge
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📉 فرمول فیلتر هوشمند VIP مستعد:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "(حجم روز > ۱میلیون) ✖ (ارزش معاملات > ۱۰میلیارد ریال) ✖ (EPS مثبت) ✖ (پتانسيل ۲ <= P/E <= ۱۰)",
                                fontSize = 11.sp,
                                color = PureWhite,
                                lineHeight = 18.ssp()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bourse statistical panels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "کل سهام بررسی شده",
                            value = allStocks.size.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "سیگنال‌های مستعد VIP",
                            value = vipSignals.size.toString(),
                            valueColor = StockGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation / Filter Tab Selectors
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        TabButton(
                            title = "فقط سیگنال‌های VIP (${vipSignals.size})",
                            isSelected = showVipOnly,
                            modifier = Modifier.weight(1f),
                            onClick = { showVipOnly = true }
                        )
                        TabButton(
                            title = "کل بازار (${allStocks.size})",
                            isSelected = !showVipOnly,
                            modifier = Modifier.weight(1f),
                            onClick = { showVipOnly = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Screening Severity Filter Selector Component
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "شدت و معیار فیلتر چند سطحی:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                            
                            val infoLabel = when (screeningSeverity) {
                                "LOW" -> "آسان‌گیر: نقدینگی پایه + مجمع سودده"
                                "MEDIUM" -> "متوازن: نقدینگی متوسط + عایق سود بانکی"
                                else -> "سخت‌گیرانه: عایق تورم ملى + نقدینگی بالا + پایداری سود"
                            }
                            Text(
                                text = infoLabel,
                                fontSize = 9.sp,
                                color = if (screeningSeverity == "HIGH") StockGreen else if (screeningSeverity == "MEDIUM") GoldPrimary else StockRed,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // LOW FILTER BUTTON
                            Button(
                                onClick = { viewModel.setScreeningSeverity("LOW") },
                                modifier = Modifier.weight(1f).height(34.dp).testTag("filter_low_button"),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (screeningSeverity == "LOW") StockRed.copy(alpha = 0.15f) else DarkSurfaceVariant,
                                    contentColor = if (screeningSeverity == "LOW") StockRed else SlateTextSecondary
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (screeningSeverity == "LOW") StockRed.copy(alpha = 0.8f) else BorderColor
                                )
                            ) {
                                Text("پایین (ریسک بالا)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // MEDIUM FILTER BUTTON
                            Button(
                                onClick = { viewModel.setScreeningSeverity("MEDIUM") },
                                modifier = Modifier.weight(1.1f).height(34.dp).testTag("filter_medium_button"),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (screeningSeverity == "MEDIUM") GoldPrimary.copy(alpha = 0.15f) else DarkSurfaceVariant,
                                    contentColor = if (screeningSeverity == "MEDIUM") GoldPrimary else SlateTextSecondary
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (screeningSeverity == "MEDIUM") GoldPrimary.copy(alpha = 0.8f) else BorderColor
                                )
                            ) {
                                Text("متوسط (متوازن)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // HIGH FILTER BUTTON
                            Button(
                                onClick = { viewModel.setScreeningSeverity("HIGH") },
                                modifier = Modifier.weight(1.2f).height(34.dp).testTag("filter_high_button"),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (screeningSeverity == "HIGH") StockGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
                                    contentColor = if (screeningSeverity == "HIGH") StockGreen else SlateTextSecondary
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (screeningSeverity == "HIGH") StockGreen.copy(alpha = 0.8f) else BorderColor
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (screeningSeverity == "HIGH") StockGreen else SlateTextSecondary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("قوی (VIP عالی)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (isLoading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = GoldPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "در حال اتصال به TSETMC و کراول کردن مستقیم داده‌ها...", fontSize = 12.sp, color = SlateTextSecondary)
                        }
                    }
                } else {
                    val activeList = if (showVipOnly) vipSignals else allStocks
                    if (activeList.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = SlateTextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (showVipOnly) "در حال حاضر هیچ نمادی در فیلتر سختگیرانه VIP قرار نگرفته است." else "داده‌ای در دیده بان وجود ندارد.",
                                    fontSize = 13.sp,
                                    color = SlateTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(activeList) { stock ->
                            SignalCardView(stock = stock, userMobile = userMobile, viewModel = viewModel)
                            Spacer(modifier = Modifier.height(12.dp))
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

@Composable
fun StatCard(
    title: String,
    value: String,
    valueColor: Color = PureWhite,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = SlateTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
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
    
    val currentAiResult = aiResultMap[stock.symbol]
    val currentAiLoading = aiLoadingMap[stock.symbol] ?: false

    var expanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

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
                    color = android.graphics.Color.WHITE
                    alpha = 8
                    textSize = 32f
                    isAntiAlias = true
                }
                rotate(degrees = -15f) {
                    drawContext.canvas.nativeCanvas.drawText("USER: $userMobile", 30f, 60f, paintText)
                    drawContext.canvas.nativeCanvas.drawText("VIP BOURSE", size.width - 200f, size.height - 30f, paintText)
                }
            },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(
            width = if (stock.isVipSignal) 1.5.dp else 1.dp,
            color = if (stock.isVipSignal) GoldPrimary.copy(alpha = 0.5f) else BorderColor
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
                                if (stock.isVipSignal) GoldPrimary else DarkSurfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stock.symbol,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stock.isVipSignal) DarkBackground else PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stock.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                }

                // Growth badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (if (isPositive) "+" else "") + String.format(Locale.US, "%.2f", stock.changePercent) + "%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = DarkSurfaceVariant.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Fundamental Stock parameters grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("قیمت پایانی", fontSize = 10.sp, color = SlateTextSecondary)
                    Text("$formattedPrice ریال", fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                    Text("$formattedToman تومان", fontSize = 11.sp, color = GoldPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("حجم معاملات", fontSize = 10.sp, color = SlateTextSecondary)
                    Text(formattedVolume, fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                    Text("P/E: ${String.format(Locale.US, "%.1f", stock.pe)}", fontSize = 11.sp, color = SlateTextSecondary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("ارزش معاملات", fontSize = 10.sp, color = SlateTextSecondary)
                    Text("$formattedValueToman ت", fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                    Text("EPS: ${stock.eps}", fontSize = 11.sp, color = SlateTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Crucial requested DPS dividend calculations badge!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoldPrimary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "پیش‌بینی سود نقدی سهم (DPS):",
                        fontSize = 11.sp,
                        color = SlateTextSecondary
                    )
                }
                Text(
                    text = "${formatPrice(stock.dps.toLong())} ریال (${String.format(Locale.US, "%.1f", stock.dpsYield)}% بازدهی)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StockGreen
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
                    Divider(color = DarkSurfaceVariant, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Realistic Dual Level tags & annual target potential representation requested by user
                    Text(
                        text = "⚖️ ارزیابی سطح سود مجمع (واقع‌بینانه):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Displaying the active rating level
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GoldSecondary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = stock.dpsLevel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "حداقل سود تضمینی بانکی مرجع: ۲۲.۰٪ | پتانسیل رشد سرمایه‌گذاری سالیانه: ${stock.growthPotential}٪ | پتانسیل کل مجمع (سود + رشد): ${String.format(Locale.US, "%.1f", stock.totalReturnPotential)}٪",
                                fontSize = 10.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📅 تاریخ‌های کلیدی و برنامه زمانی مجمع:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = stock.keyDateMilestones,
                        fontSize = 11.sp,
                        color = PureWhite,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📈 تاریخچه ۵ سال اخیر سود تقسیمی (کدال):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
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
                                    .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (idx == 4) "سال جاری" else "${5 - idx} سال قبل",
                                    fontSize = 8.sp,
                                    color = SlateTextSecondary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${formatPrice(value.toLong())}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (idx == 4) StockGreen else PureWhite
                                )
                                Text(
                                    text = "ریال",
                                    fontSize = 8.sp,
                                    color = SlateTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "⚡ تحلیل انطباق فیلتر سخت‌افزاری VIP:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = stock.reason,
                        fontSize = 11.sp,
                        color = PureWhite,
                        lineHeight = 16.ssp(),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "صنعت: ${stock.sector}",
                        fontSize = 10.sp,
                        color = SlateTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = BorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        // Expandable forms for custom modifications (persisted in offline Room Database)
                        Text(
                            text = "🛠️ تصحیح دستی سودهای ۵ سال گذشته (آفلاین / کدال):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = yr1,
                                onValueChange = { yr1 = it },
                                label = { Text("۵ سال قبل", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    focusedBorderColor = GoldPrimary
                                )
                            )
                            OutlinedTextField(
                                value = yr2,
                                onValueChange = { yr2 = it },
                                label = { Text("۴ سال قبل", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    focusedBorderColor = GoldPrimary
                                )
                            )
                            OutlinedTextField(
                                value = yr3,
                                onValueChange = { yr3 = it },
                                label = { Text("۳ سال قبل", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    focusedBorderColor = GoldPrimary
                                )
                            )
                            OutlinedTextField(
                                value = yr4,
                                onValueChange = { yr4 = it },
                                label = { Text("۲ سال قبل", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    focusedBorderColor = GoldPrimary
                                )
                            )
                            OutlinedTextField(
                                value = yr5,
                                onValueChange = { yr5 = it },
                                label = { Text("سال جاری (DPS)", fontSize = 8.sp) },
                                modifier = Modifier.weight(1.2f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    focusedBorderColor = GoldPrimary
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
                                label = { Text("پتانسیل رشد ٪", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    focusedBorderColor = GoldPrimary
                                )
                            )
                            OutlinedTextField(
                                value = keyMonthsInput,
                                onValueChange = { keyMonthsInput = it },
                                label = { Text("مناسبت مجمع سالانه", fontSize = 10.sp) },
                                modifier = Modifier.weight(2f),
                                maxLines = 1,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    focusedBorderColor = GoldPrimary
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
                                colors = ButtonDefaults.buttonColors(containerColor = StockGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("ذخیره در استوریج Room", fontSize = 11.sp, color = DarkBackground, fontWeight = FontWeight.Bold)
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
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary.copy(alpha = 0.9f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = DarkBackground,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اصلاح و شخصی‌سازی دستی سود نقدی سال‌های گذشته مجمع",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBackground
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
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (currentAiResult != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            color = DarkBackground,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = currentAiResult,
                                    fontSize = 11.sp,
                                    color = PureWhite,
                                    lineHeight = 18.ssp()
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.analyzeStockWithAI(stock.symbol, stock) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, GoldPrimary),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("به‌روزرسانی تحلیل هوشمند مجمع", fontSize = 10.sp, color = GoldPrimary)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.analyzeStockWithAI(stock.symbol, stock) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (currentAiLoading) {
                                CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = DarkBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تحلیل و پایش آنی سهم با هوش مصنوعی (جمنای)", fontSize = 11.sp, color = DarkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
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
