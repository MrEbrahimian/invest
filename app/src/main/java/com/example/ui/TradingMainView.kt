package com.example.ui

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.graphicsLayer
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
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2500) // Premium 2.5s golden entry delay
        showSplash = false
    }

    // Enforce Right-to-Left for Persian language interface
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = showSplash,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) with fadeOut(animationSpec = tween(600))
                },
                label = "SplashScreenTransition"
            ) { splashActive ->
                if (splashActive) {
                    SplashScreen(isDarkMode = isDarkMode)
                } else {
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
    }
}

@Composable
fun SplashScreen(isDarkMode: Boolean) {
    val bgCol = if (isDarkMode) Color(0xFF0A0D14) else Color(0xFFFAF8F5)
    val goldClr = if (isDarkMode) GoldPrimary else Color(0xFFB39256)
    val goldSec = if (isDarkMode) GoldSecondary else Color(0xFF9E7E45)
    val textPrimary = if (isDarkMode) Color(0xFFF5F3EF) else Color(0xFF1F1B18)
    val textMuted = if (isDarkMode) SlateTextSecondary else Color(0xFF6B6259)
    val borderCol = if (isDarkMode) BorderColor else Color(0xFFDFD9D0)

    // Pulse animation for the Crown icon
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleFactor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgCol),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient gold background glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(goldClr.copy(alpha = 0.08f), Color.Transparent),
                        radius = 450f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High prestige Diamond Crown visual icon overlay
            Box(
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .size(130.dp)
                    .background(
                        Brush.linearGradient(listOf(goldClr, goldSec)),
                        CircleShape
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDarkMode) Color(0xFF0A0D14) else Color(0xFFFAF8F5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "VIP Icon",
                        tint = goldClr,
                        modifier = Modifier.size(86.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Application Title
            Text(
                text = "وی‌آی‌پی بورس (VIP Bourse)",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Application Subtitle
            Text(
                text = "پایگاه هوشمند تحلیل مجمع و سیگنال‌های نخبگان بورس",
                fontSize = 11.sp,
                color = textMuted,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(52.dp))

            // Minimalist luxury progress line
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(3.dp)
                    .background(borderCol.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            ) {
                // Animated loading strip
                val progressTransition = rememberInfiniteTransition(label = "Progress")
                val animatedProgress by progressTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ProgressAnim"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(Brush.linearGradient(listOf(goldClr, goldSec)), RoundedCornerShape(10.dp))
                )
            }
        }

        // Footnote
        Text(
            text = "هوشمند، برتر، بی‌انتها • نسخه مجمع‌یاب VIP 3.5",
            fontSize = 9.sp,
            color = textMuted.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        )
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

    // Smooth luxury color theme
    val cardBackground = if (isDarkMode) Color(0xFF111622) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkMode) Color(0xFF1E283A) else Color(0xFFE2DCD5)
    val selectionBg = if (isDarkMode) Color(0xFF182030) else Color(0xFFF6F3ED)
    val textPrimary = if (isDarkMode) Color(0xFFF8F6F0) else Color(0xFF1E1916)
    val textMuted = if (isDarkMode) Color(0xFF868F9C) else Color(0xFF6F655D)
    val goldClr = if (isDarkMode) GoldPrimary else Color(0xFFB59358)
    val goldSec = if (isDarkMode) GoldSecondary else Color(0xFF9E7E45)
    val greenSpark = Color(0xFF10B981)

    // Developer / Simulator key generation
    var testMobileInput by remember { mutableStateOf("09123456789") }
    var generatedTrialKey by remember { mutableStateOf("") }
    var isSandboxExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xFF090B11) else Color(0xFFFCFAF7))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Spacer(modifier = Modifier.height(48.dp))

            // Premium Visual Badge: Creative Glowing Bull & Analytical Upward Candle Graph
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.linearGradient(listOf(goldClr, goldSec, greenSpark)),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDarkMode) Color(0xFF0D111A) else Color(0xFFFAF7F4), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "الگوریتم‌های پیشرفته بورس",
                        tint = goldClr,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "پلتفرم هوشمند مجمع‌یاب VIP",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = goldClr,
                textAlign = TextAlign.Center
            )

            Text(
                text = "برنامه هوشمند پیش‌بینی سود نقدی سالانه و بررسی آسان سهام بورس تهران",
                fontSize = 12.sp,
                color = textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Beautiful Analytical Chart Simulation Graphic Card (Eliminates the VPN feel)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(greenSpark, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔐 محاسبات امن درون برنامه‌ای (روی گوشی)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "محاسبه سودها، کارمزد بورس و بررسی درآمد سهام شما، کاملاً بدون نیاز به اینترنت (آفلاین) روی گوشی شما انجام می‌شود. برای بروزرسانی قیمت‌ها و سودهای جدید، کلید دسترسی (توکن) خود را در کادر زیر وارد کنید.",
                        fontSize = 11.5.sp,
                        color = textMuted,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs UI with gorgeous typography
            Text(
                text = "کلید اتصال حساب کاربری",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 4.dp),
                textAlign = TextAlign.Start
            )

            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("مثال: کد رمزنگاری شده ورود...", color = textMuted, fontSize = 12.sp) },
                singleLine = false,
                maxLines = 3,
                textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = goldClr,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    focusedContainerColor = cardBackground,
                    unfocusedContainerColor = cardBackground
                ),
                shape = RoundedCornerShape(14.dp)
            )

            if (activationError != null) {
                Text(
                    text = "⚠️ " + activationError,
                    color = StockRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 4.dp),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Prime Action Button
            Button(
                onClick = { viewModel.activateApp(codeInput) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = goldClr,
                    contentColor = if (isDarkMode) Color(0xFF030508) else Color(0xFFFFFFFF)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp).size(20.dp)
                )
                Text("ورود امن و بارگذاری مجمع‌یاب", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SUPER COOL REVIEW FEATURE: Instant Demo Trial Login (Simplifies evaluation, removes hassle)
            Button(
                onClick = {
                    val automaticDemoKey = viewModel.generateTestKey("09123456789")
                    codeInput = automaticDemoKey
                    viewModel.activateApp(automaticDemoKey)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = greenSpark.copy(alpha = 0.12f),
                    contentColor = greenSpark
                ),
                border = BorderStroke(1.dp, greenSpark.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp).size(18.dp)
                )
                Text("تکمیل کلید آزمایشی و ورود آسان فوری 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Advanced Sandbox settings: Styled as a discrete collapsible system tools widget
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBackground.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1E2635) else Color(0xFFE9E5DE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSandboxExpanded = !isSandboxExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = goldClr,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ابزار توسعه‌دهنده و شبیه‌ساز لایسنس AES",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }
                        Icon(
                            imageVector = if (isSandboxExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (isSandboxExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "جهت بررسی و شبیه‌سازی با شماره تلفن‌های گوناگون، شماره موبایل دلخواه را وارد کرده تا کلید دسترسی متناظر ساخته شود:",
                            fontSize = 11.sp,
                            color = textMuted,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = testMobileInput,
                            onValueChange = { testMobileInput = it },
                            label = { Text("شماره همراه شما", color = textMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = goldClr,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedContainerColor = selectionBg,
                                unfocusedContainerColor = selectionBg
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                generatedTrialKey = viewModel.generateTestKey(testMobileInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = selectionBg),
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("دریافت لایسنس سفارشی", color = goldClr, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        if (generatedTrialKey.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDarkMode) Color(0xFF090C12) else Color(0xFFFAF7F3), RoundedCornerShape(8.dp))
                                    .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(generatedTrialKey))
                                    }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("کلید تولید شده (جهت کپی):", fontSize = 10.sp, color = goldClr, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = generatedTrialKey,
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = textPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "کپی",
                                        tint = textMuted,
                                        modifier = Modifier.size(16.dp).padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
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
    val isDarkMode by viewModel.isDarkMode.collectAsState()
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
    var showStepByStepGuide by remember { mutableStateOf(false) }
    var showVisualTutorialSheet by remember { mutableStateOf(false) }

    if (showQuantAuditDialog) {
        QuantAuditDebugDialog(viewModel = viewModel) {
            showQuantAuditDialog = false
        }
    }

    if (showStepByStepGuide) {
        StepByStepGuideDialog(isDarkMode = isDarkMode) {
            showStepByStepGuide = false
        }
    }

    if (showVisualTutorialSheet) {
        VisualTutorialDialog(isDarkMode = isDarkMode) {
            showVisualTutorialSheet = false
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
                        IconButton(onClick = { showStepByStepGuide = true }) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "راهنمای قدم به قدم (Tips)",
                                tint = goldAccent
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
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = if (currentTab == 3) goldAccent else mutedCol) }
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
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "کاربری ویژه VIP بورس من",
                                                fontSize = 13.sp,
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
                                                text = "اشتراک فعال بومی",
                                                fontSize = 9.sp,
                                                color = goldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "تلفن همراه فعالساز: $userMobile",
                                        fontSize = 11.sp,
                                        color = textCol
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "اعتبار اشتراک تا تاریخ مجمع سال بعد: $expiryDate",
                                        fontSize = 10.sp,
                                        color = mutedCol
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

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
                                    SignalCardView(stock = stock, userMobile = userMobile, viewModel = viewModel, onShowGuide = { showStepByStepGuide = true })
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }

                    // TAB 2: INVEST & SCREENING PANEL
                    if (currentTab == 2) {
                        item {
                            val selectedFormula by viewModel.selectedFormula.collectAsState()

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
                                val (recDps, recGrowth) = when (selectedFormula) {
                                    "DIVIDEND" -> Pair(16.0f, 4.0f)
                                    "HEDGE" -> Pair(1.0f, 22.0f)
                                    "DEFENSIVE" -> Pair(14.0f, 8.0f)
                                    "BEAT_INFLATION" -> Pair(10.0f, 28.0f)
                                    else -> Pair(12.0f, 15.0f)
                                }
                                val isCustomized = (tempDps != recDps || tempGrowth != recGrowth)
                                val presetName = when (selectedFormula) {
                                    "ALL" -> "جامع مرجع"
                                    "DIVIDEND" -> "سود نقدی طلایی"
                                    "HEDGE" -> "طلا و عایق تورمی"
                                    "DEFENSIVE" -> "حاشیه امنیت"
                                    "BEAT_INFLATION" -> "برنده تورم"
                                    else -> "نامشخص"
                                }

                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = goldAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "تنظیم دلخواه سود سالانه و رشد سهام (شخصی‌سازی)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textCol
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = if (isCustomized) "✏️ شخصی‌سازی شده مستقل روی فرضیات ($presetName)" else "🔒 همگام با فرمول فعال: $presetName",
                                                    fontSize = 9.sp,
                                                    color = if (isCustomized) goldSecondaryAccent else StockGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (hiddenSignals.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .background(goldAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${hiddenSignals.size} سهم فیلتر شد",
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
                                            text = "حداقل سود نقدی مجمع مورد انتظار شما (DPS):",
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
                                            text = "حداقل رشد پیش‌بینی شده برای قیمت خود سهم در سال:",
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
                                            text = "حداقل سود انتخابی شما برای فرستادن هشدارهای فوری:",
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

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = borderCol.copy(alpha = 0.5f), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // 🌟 ماشین‌حساب آینده‌پژوهی و تصویرسازی قدرت اثر مرکب
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(goldSecondaryAccent.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                            .border(1.dp, goldSecondaryAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = goldSecondaryAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "شبیه‌ساز اثر مرکب بلندمدت هم‌سویی سود و رشد",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = goldSecondaryAccent
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "سود نقدی بالا عالی است، اما سرمایه‌گذاری دوباره آن در بازوی افزایش ارزش و رشد بنیادی شرکت‌هاست که دارایی شما را در بلندمدت بیمه می‌کند. با تنظیمات فعلی شما (سود نقدی ${String.format(Locale.US, "%.1f", tempDps)}٪ و نرخ رشد ${String.format(Locale.US, "%.1f", tempGrowth)}٪):",
                                            fontSize = 9.5.sp,
                                            color = textCol,
                                            lineHeight = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        val totalAnnualRate = (tempDps + tempGrowth) / 100f
                                        val multiplier = Math.pow((1f + totalAnnualRate).toDouble(), 5.0).toFloat()
                                        val initialMoney = 100 // Million Tomans
                                        val finalMoneyValue = initialMoney * multiplier
                                        val bankMultiplier = Math.pow(1.30, 5.0).toFloat() // 30% fixed income bank yield
                                        val finalBankValue = initialMoney * bankMultiplier
                                        val diffPower = finalMoneyValue - finalBankValue

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "رشد فرضی ۱۰۰ میلیون تومان شما پس از ۵ سال با این فیلتر:",
                                                fontSize = 9.5.sp,
                                                color = mutedCol
                                            )
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", finalMoneyValue)} میلیون تومان",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = StockGreen
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "در مقایسه با سپرده ثابت بانکی (۳۰٪ سالانه):",
                                                fontSize = 9.5.sp,
                                                color = mutedCol
                                            )
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", finalBankValue)} میلیون تومان",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = mutedCol
                                            )
                                        }

                                        if (diffPower > 0) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "🚀 برتری مجمع‌شناختی: انتخاب هوشمندانه شما ثروتتان را ${String.format(Locale.US, "%.1f", diffPower)} میلیون تومان جلوتر از بهره بانکی قرار می‌دهد و اثرات تخریبی تورم را کاملاً مهار می‌کند!",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent,
                                                lineHeight = 14.sp
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "⚠️ راهنمای فرار عالی از تورم بورس: برای کسب نتایجی بزرگتر از سود سپرده‌های متعارف بانکی و جبران کامل تورم سالانه، سعی کنید جمع مجموع اهرم رشد قیمت و بازده نقدی را کمی بالاتر هم ببرید.",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StockRed,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = borderCol.copy(alpha = 0.5f), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Educational calibration guidelines in premium Persian
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(goldAccent.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                            .border(1.dp, goldAccent.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "راهنمای کالیبراسیون",
                                            tint = goldAccent,
                                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "💡 راهنما به زبان ساده برای تنظیم فیلترهای سود و قیمت:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "۱. سود نقدی (DPS): یعنی مقداری از سود شرکت که پس از جلسه سالانه به حساب شما واریز می‌شود. بازده معمولی شرکت‌ها در ایران ۱۰ تا ۱۵ درصد است. اگر بالاتر بگذارید، بقیه شرکت‌ها مخفی می‌شوند.\n" +
                                                       "۲. پتانسیل رشد سهم: یعنی پیش‌بینی اینکه چقدر قیمت خود برگه سهم بالا می‌رود تا ارزش پول شما حفظ شود. با ترکیب عالی این دو مقدار، بهترین سهام‌های رشدکننده بورس را پیدا می‌کنید.",
                                                fontSize = 9.sp,
                                                color = mutedCol,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Tab 1 will handle the list feed
                        }
                    }

                    // TAB 3: ACCOUNT, AI SETTINGS & LOCAL SIMULATION OVERRIDES
                    if (currentTab == 3) {
                        item {
                            // 📱 Premium Visual Walkthrough & Interactive Screenshot Tutorial Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clickable { showVisualTutorialSheet = true },
                                colors = CardDefaults.cardColors(
                                    containerColor = goldAccent.copy(alpha = 0.08f)
                                ),
                                border = BorderStroke(1.2.dp, goldAccent.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(goldAccent.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = goldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📱 آکادمی تصویری و راهنمای قدم‌به‌قدم VIP",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = goldAccent
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "آموزش تصویری کاربری و راهنمای قدم‌به‌قدم مجمع‌یاب هوشمند",
                                            fontSize = 9.5.sp,
                                            color = mutedCol,
                                            lineHeight = 14.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack, // Persian RTL Arrow Back pointing outward
                                        contentDescription = null,
                                        tint = goldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

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
                                            placeholder = { Text("gemini-3.5-flash", fontSize = 11.sp, color = mutedCol) },
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

                            // 🔄 Diagnostic, Cache Clearance & Synchronizer Card (Direct Customer requested Reset / Cache and Deep-Sync Suite)
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceCol),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                var syncStatus by remember { mutableStateOf("") }

                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = goldAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "بروزرسانی اطلاعات و رفع مشکلات احتمالی برنامه",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textCol
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "اگر احساس می‌کنید اطلاعات سهام یا سودها قدیمی شده‌اند یا تنظیماتی را به اشتباه تغییر داده‌اید، با دکمه‌های زیر می‌توانید اطلاعات زنده بازار بورس را مجدداً دریافت یا برنامه را به حالت اولیه برگردانید:",
                                        fontSize = 10.sp,
                                        color = mutedCol,
                                        lineHeight = 15.sp
                                    )

                                    if (syncStatus.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(goldAccent.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                                .border(1.dp, goldAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = StockGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = syncStatus,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textCol
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Action 1: Refresh Bourse Data pipeline
                                    Button(
                                        onClick = {
                                            syncStatus = "در حال اتصال به وب سرویس بورس و بازخوانی اطلاعات..."
                                            viewModel.fetchMarketData()
                                            syncStatus = "✅ اطلاعات بورس تهران با موفقیت بازخوانی و به‌روز شد."
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = surfaceVariantCol,
                                            contentColor = textCol
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = goldAccent,
                                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                        )
                                        Text(
                                            text = "دریافت جدیدترین اطلاعات زنده سود سهام و قیمت‌ها",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action 2: Clear Application state / Calibration defaults
                                    Button(
                                        onClick = {
                                            viewModel.clearCacheAndDefaults()
                                            syncStatus = "✅ حافظه موقت پاک شد! تنظیمات برنامه به حالت اول بازگشت."
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = surfaceVariantCol,
                                            contentColor = textCol
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cached,
                                            contentDescription = null,
                                            tint = goldAccent,
                                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                        )
                                        Text(
                                            text = "پاک کردن حافظه موقت (برگشت تنظیمات به اول)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action 3: Hard destructive factory reset
                                    Button(
                                        onClick = {
                                            viewModel.performHardFactoryReset()
                                            syncStatus = "🔥 بازنشانی کامل انجام شد! پایگاه داده SQLite مجدداً سازماندهی گردید."
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StockRed.copy(alpha = 0.12f),
                                            contentColor = StockRed
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        border = BorderStroke(1.dp, StockRed.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = StockRed,
                                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                        )
                                        Text(
                                            text = "پاکسازی دیتابیس بورس و ریست فکتوری (Factory Reset)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
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
fun SignalCardView(stock: StockSignal, userMobile: String, viewModel: TradingViewModel, onShowGuide: () -> Unit) {
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

                // Clean growth badge (no nested click)
                Box(
                    modifier = Modifier
                        .background(
                            (if (isPositive) StockGreen else StockRed).copy(alpha = 0.12f),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            (if (isPositive) StockGreen else StockRed).copy(alpha = 0.4f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = (if (isPositive) "+" else "") + String.format(Locale.US, "%.2f", stock.changePercent) + "%",
                        fontSize = 12.sp,
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
                    Text(stock.priceLabel, fontSize = 10.sp, color = textMuted)
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
                    Text("${stock.displayEpsLabel}: ${stock.eps}", fontSize = 10.sp, color = textMuted)
                }
            }

            if (stock.warning.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF9800).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stock.warning,
                        fontSize = 11.sp,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
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
                        text = "پیش‌بینی سود نقدی (${stock.dpsSource}):",
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
                                    text = "ارزیابی هوشمند و تحلیل سود سهامدار (VIP)",
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
                                        text = "امتیاز کلی کیفیت سهم (از ۱۰۰)",
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
                                        text = "شاخص حفظ ارزش پول در برابر تورم",
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
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("inline_stock_calculator_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = goldClr.copy(alpha = 0.04f)),
                        border = BorderStroke(1.2.dp, goldClr.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = goldClr,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "ماشین‌حساب هوشمند سود نو مجمع (${stock.symbol})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "پیش‌بینی دقیق کارمزد خرید بورس، قیمت تمام‌شده مواجهه مجمع، سود نقدی تقسیمی و برآورد سهام جایزه فزر، شپدیس و ...",
                                fontSize = 10.sp,
                                color = textMuted,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = investmentAmountText,
                                onValueChange = { investmentAmountText = it },
                                label = { Text("مبلغ سرمایه‌گذاری مورد نظر (میلیون تومان)", fontSize = 11.sp, color = textMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                shape = RoundedCornerShape(12.dp),
                                suffix = { Text("میلیون تومان", fontSize = 10.sp, color = textMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = {
                                    Icon(Icons.Default.Savings, contentDescription = null, tint = goldClr, modifier = Modifier.size(16.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cardBorder,
                                    focusedBorderColor = goldClr,
                                    unfocusedTextColor = textPrimary,
                                    focusedTextColor = textPrimary,
                                    focusedLabelColor = goldClr,
                                    unfocusedLabelColor = textMuted
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Shortcut buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "۱۰" to "۱۰ میلیون",
                                    "۵۰" to "۵۰ میلیون",
                                    "۱۰۰" to "۱۰۰ میلیون",
                                    "۵۰۰" to "۵۰۰ میلیون"
                                ).forEach { (valStr, label) ->
                                    val isSelected = investmentAmountText == valStr
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) goldClr else goldClr.copy(alpha = 0.05f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) goldClr else cardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { investmentAmountText = valStr }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF0C0E14) else textPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = dividerColor, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(14.dp))

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

                            // Visual Grid Layout (2x2)
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Item: Shares count (A)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(itemBg, RoundedCornerShape(10.dp))
                                            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("تعداد کل برگ سهام:", fontSize = 9.sp, color = textMuted)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${formatPrice(sharesCountVal)} برگه سهم", fontSize = 12.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    // Item: Real cost basis (B)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(itemBg, RoundedCornerShape(10.dp))
                                            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("قیمت تمام شده (با کارمزد):", fontSize = 9.sp, color = textMuted)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${formatPrice(costBasisPerShareVal.toLong())} تومان", fontSize = 12.sp, color = goldClr, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Item: Brokerage fee (C)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(itemBg, RoundedCornerShape(10.dp))
                                            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("کارمزد خرید کارگزاری (۰.۳۷٪):", fontSize = 9.sp, color = textMuted)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${formatPrice(commissionFeeVal.toLong())} تومان", fontSize = 11.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                    }

                                    // Item: Table Price (D)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(itemBg, RoundedCornerShape(10.dp))
                                            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("قیمت جاری روی تابلو بورس:", fontSize = 9.sp, color = textMuted)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${formatPrice(sharePriceTomanVal)} تومان", fontSize = 11.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Divider(color = dividerColor.copy(alpha = 0.5f), thickness = 1.dp)

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "📊 پیش‌بینی دریافتی‌ها و بازدهی نهایی مجمع:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = goldClr,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )

                                    // Payout Ribbon 1: sajams dividend
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(StockGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .border(1.dp, StockGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StockGreen, modifier = Modifier.size(16.dp))
                                            Text("عایدی خالص نقدی سود مجمع (Sajam):", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                        }
                                        Text("${formatPrice(totalCashDividendVal.toLong())} تومان", fontSize = 12.sp, color = StockGreen, fontWeight = FontWeight.Black)
                                    }

                                    // Payout Ribbon 2: bonus shares
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(goldClr.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .border(1.dp, goldClr.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = goldClr, modifier = Modifier.size(16.dp))
                                            Text("سهام جایزه اعطایی رایگان مجمع ($capIncreasePercentVal%):", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                        }
                                        Text("${formatPrice(bonusSharesCountVal)} برگه سهم جایزه", fontSize = 11.sp, color = goldClr, fontWeight = FontWeight.Black)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Payout Ribbon 3: Total Asset Value Compound (The luxurious finish highlight!)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(goldClr.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                            .border(1.2.dp, goldClr, RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("ارزش کل دارایی و سودها بعد از یک سال:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                            Text("شامل اصل سرمایه + سود نهایی مرکب مجمع", fontSize = 8.sp, color = textMuted)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${formatPrice(finalCapitalValueVal.toLong())} تومان", fontSize = 14.sp, color = textPrimary, fontWeight = FontWeight.Black)
                                            Text("(+${formatPrice(netCompoundProfitVal.toLong())} عایدی مرکب)", fontSize = 10.sp, color = StockGreen, fontWeight = FontWeight.Bold)
                                        }
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
                                        modifier = Modifier.weight(1.1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "کپی",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text("کپی تلگرام", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            generateStockPdfReport(context, stock, currentAiResult, isDarkMode)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = "PDF",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text("دانلود PDF", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    val containsError = currentAiResult.contains("❌") || currentAiResult.contains("خطا")
                                    if (containsError) {
                                        Button(
                                            onClick = { viewModel.analyzeStockWithAI(stock.symbol, stock) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(1.1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (currentAiLoading) {
                                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = "تلاش مجدد",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                                Text("تلاش مجدد", fontSize = 9.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = onShowGuide,
                                            colors = ButtonDefaults.buttonColors(containerColor = goldClr.copy(alpha = 0.12f)),
                                            border = BorderStroke(1.dp, goldClr),
                                            modifier = Modifier.weight(1.1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.HelpOutline,
                                                    contentDescription = "راهنمای قدم به قدم",
                                                    tint = goldClr,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text("پوشه راهنما", fontSize = 9.sp, color = goldClr, fontWeight = FontWeight.Bold)
                                            }
                                        }
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
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "کپی",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text("کپی تلگرام", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                val demoAiText = "تحلیل بنیادی نماد ${stock.symbol} به عنوان عایق تورمی بورس با کلاس تعهد و سودآوری ${stock.dpsLevel} و بازده مجمع پیش‌فرض ممتاز ارزیابی می‌گردد."
                                generateStockPdfReport(context, stock, demoAiText, isDarkMode)
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // PDF Green
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text("گزارش PDF", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = goldClr),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن",
                                    tint = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text("بستن", fontSize = 10.sp, color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StepByStepGuideDialog(isDarkMode: Boolean, onDismiss: () -> Unit) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 4

    val goldClr = if (isDarkMode) GoldPrimary else Color(0xFFB39256)
    val textPrimary = if (isDarkMode) Color(0xFFF5F3EF) else Color(0xFF1F1B18)
    val textMuted = if (isDarkMode) SlateTextSecondary else Color(0xFF6B6259)
    val bgCol = if (isDarkMode) Color(0xFF11141E) else Color(0xFFFFFFFF)
    val borderCol = if (isDarkMode) BorderColor else Color(0xFFDFD9D0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = goldClr,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "راهنمای گام‌به‌گام پلتفرم مجمع‌یاب VIP",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = goldClr
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress counter
                Box(
                    modifier = Modifier
                        .background(goldClr.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "گام $currentStep از $totalSteps",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldClr
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                    },
                    label = "GuideStepTransition"
                ) { step ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        when (step) {
                            1 -> {
                                Text(
                                    text = "۱. پایشگر و فیلتر پیشرفته مجامع 📊",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "جدول اصلی برنامه لیستی از نمادهای برتر بورس با مجامع نزدیک را نمایش می‌دهد. شما می‌توانید با فیلتر هوشمند فقط نمادهای VIP (با بازده سود مجمع بالا)، سودهای زود واریز (زیر ۴ ماه) یا ارزش رشد فنی مناسب را به سرعت تفکیک کنید.",
                                    fontSize = 11.sp,
                                    color = textMuted,
                                    lineHeight = 18.sp
                                )
                            }
                            2 -> {
                                Text(
                                    text = "۲. ارزیابی ریسک و کلاس ایمنی مجمع ⚖️",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "هر مجمع دارای یک «کلاس تعهد ایمنی» (مثل رتبه A یا B) است. این رتبه بر اساس تاریخچه نحوه تسویه سود مجمع شرکت در کُدال و واریز سریع از بستر سجام مشخص شده است تا با اطمینان خاطر سرمایه‌گذاری کنید.",
                                    fontSize = 11.sp,
                                    color = textMuted,
                                    lineHeight = 18.sp
                                )
                            }
                            3 -> {
                                Text(
                                    text = "۳. گزارش زنده تحلیلی تلگرام و PDF 🤖",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "با کلیک بر روی گزینه «تحلیل VIP با هوش مصنوعی»، جمنای گزارش جامعی صادر می‌کند. با زدن دکمه «کپی تلگرام»، یک متن آماده به همراه جزئیات برای کانال‌های تلگرامی کپی می‌شود. دکمه «گزارش PDF» نیز یک خلاصه زیبا با رنگ‌های کلاسیک و تمیز برای ذخیره آفلاین صادر می‌نماید.",
                                    fontSize = 11.sp,
                                    color = textMuted,
                                    lineHeight = 18.sp
                                )
                            }
                            4 -> {
                                Text(
                                    text = "۴. ماشین‌حساب هوشمند سود سهامدار 🧮",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "در پایینِ بخش جزئیات هر سهم، یک ماشین‌حساب هوشمند قرار دارد. کافی‌ست مبلغ سرمایه‌گذاری دلخواه را وارد کنید تا سود نقدی نهایی و برگ سهام جایزه‌تان پیش از مجمع، همراه با تحلیل ارزش دارایی‌تان فوراً محاسبه شود.",
                                    fontSize = 11.sp,
                                    color = textMuted,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    TextButton(onClick = { currentStep-- }) {
                        Text("گام قبلی", color = goldClr, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentStep < totalSteps) {
                    Button(
                        onClick = { currentStep++ },
                        colors = ButtonDefaults.buttonColors(containerColor = goldClr),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("گام بعدی", color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = goldClr),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("فهمیدم، بستن", color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = bgCol,
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VisualTutorialDialog(isDarkMode: Boolean, onDismiss: () -> Unit) {
    var activeSlide by remember { mutableStateOf(1) }
    val totalSlides = 4

    val goldClr = if (isDarkMode) GoldPrimary else Color(0xFFB39256)
    val textPrimary = if (isDarkMode) Color(0xFFF5F3EF) else Color(0xFF1F1B18)
    val textMuted = if (isDarkMode) SlateTextSecondary else Color(0xFF6B6259)
    val bgCol = if (isDarkMode) Color(0xFF0F121C) else Color(0xFFFFFFFF)
    val cardBg = if (isDarkMode) Color(0xFF161A26) else Color(0xFFF9F7F3)
    val borderCol = if (isDarkMode) BorderColor else Color(0xFFDFD9D0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = goldClr,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "آکادمی تصویری VIP Bourse",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = goldClr
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Horizontal Slide Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..totalSlides) {
                        Box(
                            modifier = Modifier
                                .size(if (i == activeSlide) 18.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (i == activeSlide) goldClr else borderCol.copy(alpha = 0.4f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated Mobile Phone Screen Container representing the Screenshot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.5.dp, goldClr.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    AnimatedContent(
                        targetState = activeSlide,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
                        },
                        label = "ScreenshotTransition"
                    ) { slide ->
                        Column(modifier = Modifier.fillMaxSize()) {
                            when (slide) {
                                1 -> {
                                    // Slide 1: TABLE MOCKUP
                                    Text(
                                        text = "📱 نمای مجمع‌یاب و فیلترهای نخبگان",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = goldClr
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Draw a beautiful simulated stock row
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(bgCol, RoundedCornerShape(10.dp))
                                            .border(1.dp, borderCol.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(
                                                    modifier = Modifier.background(goldClr.copy(alpha = 0.15f), CircleShape).padding(4.dp)
                                                ) {
                                                    Text("فزر", fontSize = 10.sp, color = goldClr, fontWeight = FontWeight.Bold)
                                                }
                                                Column {
                                                    Text("پویا زرکان آق‌دره", fontSize = 9.sp, color = textPrimary)
                                                    Text("مجمع: ۱۴۰۵/۰۴/۱۵", fontSize = 8.sp, color = textMuted)
                                                }
                                            }
                                            // Royal indicator
                                            Box(
                                                modifier = Modifier.background(Color(0xFF2E7D32).copy(alpha = 0.12f), RoundedCornerShape(6.dp)).padding(4.dp)
                                            ) {
                                                Text("کلاس تعهد: A+", fontSize = 8.sp, color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("سود پیش‌بینی نقدی (DPS): ۸۹٪", fontSize = 8.sp, color = textMuted)
                                            Text("روز تا مجمع: ۱۴ روز", fontSize = 8.sp, color = Color.Red.copy(alpha = 0.8f))
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    // Explanation tool-tip pointing to it
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF2196F3).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                                        Text(
                                            text = "کلاس تعهد مجمع نشان‌دهنده سرعت تسویه سود سهامداران بوسیله سجام در مجامع قبلی است.",
                                            fontSize = 9.sp,
                                            color = textPrimary,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                                2 -> {
                                    // Slide 2: GEMINI AI & PDF EXPORTS
                                    Text(
                                        text = "🤖 گزارش هوشمند و خروجی‌های تلگرام/PDF",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = goldClr
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Draw simulated report screen
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(bgCol, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("گزارش تحلیلی VIP مجمع فاملی توسط جمنای 💎", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("«شرکت ملی صنایع مس با حاشیه سود بالا و تعهد پرداخت ۷۲ روزه سجام ارزیابی عالی دارد.»", fontSize = 8.sp, color = textMuted)
                                        
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.weight(1f).background(Color(0xFF1D9BF0), RoundedCornerShape(6.dp)).padding(6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("کپی تلگرام 📢", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier.weight(1f).background(Color(0xFF2E7D32), RoundedCornerShape(6.dp)).padding(6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("دانلود PDF 📥", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(goldClr.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = goldClr, modifier = Modifier.size(14.dp))
                                        Text(
                                            text = "با یک کلیک متن کپی شده را مستقیماً در کانال‌های تلگرامی قرار دهید یا گزارش مانیتورینگ PDF بسازید.",
                                            fontSize = 9.sp,
                                            color = textPrimary,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                                3 -> {
                                    // Slide 3: COUANT QUANT CALCULATOR MOCKUP
                                    Text(
                                        text = "🧮 ماشین‌حساب هوشمند سود و اثرات تورمی",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = goldClr
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Draw calculator UI mockup
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(bgCol, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("مبلغ سرمایه‌گذاری فرضی:", fontSize = 8.sp, color = textMuted)
                                            Text("۱۰۰,۰۰۰,۰۰۰ تومان", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                        }
                                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = borderCol.copy(alpha = 0.2f))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("خالص سود نقدی مجمع (ریال):", fontSize = 8.sp, color = textMuted)
                                            Text("۱۲,۵۰۰,۰۰۰ تومان (۱۲.۵٪)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("شاخص موازنه تورم سال:", fontSize = 8.sp, color = textMuted)
                                            Text("ضمانت خرید ۳۸٪ تورم", fontSize = 8.sp, color = goldClr, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF4ADE80).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(14.dp))
                                        Text(
                                            text = "محاسبه خودکار ارزش ریالی سود نقدی مجمع، تعداد سهام جایزه اضافه و کارمزدهای خرید کارگزاری.",
                                            fontSize = 9.sp,
                                            color = textPrimary,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                                4 -> {
                                    // Slide 4: OFFLINE DATABASE PORTFOLIO
                                    Text(
                                        text = "🗄️ ذخیره‌سازی آفلاین و تنظیمات شخصی شما",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = goldClr
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Draw Database portfolio mockup
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(bgCol, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("📁 سبد فرضی سفارشی مجامع من (مستقیم در SQLite)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("شرکت‌های با فرضیه سود مجمع اصلاح شده توسط شما به شکل آفلاین نگهداری می‌شوند.", fontSize = 8.sp, color = textMuted)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier.fillMaxWidth().background(goldClr.copy(alpha = 0.12f), RoundedCornerShape(6.dp)).padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("بازیابی الگوها و ذخیره‌سازی مراجع بورس بومی", fontSize = 8.sp, color = goldClr)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFE91E63).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(14.dp))
                                        Text(
                                            text = "ذخیره محلی و هوشمند این امکان را می‌دهد تا بدون نیاز به اینترنت، همیشه به اطلاعات و تحلیل سهام خود دسترسی داشته باشید.",
                                            fontSize = 9.sp,
                                            color = textPrimary,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Explain text below screenshot mockup
                Text(
                    text = when (activeSlide) {
                        1 -> "💡 در جدول فیلترها، رتبه تعهد 'A' یعنی تسویه سریع سود زیر ۷۰ روز. با تغییر فیلترها سهام مطابق با ایده خود را پیدا کنید."
                        2 -> "💡 متن خروجی تلگرام ساختار آماده مارک‌داون دارد و کپی مستقیم در پیام‌رسان‌ها را آسان می‌کند."
                        3 -> "💡 با تغییر حجم سهام در ماشین‌حساب، سود خالص تخمینی شما بر اساس کسر کارمزد دقیق سپرده‌گذاری محاسبه می‌شود."
                        else -> "💡 پورتفوی شخصی شما در محیط مجمع‌یاب کاملاً آفلاین و مطمئن روی حافظه داخلی گوشی ذخیره می‌گردد."
                    },
                    fontSize = 10.sp,
                    color = textMuted,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (activeSlide > 1) {
                    TextButton(onClick = { activeSlide-- }) {
                        Text("گام قبلی", color = goldClr, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (activeSlide < totalSlides) {
                    Button(
                        onClick = { activeSlide++ },
                        colors = ButtonDefaults.buttonColors(containerColor = goldClr),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("گام بعدی", color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = goldClr),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تکمیل آموزش تصویری", color = if (isDarkMode) Color(0xFF0C0E14) else Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = bgCol,
        shape = RoundedCornerShape(16.dp)
    )
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
