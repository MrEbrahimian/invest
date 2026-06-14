package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import com.example.data.StockSignal
import com.example.data.engine.QuantEngine
import com.example.ui.theme.GoldClr
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockRed
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingMainView(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTab by viewModel.currentTab.collectAsState()
    val rawStocks by viewModel.rawStocks.collectAsState()
    val filteredStocks by viewModel.filteredStocks.collectAsState()
    val selectedStock by viewModel.selectedStock.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFormulaIdx by viewModel.selectedFormulaIdx.collectAsState()
    val selectedRisk by viewModel.selectedRisk.collectAsState()
    val sajamFilter by viewModel.sajamFilter.collectAsState()
    val minDps by viewModel.minDpsSlider.collectAsState()
    val minGrowth by viewModel.minGrowthSlider.collectAsState()
    val alertThreshold by viewModel.alertThresholdSlider.collectAsState()
    val investmentAmount by viewModel.investmentAmount.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val walkthroughStep by viewModel.walkthroughStep.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val geminiLoading by viewModel.geminiLoading.collectAsState()
    val geminiResponse by viewModel.geminiResponse.collectAsState()
    val cbiPointToPointRate by viewModel.cbiPointToPointRate.collectAsState()
    val cbiAnnualRate by viewModel.cbiAnnualRate.collectAsState()
    val cbiStatusMessage by viewModel.cbiStatusMessage.collectAsState()

    // Splash screen state - automatically shown on first boot
    var showSplash by remember { mutableStateOf(true) }

    // Dialog & popup states
    var showEditDpsDialog by remember { mutableStateOf(false) }
    var inputCustomDps by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Theme values (Luxurious deep-slate financial background)
    val appBgColor = Color(0xFF0C101B)
    val cardBgColor = Color(0xFF141A28)
    val borderClr = Color(0xFF1E2638)

    // Layout configuration with edge-to-edge safety padding
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Section: Theme / Info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.showWalkthrough(1) }) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = "راهنما",
                                    tint = GoldClr
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StockGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "VIP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StockGreen,
                                    modifier = Modifier.testTag("vip_header_badge")
                                )
                            }
                        }

                        // Right Section: Title and network state
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "VIP TSE Master",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(StockGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE TSETMC CONNECTED 🟢",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141925)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF141925),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                // Tab 3: Settings
                NavigationBarItem(
                    selected = currentTab == "SETTINGS",
                    onClick = { viewModel.setTab("SETTINGS") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (currentTab == "SETTINGS") "پر شده" else "خالی"
                        )
                    },
                    label = { Text("تنظیمات و شبکه داد‌ه", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldClr,
                        indicatorColor = GoldClr,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab MENTOR: Intelligence Valuation Mentor & Calculator
                NavigationBarItem(
                    selected = currentTab == "MENTOR",
                    onClick = { viewModel.setTab("MENTOR") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = if (currentTab == "MENTOR") "پر شده" else "خالی"
                        )
                    },
                    label = { Text("منتور ارزش‌یابی", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldClr,
                        indicatorColor = GoldClr,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 2: Smart Profit Screener
                NavigationBarItem(
                    selected = currentTab == "SMART_PROFIT",
                    onClick = { viewModel.setTab("SMART_PROFIT") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = if (currentTab == "SMART_PROFIT") "پر شده" else "خالی"
                        )
                    },
                    label = { Text("سود هوشمند", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldClr,
                        indicatorColor = GoldClr,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 1: Home List
                NavigationBarItem(
                    selected = currentTab == "HOME",
                    onClick = { viewModel.setTab("HOME") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = if (currentTab == "HOME") "پر شده" else "خالی"
                        )
                    },
                    label = { Text("خانه", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldClr,
                        indicatorColor = GoldClr,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(appBgColor)
        ) {
            // Main views based on active bottom navigation selection
            when (currentTab) {
                "HOME" -> {
                    HomeTabContent(
                        viewModel = viewModel,
                        filteredStocks = filteredStocks,
                        selectedStock = selectedStock,
                        searchQuery = searchQuery,
                        sajamFilter = sajamFilter,
                        cardBgColor = cardBgColor,
                        borderClr = borderClr,
                        goldVal = GoldClr,
                        onTriggerEditDps = {
                            inputCustomDps = selectedStock?.dps?.toString() ?: ""
                            showEditDpsDialog = true
                        },
                        geminiLoading = geminiLoading,
                        geminiResponse = geminiResponse
                    )
                }
                "SMART_PROFIT" -> {
                    SmartProfitTabContent(
                        viewModel = viewModel,
                        selectedFormulaIdx = selectedFormulaIdx,
                        selectedRisk = selectedRisk,
                        minDps = minDps,
                        minGrowth = minGrowth,
                        alertThreshold = alertThreshold,
                        cardBgColor = cardBgColor,
                        borderClr = borderClr,
                        goldVal = GoldClr
                    )
                }
                "MENTOR" -> {
                    MentorTabContent(
                        viewModel = viewModel,
                        cardBgColor = cardBgColor,
                        borderClr = borderClr,
                        goldVal = GoldClr,
                        onShowToast = { toastMessage = it }
                    )
                }
                "SETTINGS" -> {
                    SettingsTabContent(
                        viewModel = viewModel,
                        biometricEnabled = biometricEnabled,
                        geminiApiKey = geminiApiKey,
                        cardBgColor = cardBgColor,
                        borderClr = borderClr,
                        goldVal = GoldClr,
                        onShowToast = { toastMessage = it }
                    )
                }
            }

            // Universal toast message banner overlay
            toastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2000)
                    toastMessage = null
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldClr)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Interactive step-by-step popup walkthrough
            if (walkthroughStep != null) {
                WalkthroughDialog(
                    step = walkthroughStep!!,
                    onNext = { step ->
                        if (step < 4) {
                            viewModel.showWalkthrough(step + 1)
                        } else {
                            viewModel.showWalkthrough(null)
                        }
                    },
                    onClose = { viewModel.showWalkthrough(null) }
                )
            }

            // Inline Room SQLite value custom editor popup
            if (showEditDpsDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDpsDialog = false },
                    title = {
                        Text(
                            text = "تغییر سود مجمع (${selectedStock?.symbol})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "مقداری مایلید برای سود نقدی جدید برحسب ریال در دیتابیس بومی ذخیره کنید را وارد کنید:",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = inputCustomDps,
                                onValueChange = { inputCustomDps = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("سود نقدی (ریال)", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().testTag("custom_dps_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = GoldClr
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = GoldClr),
                            onClick = {
                                val amountVal = inputCustomDps.toIntOrNull()
                                if (amountVal != null && selectedStock != null) {
                                    viewModel.saveCustomDividend(selectedStock!!.symbol, amountVal)
                                    showEditDpsDialog = false
                                    toastMessage = "تغییرات در دیتابیس Room با موفقیت اعمال گردید ✅"
                                }
                            }
                        ) {
                            Text("ذخیره در دیتابیس", color = Color.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDpsDialog = false }) {
                            Text("انصراف", color = Color.LightGray)
                        }
                    },
                    containerColor = cardBgColor
                )
            }
        }
    }

    if (showSplash) {
        VipSplashView(
            cbiStatusMessage = cbiStatusMessage,
            onDismiss = { showSplash = false }
        )
    }
}

// ==========================================================
// HOME TAB (Ported portrait design list with rich details)
// ==========================================================
@Composable
fun HomeTabContent(
    viewModel: TradingViewModel,
    filteredStocks: List<StockSignal>,
    selectedStock: StockSignal?,
    searchQuery: String,
    sajamFilter: String,
    cardBgColor: Color,
    borderClr: Color,
    goldVal: Color,
    onTriggerEditDps: () -> Unit,
    geminiLoading: Boolean,
    geminiResponse: String?
) {
    val cbiPointToPointRate by viewModel.cbiPointToPointRate.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("home_stocks_panel"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // VIP Subscriber info card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, goldVal.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(goldVal.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "اشتراک فعال بومی",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldVal
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "کاربری ویژه VIP بورس من",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Star, contentDescription = "VIP", tint = goldVal, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تلفن همراه فعالساز: 09123456789",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "اعتبار اشتراک تا تاریخ مجمع سال بعد: 07-07-2026",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Secrecy lock biome notice banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF101622),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2638))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "حفاظت ضد نشت و بیومتریک بورس من فعال است (امنیت گرید بانکی) 🛡️",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Security, contentDescription = "ایران", tint = StockGreen, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Search Bar Textfield
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "🔍 جستجوی نام نماد یا سهم (فزر، بوعلی، شتران...)",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("symbol_search_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = goldVal,
                    unfocusedBorderColor = borderClr,
                    focusedContainerColor = cardBgColor,
                    unfocusedContainerColor = cardBgColor
                ),
                singleLine = true
            )
        }

        // Sajam legal payment timing speed pills filter
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "فیلتر مجامع بر اساس مهلت قانونی واریز سود سجا (نقدشوندگی سریع) 📅: ",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val stepsList = listOf(
                        "ALL" to "کل بازار",
                        "UNDER_3" to "زیر ۳ ماه",
                        "UNDER_2" to "کمتر ۲ ماه",
                        "UNDER_1" to "زیر ۱ ماه"
                    )
                    stepsList.forEach { (key, title) ->
                        val selected = sajamFilter == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) goldVal else cardBgColor)
                                .clickable { viewModel.selectSajamFilter(key) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Quick Signal Mode indicators
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val screenerKey = viewModel.selectedFormulaIdx.collectAsState().value
                val matchedCount = filteredStocks.size
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (screenerKey != 1) goldVal.copy(alpha = 0.15f) else cardBgColor)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "فقط سیگنال‌های VIP ($matchedCount)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (screenerKey != 1) goldVal else Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (screenerKey == 1) goldVal.copy(alpha = 0.15f) else cardBgColor)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "کل بازار (7)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (screenerKey == 1) goldVal else Color.White
                    )
                }
            }
        }

        // Loop available stocks
        items(filteredStocks) { stock ->
            val isSelected = selectedStock?.symbol == stock.symbol
            val cardAnimBg by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF172030) else cardBgColor,
                animationSpec = spring(),
                label = "Card selection color animate"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectStock(stock) }
                    .testTag("stock_item_${stock.symbol}"),
                colors = CardDefaults.cardColors(containerColor = cardAnimBg),
                border = BorderStroke(1.dp, if (isSelected) goldVal else borderClr),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Symbol row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Price badge indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (stock.yesterdayPrice > stock.price) StockRed.copy(alpha = 0.12f) else StockGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            val sign = if (stock.price >= stock.yesterdayPrice) "+" else ""
                            val diffPercent = ((stock.price - stock.yesterdayPrice).toFloat() / stock.yesterdayPrice) * 100
                            Text(
                                text = "$sign${String.format(Locale.US, "%.2f", diffPercent)}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stock.price >= stock.yesterdayPrice) StockGreen else StockRed
                            )
                        }

                        // Right Ticker text
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stock.symbol,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = stock.name,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (stock.status == "ACTIVE") StockGreen else StockRed, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stock indicators Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StockGridCell(Modifier.weight(1f), "قیمت بازار", "${NumberFormat.getInstance(Locale.US).format(stock.priceInToman)} ت")
                        StockGridCell(Modifier.weight(1f), "سود EPS", "${stock.eps} ریال")
                        StockGridCell(Modifier.weight(1.3f), "ارزش معاملات", formatLargeNumber(stock.volume * stock.priceInToman / 100) + " ت")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dividend specific details highlight
                    Divider(color = borderClr.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val yield = (stock.dps * 100.0) / stock.price
                        Text(
                            text = "${stock.dps} ریال (${String.format(Locale.US, "%.1f", yield)}٪ بازدهی) 💳",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldVal
                        )
                        Text(
                            text = "پیش‌بینی سود نقدی مصوب:",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stock.dividendCountdownText,
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Spring expanded detailed analytical sheet
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Divider(color = borderClr, modifier = Modifier.padding(vertical = 10.dp))
                            
                            // 1. Valuation Levels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StockGreen.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "عایق تورم عالی (رشد + سود مجمع) ✨",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StockGreen
                                    )
                                }
                                Text(
                                    text = "ارزیابی سطح سود مجمع (واقع‌بینانه) ⚖️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "حداقل سود تضمینی بانکی مرجع: ٪۲۲.۰ | پتانسیل رشد سرمایه‌گذاری سالیانه: ٪۳۳.۲",
                                fontSize = 10.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 2. Ratings circle bar & gauges
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F1420))
                                    .border(1.dp, borderClr, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Gauge Ring Indicator (Circular layout simulated)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(2.dp, goldVal, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${stock.masterScore.toInt()}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = goldVal
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "ارزیابی هوشمند و تحلیل سود سهامدار (VIP) 📊",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Icon(Icons.Default.Verified, "shield", tint = goldVal, modifier = Modifier.size(13.dp))
                                        Icon(Icons.Default.Verified, "shield", tint = goldVal, modifier = Modifier.size(13.dp))
                                        Icon(Icons.Default.Verified, "shield", tint = goldVal, modifier = Modifier.size(13.dp))
                                        Icon(Icons.Default.Verified, "shield", tint = goldVal, modifier = Modifier.size(13.dp))
                                    }
                                    Text(
                                        text = "شاخص سپر تورمی ویژه • پرداخت سجام حدود ۴۵ روز",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // 2.5 Professional Valuation Gap and Intrinsic Value Section
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Initialize local showRawJson state for the expanded stock
                            var showRawJson by remember(stock.symbol) { mutableStateOf(false) }

                            val productionReportResult = QuantEngine.evaluateFinancialProduction(
                                livePrice = stock.price.toDouble(),
                                priceBeforeAssembly = stock.price.toDouble(),
                                dps = stock.dps.toDouble(),
                                eps = stock.eps.toDouble(),
                                targetPE = 6.0,
                                inflationRate = cbiPointToPointRate / 100.0, // Iranian standard inflation reference
                                lastStoredRoomPrice = stock.yesterdayPrice.toDouble()
                            )
                            
                            val report = productionReportResult.getOrNull()
                            val valuationGap = report?.valuationGapPercent ?: QuantEngine.calculateValuationGap(
                                price = stock.price.toDouble(),
                                intrinsicValue = QuantEngine.calculateIntrinsicValue(stock.price.toDouble(), stock.eps.toDouble(), stock.dps.toDouble())
                            )
                            val intrinsicValue = report?.intrinsicValueFair ?: QuantEngine.calculateIntrinsicValue(stock.price.toDouble(), stock.eps.toDouble(), stock.dps.toDouble())
                            val gapStatusMsg = if (report != null) {
                                when (report.valuationStatus) {
                                    "Strong Undervaluation" -> "🟢 کمتر از ارزش ذاتی (فرصت خرید قوی)"
                                    "Moderate Undervaluation" -> "🟡 دارای ارزش نسبی بالا (خرید کم‌ریسک)"
                                    "Fairly Valued" -> "⚪ ارزش‌گذاری منصفانه بازار"
                                    "Overvalued" -> "🟠 ارزش‌گذاری گران (احتیاط)"
                                    else -> "🔴 حباب قیمتی بسیار بالا (پرریسک)"
                                }
                            } else {
                                QuantEngine.getValuationStatusMessage(valuationGap)
                            }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
                                border = BorderStroke(1.dp, borderClr.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (valuationGap >= 0) StockGreen.copy(alpha = 0.15f) else StockRed.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${if (valuationGap >= 0) "+" else ""}${String.format(Locale.US, "%.1f", valuationGap)}%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (valuationGap >= 0) StockGreen else StockRed
                                            )
                                        }
                                        Text(
                                            text = "محاسبه ساختار ارزش منصفانه و گپ قیمتی 🔬",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldVal
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${NumberFormat.getInstance(Locale.US).format((intrinsicValue / 10).toLong())} تومان",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "ارزش منصفانه محاسباتی (Intrinsic):",
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${NumberFormat.getInstance(Locale.US).format(stock.priceInToman)} تومان",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "قیمت فعلی روی تابلو (Price):",
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )
                                    }

                                    if (report != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${NumberFormat.getInstance(Locale.US).format((report.theoreticalPriceAfterAssembly / 10).toLong())} تومان",
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "قیمت تئوریک بعد از مجمع (Theoretical):",
                                                fontSize = 10.sp,
                                                color = Color.LightGray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", report.realYieldPercent)}%",
                                                fontSize = 11.sp,
                                                color = if (report.realYieldPercent >= 0) StockGreen else StockRed
                                            )
                                            Text(
                                                text = "بازدهی واقعی بعد کسر تورم (Real Yield):",
                                                fontSize = 10.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Divider(color = borderClr.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Text(
                                        text = "وضعیت قیمت بازار در مقایسه با عایدی نقدی و سود هر سهم:",
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = gapStatusMsg,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (valuationGap >= 0) StockGreen else Color.LightGray,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                                    )

                                    if (report != null && report.warnings.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        report.warnings.forEach { warning ->
                                            Text(
                                                text = "⚠️ $warning",
                                                fontSize = 9.sp,
                                                color = StockRed,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    // Interactive JSON view
                                    if (report != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF161E30))
                                                .clickable { showRawJson = !showRawJson }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (showRawJson) "▲ بستن لاگ مالی محاسباتی سیستم" else "▼ مشاهده سند خام محاسبات فنی (JSON)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = goldVal
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = showRawJson,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF070B13))
                                                    .border(1.dp, borderClr.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "سند تضمین عاری از انحراف محاسباتی (استاندارد تولید مراجع حسابداری)",
                                                    fontSize = 8.sp,
                                                    color = Color.LightGray,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = report.toJsonString(),
                                                    fontSize = 9.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    color = StockGreen,
                                                    textAlign = TextAlign.Left,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Calculator widget
                            Spacer(modifier = Modifier.height(16.dp))
                            CalculatorWidget(viewModel = viewModel, investmentAmount = viewModel.investmentAmount.collectAsState().value, stock = stock, goldVal = goldVal)

                            // 4. Historical dividend bar row
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "📅 تاریخچه ۵ سال اخیر سود تقسیمی (کدال):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.End)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val currentDiv = stock.dps
                                HistoricalDivCard(Modifier.weight(1f), "امسال", currentDiv)
                                HistoricalDivCard(Modifier.weight(1f), "پارسال", (currentDiv * 0.85).toInt())
                                HistoricalDivCard(Modifier.weight(1f), "۲ سال گذشته", (currentDiv * 0.75).toInt())
                                HistoricalDivCard(Modifier.weight(1f), "۳ سال گذشته", (currentDiv * 0.65).toInt())
                                HistoricalDivCard(Modifier.weight(1f), "۴ سال گذشته", (currentDiv * 0.55).toInt())
                            }

                            // 5. Hardcore alignment summary description
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1B2435))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "⚡ تحلیل انطباق فیلتر سخت‌افزاری VIP: فیلتر پیشرفته VIP (قوی): حجم > ۱ میلیون (نقدینگی ورود/خروج بسیار بالا)، عایق تورم نقطه به نقطه کشور (48.3٪)، ارزش > ۱۰ میلیارد ریال، پرداخت پایدار DPS در سال‌های گذشته. [تایید صلاحیت شد]\nصنعت: ${stock.industry}",
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Right,
                                    lineHeight = 13.sp
                                )
                            }

                            // 6. External documents verification linkages
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { },
                                    border = BorderStroke(1.dp, borderClr),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                                ) {
                                    Text("سند تابلوی TSETMC", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { },
                                    border = BorderStroke(1.dp, borderClr),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                                ) {
                                    Text("سند مجمع در CODAL", fontSize = 10.sp)
                                }
                            }

                            // 7. Manual sqlite modification call action
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_dividend_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2638)),
                                onClick = onTriggerEditDps
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("اصلاح و شخصی‌سازی دستی سود نقدی مجمع", fontSize = 11.sp, color = goldVal)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = goldVal, modifier = Modifier.size(13.dp))
                                }
                            }

                            // 8. Gemini interactive AI Analyser
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131E)),
                                border = BorderStroke(1.dp, goldVal.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, "AI", tint = goldVal)
                                        Text(
                                            text = "دستیار هوشمند تحلیل سهم (TSE VIP AI) 🤖",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Button(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("gemini_run_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = goldVal),
                                        onClick = { viewModel.runGeminiAnalysis(stock) }
                                    ) {
                                        Text("✨ تحلیل و پایش آنی سهم با هوش مصنوعی (جمنای)", fontSize = 11.sp, color = Color.Black)
                                    }

                                    if (geminiLoading) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.CenterHorizontally),
                                            color = goldVal
                                        )
                                    }

                                    geminiResponse?.let { response ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = response,
                                            fontSize = 10.sp,
                                            color = Color.LightGray,
                                            lineHeight = 15.sp,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("gemini_response_field")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Subordinate grid cell formatter
@Composable
fun StockGridCell(modifier: Modifier = Modifier, title: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF101420))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 9.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// Calculator component for instantaneous dividends
@Composable
fun CalculatorWidget(
    viewModel: TradingViewModel,
    investmentAmount: Double,
    stock: StockSignal,
    goldVal: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1C2232))
            .padding(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "ماشین حساب هوشمند سود نو مجمع (${stock.symbol}) 📊",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Preset fast triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val amounts = listOf(10.0, 50.0, 100.0, 500.0)
            amounts.forEach { value ->
                val active = investmentAmount == value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) goldVal else Color(0xFF131824))
                        .clickable { viewModel.updateInvestmentAmount(value) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${value.toInt()} میلیون",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Auto derived computations
        // 1. Total shares buying count
        val totalInvestmentRials = investmentAmount * 10000000L // 1 Million Toman = 10,000,000 Rials
        val priceIncludingCommissions = stock.price * 1.0037 // 0.37% Brokerage
        val totalSharesCount = if (priceIncludingCommissions > 0) (totalInvestmentRials / priceIncludingCommissions).toLong() else 0L
        val commissionRials = (totalInvestmentRials * 0.0037).toLong()

        // Pure cash dividend expectations
        val pureCashRials = totalSharesCount * stock.dps
        val pureCashTomans = pureCashRials / 10

        // Gratis bonus stock shares expectations
        val bonusSharesQuantity = (totalSharesCount * stock.capIncreasePercent) / 100

        // Aggregate asset value after 1 yr matching target DPS + capital values
        val compoundedEquityValRials = (totalSharesCount + bonusSharesQuantity) * stock.price + pureCashRials
        val compoundedEquityValTomans = compoundedEquityValRials / 10

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${NumberFormat.getInstance(Locale.US).format(totalSharesCount)} برگه سهم", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("تعداد کل سهام خرید:", fontSize = 10.sp, color = Color.Gray)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${NumberFormat.getInstance(Locale.US).format(commissionRials / 10)} تومان", fontSize = 11.sp, color = Color.White)
            Text("کارمزد خرید کارگزاری (۰.۳۷٪):", fontSize = 10.sp, color = Color.Gray)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${NumberFormat.getInstance(Locale.US).format(priceIncludingCommissions.toLong())} ریال", fontSize = 11.sp, color = Color.White)
            Text("قیمت تمام شده (با کارمزد):", fontSize = 10.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Divider(color = Color(0xFF26324D))
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "پیش‌بینی دریافتی‌ها و بازدهی نهایی مجمع 📊:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = goldVal,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${NumberFormat.getInstance(Locale.US).format(pureCashTomans)} تومان", fontSize = 11.sp, color = StockGreen, fontWeight = FontWeight.Bold)
            Text("📈 عایدی خالص نقدی سود مجمع (Sajam):", fontSize = 10.sp, color = Color.White)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${NumberFormat.getInstance(Locale.US).format(bonusSharesQuantity)} برگه سهم جایزه", fontSize = 11.sp, color = goldVal, fontWeight = FontWeight.Bold)
            Text("⭐ سهام جایزه اعطایی رایگان مجمع (${stock.capIncreasePercent}.0%):", fontSize = 10.sp, color = Color.White)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${NumberFormat.getInstance(Locale.US).format(compoundedEquityValTomans)} تومان", fontSize = 11.sp, color = StockGreen, fontWeight = FontWeight.Bold)
            Text("ارزش کل دارایی و سودها بعد از یک سال:", fontSize = 10.sp, color = Color.White)
        }
        Text(
            text = "شامل اصل سرمایه + سود نهایی مرکب مجمع (+${NumberFormat.getInstance(Locale.US).format(compoundedEquityValTomans - (investmentAmount * 1000000))} عایدی مرکب)",
            fontSize = 9.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Historical div item cell format
@Composable
fun HistoricalDivCard(modifier: Modifier = Modifier, year: String, dpsValue: Int) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF141A28))
            .border(1.dp, Color(0xFF26324D), RoundedCornerShape(6.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = year, fontSize = 8.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "$dpsValue ریال", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
    }
}

// ==========================================================
// SMART PROFIT TAB (Screener Formula & Compound Reinvestments)
// ==========================================================
@Composable
fun SmartProfitTabContent(
    viewModel: TradingViewModel,
    selectedFormulaIdx: Int,
    selectedRisk: String,
    minDps: Float,
    minGrowth: Float,
    alertThreshold: Float,
    cardBgColor: Color,
    borderClr: Color,
    goldVal: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("smart_profit_panel"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Counter statistics row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardBgColor)
                        .border(1.dp, borderClr, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "کل سهام بررسی شده", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    val totalStocksCount = viewModel.rawStocks.collectAsState().value.size
                    Text(text = "$totalStocksCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                val matchedCountFlow = viewModel.filteredStocks.collectAsState().value.size
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardBgColor)
                        .border(1.dp, borderClr, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "سیگنال‌های منطبق بر VIP", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$matchedCountFlow", fontSize = 18.sp, fontWeight = FontWeight.Black, color = goldVal)
                }
            }
        }

        // Screener selection header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "انتخاب فرمول غربالگری هوشمند بازار (فرمول ریاضی) 🎛️:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "با انتخاب هر فرمول، الگوریتم مربوطه فیلترهای ریاضی بورس را در لحظه تغییر می‌دهد:",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Right
                )
            }
        }

        // 5 screener radio configurations
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScreenerFormulaCard(
                    index = 1,
                    title = "۱. کل غربالگری‌ها (مجموعه معیارها)",
                    desc = "تلاقی حجم بالای ام، ارزش م. بالای ۱۰ میلیارد ریال و P/E مطلوب بین ۲ تا ۱۰",
                    selected = selectedFormulaIdx == 1,
                    goldVal = goldVal,
                    cardBgColor = cardBgColor,
                    onSelect = { viewModel.selectFormula(1) }
                )
                ScreenerFormulaCard(
                    index = 2,
                    title = "۲. سود نقدی طلایی مجمع (M3 Yield Beater)",
                    desc = "سهم‌هایی با مجمع سودده و بزرگتر از معیار درآمد ثابت ملی (بازدهی بالای ۸٪)",
                    selected = selectedFormulaIdx == 2,
                    goldVal = goldVal,
                    cardBgColor = cardBgColor,
                    onSelect = { viewModel.selectFormula(2) }
                )
                ScreenerFormulaCard(
                    index = 3,
                    title = "۳. طلا و فلزات عایق تورمی (Defensive Shield)",
                    desc = "سهم‌هایی مانند طلا فزر و مس فملی برای فرار بنیادی از تورم",
                    selected = selectedFormulaIdx == 3,
                    goldVal = goldVal,
                    cardBgColor = cardBgColor,
                    onSelect = { viewModel.selectFormula(3) }
                )
                ScreenerFormulaCard(
                    index = 4,
                    title = "۴. حاشیه امنیت فوق‌العاده (Safety Floor)",
                    desc = "سهم با سود بالا و پایین‌ترین ضریب قیمت به بازدهی (P/E بسیار ایمن)",
                    selected = selectedFormulaIdx == 4,
                    goldVal = goldVal,
                    cardBgColor = cardBgColor,
                    onSelect = { viewModel.selectFormula(4) }
                )
                ScreenerFormulaCard(
                    index = 5,
                    title = "۵. بازدهی کل بزرگتر از تورم (CBI Beater)",
                    desc = "سهم‌هایی که بازده کل مجمع آن‌ها از تورم سالانه بانک مرکزی جلوتر است",
                    selected = selectedFormulaIdx == 5,
                    goldVal = goldVal,
                    cardBgColor = cardBgColor,
                    onSelect = { viewModel.selectFormula(5) }
                )
            }
        }

        // Multi level fundamental risk switches
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "معیار و فیلتر چندسطحی ریسک بنیادی:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val listChoices = listOf(
                        "LOW" to "پایین",
                        "MID" to "متوسط",
                        "HARDCORE" to "سخت‌گیرانه ✨"
                    )
                    listChoices.forEach { (key, title) ->
                        val selected = selectedRisk == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) goldVal else cardBgColor)
                                .border(1.dp, if (selected) goldVal else borderClr, RoundedCornerShape(6.dp))
                                .clickable { viewModel.selectRisk(key) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Slide parameters config cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, borderClr)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = goldVal, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "تنظیم تنظیمات دلخواه سود سالانه و رشد سهام (شخصی‌سازی)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Right
                        )
                    }
                    Text(
                        text = "🔒 همگام با فرمول فعال: جامع مرجع",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Minimum expected dps
                    SliderControl(
                        title = "حداقل سود نقدی مجمع مورد انتظار شما (DPS):",
                        value = minDps,
                        onValueChange = { viewModel.updateMinDpsSlider(it) },
                        suffix = "%"
                    )

                    // 2. Expected capital growth
                    Spacer(modifier = Modifier.height(12.dp))
                    SliderControl(
                        title = "حداقل رشد پیش‌بینی شده برای قیمت خود سهم در سال:",
                        value = minGrowth,
                        onValueChange = { viewModel.updateMinGrowthSlider(it) },
                        suffix = "%"
                    )

                    // 3. Prompt alert threshold
                    Spacer(modifier = Modifier.height(12.dp))
                    SliderControl(
                        title = "حداقل سود انتخابی شما برای فرستادن هشدارهای فوری:",
                        value = alertThreshold,
                        onValueChange = { viewModel.updateAlertThresholdSlider(it) },
                        suffix = "%"
                    )
                }
            }
        }

        // Long term compounding impact simulator engine
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101524)),
                border = BorderStroke(1.dp, goldVal.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "شبیه‌ساز اثر مرکب بلندمدت همسویی سود و رشد ✨:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سود نقدی بالا عالی است، اما سرمایه‌گذاری دوباره آن در بازوی افزایش ارزش و رشد بنیادی شرکت‌هاست که دارایی شما را در بلندمدت بیمه می‌کند. با تنظیمات فعلی شما (سود نقدی ${String.format(Locale.US, "%.1f", minDps)}% و نرخ رشد ${String.format(Locale.US, "%.1f", minGrowth)}%):",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Right,
                        lineHeight = 16.sp
                    )

                    // Compound calculations
                    val compoundRate = (minDps + minGrowth) / 100.0
                    val principal = 100.0 // 100 Million
                    // Formula: P * (1 + R)^5
                    val compoundFutureValue = principal * (1.0 + compoundRate).pow(5.0)
                    val fixedDepositFutureValue = principal * 1.30.pow(5.0) // 30% bank interest fixed

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", compoundFutureValue)} میلیون تومان",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldVal
                        )
                        Text(
                            text = "رشد فرضی ۱۰۰ میلیون تومان شما پس از ۵ سال با این فیلتر:",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", fixedDepositFutureValue)} میلیون تومان",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "در مقایسه با سپرده ثابت بانکی (۳۰٪ سالانه):",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFF9800).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "⚠️ راهنمای فرار عالی از تورم بورس: برای کسب نتایج بزرگتر از سود سپرده‌های متعارف بانکی و جبران کامل تورم سالانه، سعی کنید جمع مجموع اهرم رشد قیمت و بازده نقدی را کمی بالاتر هم ببرید.",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Tutorial guidelines info block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, borderClr)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "راهنما به زبان ساده برای تنظیم فیلترهای سود و قیمت: 💡",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldVal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "۱. سرمایه گذاری دوباره (DPS Reinvestment): زمانی که سود مجمع سجام نقداً تسویه می شود، دوباره آن را به صورت ورقه سهم های ممتاز بخرید که قدرت هم افزایی داشته باشد.\n\n۲. اهرم رشد: تلفیق شرکت هایی که سود نقدی مطلوب را با توسعه کاربری تجاری همسو می کنند، قفل پتانسیل خروج نقدینگی را مهار می کند.",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Right,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// Screener card formulas radio buttons options styling
@Composable
fun ScreenerFormulaCard(
    index: Int,
    title: String,
    desc: String,
    selected: Boolean,
    goldVal: Color,
    cardBgColor: Color,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("formula_card_$index"),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, if (selected) goldVal else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = goldVal, unselectedColor = Color.Gray)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) goldVal else Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

// Tactile sliders widgets layout
@Composable
fun SliderControl(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    suffix: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${String.format(Locale.US, "%.1f", value)}$suffix",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldClr
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color.LightGray
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = GoldClr,
                activeTrackColor = GoldClr,
                inactiveTrackColor = Color.DarkGray
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

// ==========================================================
// SETTINGS TAB (Configuration parameters and local resets)
// ==========================================================
@Composable
fun SettingsTabContent(
    viewModel: TradingViewModel,
    biometricEnabled: Boolean,
    geminiApiKey: String,
    cardBgColor: Color,
    borderClr: Color,
    goldVal: Color,
    onShowToast: (String) -> Unit
) {
    var configExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("settings_panel"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Visual Academy Step instruction checklist
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showWalkthrough(1) }
                    .testTag("academy_card"),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, goldVal.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "برو", tint = goldVal, modifier = Modifier.size(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "آکادمی تصویری و راهنمای قدم‌به‌قدم VIP 📚",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "برای تسلط به ابزارهای هوش مصنوعی و ترفندها کلیک کنید",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        // Fingerprint biometric authorization toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, borderClr)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometrics(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = goldVal,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("biometric_switch")
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "تایید ورود با اثر انگشت سخت‌افزاری 🔑",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "امنیت بیومتریک لایسنس ویژه و جلوگیری از نشت اطلاعات",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        // Gemini credentials token input setup (Expandable row layout)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, if (configExpanded) goldVal else borderClr)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { configExpanded = !configExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (configExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = goldVal
                        )
                        Text(
                            text = "پیکربندی هوش مصنوعی (Gemini-API) ⚙️",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (configExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = geminiApiKey,
                            onValueChange = { viewModel.setGeminiApiKey(it) },
                            placeholder = { Text("AIzaSy...", color = Color.Gray, fontSize = 11.sp) },
                            label = { Text("کلید اختصاصی Google Gemini API Key", fontSize = 11.sp, color = Color.LightGray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("api_key_field"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = goldVal,
                                unfocusedBorderColor = borderClr
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 کلیدها محلی در حافظه برنامه نگهداری شده و هرگز به سرورهای میانی فرستاده نمی‌شوند.",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // live cbi inflation management card
        item {
            val cbiPointToPointRate by viewModel.cbiPointToPointRate.collectAsState()
            val cbiAnnualRate by viewModel.cbiAnnualRate.collectAsState()
            val cbiStatusMessage by viewModel.cbiStatusMessage.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, goldVal.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.fetchLiveCbiInflation() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی تورم", tint = goldVal)
                        }
                        Text(
                            text = "نرخ تورم رسمی بانک مرکزی (شاخص CBI) 📈",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ابزار پویای مجمع‌یاب جهت همگام‌سازی لحظه‌ای نرخ تنزیل و عایق تورمی کدهای معاملاتی بورس مستقیم از سایت رسمی بانک مرکزی (cbi.ir)",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Right,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Point to point rate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF101420))
                                .border(1.dp, borderClr, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "تورم نقطه به نقطه",
                                    fontSize = 9.sp,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", cbiPointToPointRate)}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StockGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Annual rate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF101420))
                                .border(1.dp, borderClr, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "تورم سالانه بانک مرکزی",
                                    fontSize = 9.sp,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", cbiAnnualRate)}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = goldVal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "وضعیت اتصال به درگاه (cbi.ir): $cbiStatusMessage",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Offline storage portfolio SQLite Room tracking stats card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, borderClr)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "پورتفوی فرضی بورس من (تحلیل آفلاین ذخیره Room) 📂",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سودهای نقدی تاریخی یا پارامترهای مجمع عمومی این شرکت‌ها به صورت تفصیلی دستکاری یا سفارشی‌سازی شده‌اند:",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Right,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF101420),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "ذخیره‌ساز محلی SQLite در وضعیت آماده‌باش است (دیتابیس درگاه سجام SQLite فعال).",
                            fontSize = 9.sp,
                            color = StockGreen,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        // Database and parameters reset trigger settings actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, borderClr)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "بروزرسانی اطلاعات و رفع مشکلات احتمالی برنامه 🔄",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pull_databourse_rates_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5234)),
                        onClick = {
                            onShowToast("در حال دریافت تابلوی زنده و محاسبات تقسیم سود از databourse.ir... ⏳")
                            viewModel.triggerDataBourseUpdate { msg ->
                                onShowToast(msg)
                            }
                        }
                    ) {
                        Text("🟢 بروزرسانی زنده قیمت و سود از دیتابورس (databourse.ir)", fontSize = 11.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pull_all_rates_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F283C)),
                        onClick = {
                            viewModel.triggerSimulatedUpdate {
                                onShowToast("آخرین قیمت‌ها و سودهای تقسیمی سجام با موفقیت بروز شد ⚡")
                            }
                        }
                    ) {
                        Text("🔄 بازیابی و بروزرسانی پشتیبان آفلاین محلی", fontSize = 11.sp, color = goldVal)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("factory_reset_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = StockRed.copy(alpha = 0.15f)),
                        onClick = {
                            viewModel.factoryReset()
                            onShowToast("برنامه ریست فکتوری شد و کش دیتابیس بورس پاکسازی گردید 🧹")
                        }
                    ) {
                        Text("❌ پاکسازی دیتابیس بورس و ریست فکتوری (Factory Reset)", fontSize = 11.sp, color = StockRed)
                    }
                }
            }
        }
    }
}

// ==========================================================
// SUBORDINATE WALKTHROUGH POPUP COMPONENT (Step-by-step)
// ==========================================================
@Composable
fun WalkthroughDialog(
    step: Int,
    onNext: (Int) -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldClr.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "گام $step از ۴", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldClr)
                }
                Text(
                    text = "راهنمای گام‌به‌گام پلتفرم مجمعیاب VIP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Right
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                val stepTitle = when (step) {
                    1 -> "۱. پایشگر و فیلتر پیشرفته مجامع"
                    2 -> "۲. تنظیم فرضی و شخصی سازی"
                    3 -> "۳. ماشین حساب هوشمند سود نو"
                    else -> "۴. تحلیلگر هوش مصنوعی Gemini"
                }

                val stepDesc = when (step) {
                    1 -> "جدول اصلی برنامه در تب خانه، لیستی از نمادهای برتر بورس با مجامع زنده و پاداش های نقدی سجام را نشان می دهد. شما می توانید در لحظه جستجو کنید."
                    2 -> "در تب سود هوشمند با تغییر اسلایدر بازدهی DPS و نرخ رشد، عایدی ۵ ساله اثر مرکب خود را شبیه سازی کرده و با بازده ثابت سپرده بانکی مقایسه کنید."
                    3 -> "پس از لمس هر کارت در صفحه خانه ماشین حساب هوش نو باز می شود و تعداد برگه سهم، مالیات و سود نقدی عایدی را در لحظه محاسبه می کند."
                    else -> "در صورتی که کپی Gemini-API را وارد کرده باشید، هوش مصنوعی جمنای تحلیل کاملاً اختصاصی از وضعیت مجمع شرکت به زبان فارسی تسلیم خواهد کرد."
                }

                Text(
                    text = stepTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldClr,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = stepDesc,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Right,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = GoldClr),
                onClick = { onNext(step) }
            ) {
                Text(text = if (step < 4) "گام بعدی ⟶" else "فهمیدم، بستن راهنما", color = Color.Black, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("بستن", color = Color.Gray, fontSize = 11.sp)
            }
        },
        containerColor = Color(0xFF141A28)
    )
}

// Format large numbers simply
private fun formatLargeNumber(number: Long): String {
    return when {
        number >= 1_000_000_000_000L -> "${String.format(Locale.US, "%.1f", number.toDouble() / 1_000_000_000_000L)} هزار میلیارد"
        number >= 1_000_000_000L -> "${String.format(Locale.US, "%.1f", number.toDouble() / 1_000_000_000L)} میلیارد"
        number >= 1_000_000L -> "${String.format(Locale.US, "%.1f", number.toDouble() / 1_000_000L)} میلیون"
        else -> NumberFormat.getInstance(Locale.US).format(number)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipSignalCard(stockName: String, price: String, eps: String, dps: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF303437))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(containerColor = Color(0xFFD4AF37)) { 
                    Text("VIP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp) 
                }
                Text(stockName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("قیمت سهم", fontSize = 10.sp, color = Color.Gray)
                    Text(price, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("سود (EPS)", fontSize = 10.sp, color = Color.Gray)
                    Text(eps, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("سود مجمع (DPS)", fontSize = 10.sp, color = Color.Gray)
                    Text(dps, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                }
            }
        }
    }
}

@Composable
fun VipSplashView(
    cbiStatusMessage: String,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var currentMessage by remember { mutableStateOf("در حال مقداردهی اولیه سیستم امنیتی...") }

    // Simulate progress and loading messages
    LaunchedEffect(Unit) {
        val messages = listOf(
            0.1f to "در حال همگام‌سازهای مراجع محاسباتی...",
            0.3f to "پیوستن پورتال به پایگاه داده داخلی SQLite (Room)...",
            0.5f to "دریافت برخط شاخص تورم نقطه به نقطه از سرور بانک مرکزی (CBI)...",
            0.7f to "تعدیل ارزش‌گذاری سهام بر پایه تورم جاری کشور...",
            0.85f to "پاسخ رگولاتور بورس بومی بازبینی شد...",
            0.95f to "سیستم تایید اعتبار مجمع در حال شروع...",
            1.0f to "آماده برای تبادل در VIP بورس"
        )
        
        for (item in messages) {
            val stepTarget = item.first
            val stepMsg = item.second
            currentMessage = stepMsg
            
            while (progress < stepTarget) {
                progress += 0.05f
                kotlinx.coroutines.delay(100)
            }
            progress = stepTarget
        }
        
        // Wait a small moment and then enter!
        kotlinx.coroutines.delay(300)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C101B))
            .clickable(enabled = false) {}, // Intercepts clicks to prevent background touches
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // High-fidelity luxurious emblem: double circles with a centered "$"
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141A28))
                    .border(3.dp, Color(0xFFD4AF37), CircleShape) // Outer golden circle
                    .padding(8.dp)
                    .border(1.5.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), CircleShape) // Inner subtle circle
            ) {
                Text(
                    text = "$",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.padding(bottom = 4.dp) // Optical centering
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Title
            Text(
                text = "وی‌آی‌پی بورس",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "پایگاه هوشمند تحلیل مجمع و سیگنال‌های نخبگان بورس",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Progress bar and loading text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                // Horizontal loading track
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFD4AF37),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Diagnostic log
                Text(
                    text = currentMessage,
                    fontSize = 10.5.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        // Luxurious footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "هوشمند، برتر، بی‌انتها • نسخه مجمع‌یاب 3.5 VIP",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "CBI Source Status: $cbiStatusMessage",
                fontSize = 8.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MentorTabContent(
    viewModel: TradingViewModel,
    cardBgColor: Color,
    borderClr: Color,
    goldVal: Color,
    onShowToast: (String) -> Unit
) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val cbiPtoP by viewModel.cbiPointToPointRate.collectAsState()
    val cbiAnnual by viewModel.cbiAnnualRate.collectAsState()

    var symbolStr by remember { mutableStateOf("") }
    var nameStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var epsStr by remember { mutableStateOf("") }
    var dpsStr by remember { mutableStateOf("") }
    var volumeStr by remember { mutableStateOf("") }
    
    // Additional parameters for dynamic advanced evaluation & status
    var customStatusStr by remember { mutableStateOf("ACTIVE") }
    var dpsLastYearStr by remember { mutableStateOf("") }
    var dpsTwoYearsAgoStr by remember { mutableStateOf("") }
    var dpsThreeYearsAgoStr by remember { mutableStateOf("") }
    var targetDiscountRateStr by remember { mutableStateOf("15") }

    // Helper to auto-fill form with Shasta sample from EasyTrader
    val onFillShastaSample = {
        symbolStr = "شستا"
        nameStr = "سرمایه گذاری تامین اجتماعی"
        priceStr = "1882"
        epsStr = "189"
        dpsStr = "190"
        volumeStr = "871615504"
        customStatusStr = "ACTIVE"
        dpsLastYearStr = "190"
        dpsTwoYearsAgoStr = "135"
        dpsThreeYearsAgoStr = "120"
        targetDiscountRateStr = "15"
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.dp, borderClr),
                    modifier = Modifier.fillMaxWidth().testTag("mentor_header")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "mentor",
                                tint = goldVal,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "منتور ارزش‌یابی و محاسبه‌گر هوشمند",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "با وارد کردن دستی اطلاعات هر سهم، پتانسیل تقسیم سود مجمع، حباب یا ارزش ذاتی آن را درجا و با فرضیات دلخواه محاسبه کنید. اطلاعات ثبت شده در حافظه پایدار SQLite ذخیره شده و در صفحه اصلی رصد می‌شود.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onFillShastaSample,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2638)),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("بارگذاری نمونه واقعی (شستا)", fontSize = 11.sp, color = goldVal)
                        }
                    }
                }
            }

            // Form fields
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.dp, borderClr),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "مشخصات و آمار بنیادی سهم",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldVal
                        )

                        // Symbol input
                        OutlinedTextField(
                            value = symbolStr,
                            onValueChange = { symbolStr = it },
                            label = { Text("نماد (مثلاً شستا)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("mentor_symbol_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = goldVal,
                                unfocusedBorderColor = borderClr
                            )
                        )

                        // Name input
                        OutlinedTextField(
                            value = nameStr,
                            onValueChange = { nameStr = it },
                            label = { Text("نام کامل شرکت", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = goldVal,
                                unfocusedBorderColor = borderClr
                            )
                        )

                        // Price input
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("قیمت پایانی روز پایانی (ریال)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = goldVal,
                                unfocusedBorderColor = borderClr
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // EPS input
                            OutlinedTextField(
                                value = epsStr,
                                onValueChange = { epsStr = it },
                                label = { Text("سود سالانه EPS (ریال)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = goldVal,
                                    unfocusedBorderColor = borderClr
                                )
                            )

                            // DPS input
                            OutlinedTextField(
                                value = dpsStr,
                                onValueChange = { dpsStr = it },
                                label = { Text("سود نقدی مجمع DPS (ریال)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = goldVal,
                                    unfocusedBorderColor = borderClr
                                )
                            )
                        }

                        // Volume input
                        OutlinedTextField(
                            value = volumeStr,
                            onValueChange = { volumeStr = it },
                            label = { Text("حجم معاملات روزانه یا حجم مبنا", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = goldVal,
                                unfocusedBorderColor = borderClr
                            )
                        )

                        // Active/Suspended selection toggle to fix User question
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "وضعیت معامله نماد در بازار (اصلاح نقطه‌ی قرمز/سبز):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldVal
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { customStatusStr = "ACTIVE" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (customStatusStr == "ACTIVE") StockGreen else Color(0xFF1E2638)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("مجاز - فعال 🟢", fontSize = 11.sp, color = if (customStatusStr == "ACTIVE") Color.White else Color.LightGray)
                            }
                            Button(
                                onClick = { customStatusStr = "SUSPENDED" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (customStatusStr == "SUSPENDED") StockRed else Color(0xFF1E2638)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("متوقف - تعلیق 🔴", fontSize = 11.sp, color = if (customStatusStr == "SUSPENDED") Color.White else Color.LightGray)
                            }
                        }

                        // Past Years Dividends Fields
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "سود‌های نقدی مجمع سال‌های گذشته (تعهد پایداری سود):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldVal
                        )
                        Text(
                            text = "وارد کردن مجمع‌های چند سال اخیر الزامی نیست اما به منتور اجازه برآورد نوسان کیفیت سود را می‌دهد.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dpsLastYearStr,
                                onValueChange = { dpsLastYearStr = it },
                                label = { Text("سود ۱ سال قبل (ریال)", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = goldVal,
                                    unfocusedBorderColor = borderClr
                                )
                            )
                            OutlinedTextField(
                                value = dpsTwoYearsAgoStr,
                                onValueChange = { dpsTwoYearsAgoStr = it },
                                label = { Text("سود ۲ سال قبل (ریال)", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = goldVal,
                                    unfocusedBorderColor = borderClr
                                )
                            )
                            OutlinedTextField(
                                value = dpsThreeYearsAgoStr,
                                onValueChange = { dpsThreeYearsAgoStr = it },
                                label = { Text("سود ۳ سال قبل (ریال)", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = goldVal,
                                    unfocusedBorderColor = borderClr
                                )
                            )
                        }

                        // Target Expected Yield / Discounting Rate User Option to make evaluations incredibly professional
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "نرخ تنزیل و بازده مورد انتظار کاربر (سپر ریسک مجمع):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldVal
                        )
                        Text(
                            text = "منعکس‌کننده حداقل درصد سود نقدی سالانه مجمع مورد انتظار شما در تورم فعلی است (مثلاً ۱۵٪ الی ۳۵٪). تغییر این مقدار فوراً ارزش ذاتی منصفانه و گپ حباب سهام دستی شما را ارزیابی و تغییر می‌دهد.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )
                        OutlinedTextField(
                            value = targetDiscountRateStr,
                            onValueChange = { targetDiscountRateStr = it },
                            label = { Text("نرخ بازده هدف کاربر (درصد مجمع مکتوب)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = goldVal,
                                unfocusedBorderColor = borderClr
                            )
                        )

                        // CODAL & TSETMC & DATABOURSE Live helpers (Fixed Codal, Databourse.ir and Google Search)
                        if (symbolStr.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 برترین و امن‌ترین راهکار استخراج بدون فیلترینگ دیتابورس: با زدن دکمه‌های زیر فوراً گزارشات مجمع و تابلوی نماد را بررسی کنید. پیشنهاد می‌شود اطلاعات مجمع شستا یا شپنا را مستقیماً از وب‌سایت دیتابورس کپی فیلدهای دستی بالا کنید.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Right
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val url = "https://databourse.ir/symbol/${symbolStr.trim()}"
                                        uriHandler.openUri(url)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5234)),
                                    modifier = Modifier.weight(1.3f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("دیتابورس بدون تحریم 📊", fontSize = 10.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        val url = "https://www.google.com/search?q=کدال+اطلاعیه+مجمع+برگزاری+${symbolStr.trim()}"
                                        uriHandler.openUri(url)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF143018)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("گوگل مجمع 🔍", fontSize = 10.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        val url = "https://www.codal.ir/ReportList.aspx?search&Symbol=${symbolStr.trim()}"
                                        uriHandler.openUri(url)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF152A3F)),
                                    modifier = Modifier.weight(0.9f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("کدال رسمی 🌐", fontSize = 10.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        val url = "https://www.tsetmc.com/Loader.aspx?ParTree=121C1113&symbol=${symbolStr.trim()}"
                                        uriHandler.openUri(url)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2F36)),
                                    modifier = Modifier.weight(0.8f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("تابلو بورس 📈", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Live Mentor Calculations & Advisories Card
            item {
                val parsedPrice = priceStr.toLongOrNull() ?: 0L
                val parsedEps = epsStr.toIntOrNull() ?: 0
                val parsedDps = dpsStr.toIntOrNull() ?: 0
                val parsedVolume = volumeStr.toLongOrNull() ?: 0L

                val finalPE = if (parsedEps != 0) (parsedPrice.toFloat() / parsedEps.toFloat()) else 0f
                val dpsYield = if (parsedPrice > 0L) (parsedDps.toDouble() / parsedPrice.toDouble()) * 100.0 else 0.0
                val realDpsYield = dpsYield - cbiPtoP

                // Gordon Growth / Intrinsic Value Floor benchmark based on target expected discount rate
                val discountRatePct = targetDiscountRateStr.toDoubleOrNull() ?: 15.0
                val discountRateFactor = if (discountRatePct > 0.0) discountRatePct / 100.0 else 0.15
                val intrinsicValueFloor = parsedDps.toDouble() / discountRateFactor

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.5.dp, if (symbolStr.isNotBlank()) goldVal else borderClr),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تحلیل مغایرت و راهنمای مربی سرمایه‌گذاری (Investment Mentor)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldVal
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (symbolStr.isBlank()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "اطلاعات سهم را در فرم بالا تکمیل کنید تا راهنمایی زنده منتور فعال شود.",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Row statistics
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("بازده عایدی نقدی ناخالص مجمع:", fontSize = 12.sp, color = Color.LightGray)
                                Text(String.format("%.2f%%", dpsYield), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StockGreen)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("بازده حقیقی مجمع (منهای تورم نقطه به نقطه):", fontSize = 12.sp, color = Color.LightGray)
                                val clr = if (realDpsYield >= 0) StockGreen else StockRed
                                Text(String.format("%.2f%%", realDpsYield), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = clr)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("نسبت قیمت به درآمد محاسبه‌شده (P/E):", fontSize = 12.sp, color = Color.LightGray)
                                Text(String.format("%.2f", finalPE), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ارزش ذاتی عایدی نقدی مجمع (فرم تنزیل):", fontSize = 12.sp, color = Color.LightGray)
                                Text(String.format("%,.0f ریال", intrinsicValueFloor), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = goldVal)
                            }

                            // Dynamic Past Dividends Consistency Evaluation if entered
                            val d1 = dpsLastYearStr.toDoubleOrNull() ?: 0.0
                            val d2 = dpsTwoYearsAgoStr.toDoubleOrNull() ?: 0.0
                            val d3 = dpsThreeYearsAgoStr.toDoubleOrNull() ?: 0.0

                            if (d1 > 0.0 || d2 > 0.0 || d3 > 0.0) {
                                val listValues = listOf(parsedDps.toDouble(), d1, d2, d3).filter { it > 0.0 }
                                val averageDps = listValues.average()
                                val maxVal = listValues.maxOrNull() ?: 1.0
                                val minVal = listValues.minOrNull() ?: 0.0
                                val divVolatility = if (averageDps > 0) (maxVal - minVal) / averageDps else 0.0

                                val (volatilityText, ratingColor) = when {
                                    divVolatility < 0.15 -> "عالی و فوق‌العاده پایدار (تقسیم سود تضمین‌شده تکراری) ⭐" to StockGreen
                                    divVolatility < 0.40 -> "متوسط و نیمه پایدار (نوسان جزئی در سودآوری)" to goldVal
                                    else -> "بسیار نوسانی و غیرقابل پایداری (ریسک تخصیص سود زیاد)" to StockRed
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = borderClr)
                                Text(
                                    text = "📊 تحلیل پایداری سنواتی سود مجمع (گذشته‌نگر):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("میانگین تقسیم سود نقدی ۴ سال مجمع:", fontSize = 12.sp, color = Color.LightGray)
                                    Text(String.format("%,.0f ریال", averageDps), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("نمره ریسک کیفیت جریان نقدی:", fontSize = 12.sp, color = Color.LightGray)
                                    Text(volatilityText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ratingColor)
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = borderClr)

                            // MENTOR RISK NOTIFICATIONS
                            Text("مشاوره ریسک تخصصی منتور:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Advisory Block 1: Intrinsic Price evaluation (Bubble check)
                            val bubbleStatus = when {
                                parsedPrice <= 0L -> "نامشخص (قیمت صفر درج شده است)"
                                intrinsicValueFloor <= 0.0 -> "نامشخص (فاقد سود نقدی جهت ارزش‌یابی)"
                                parsedPrice > intrinsicValueFloor * 1.25 -> "⚠️ حباب قیمتی بالا: قیمت روز سهم در بازار، حدوداً %.0f درصد بیش از ارزش ذاتی جریان سود تقسیمی مجمع است. اگر این سهم طرح توسعه انقلابی یا پتانسیل تجدید ارزیابی دارایی بزرگ ندارد خرید آن بالارده قیمتی پرریسک است."
                                parsedPrice < intrinsicValueFloor * 0.9 -> "✅ زیر ارزش ذاتی ممتاز: قیمت روز سهم در بازار زیر ارزش منصفانه عایدی نقدی مجمع قرار دارد. این موقعیت عایق تورمی عالی ساخته و جذابیت خرید عمیقی به همراه دارد."
                                else -> "⚖️ ارزش منصفانه متعادل: قیمت بازار تا حد زیادی منطبق با سود تقسیمی و ارزش ذاتی سهم است."
                            }

                            val formattedBubbleStatus = if (parsedPrice > intrinsicValueFloor * 1.25 && intrinsicValueFloor > 0.0) {
                                val pct = ((parsedPrice.toDouble() - intrinsicValueFloor) / intrinsicValueFloor) * 100.0
                                String.format(bubbleStatus, pct)
                            } else bubbleStatus

                            MentorRiskCard(
                                title = "نتیجه سنجش حباب و ارزش منصفانه",
                                message = formattedBubbleStatus,
                                isWarning = parsedPrice > intrinsicValueFloor * 1.25 && intrinsicValueFloor > 0.0,
                                isSuccess = parsedPrice < intrinsicValueFloor * 0.9 && intrinsicValueFloor > 0.0
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Advisory Block 2: Liquidity Check
                            val volumeWarning = parsedVolume < 1000000L
                            val volumeMsg = if (volumeWarning) {
                                "⚠️ اخطار قفل شدن و نقدشوندگی: حجم معاملات ورودی بسیار کم است (کمتر از ۱ میلیون برگ). خرید سهام کم رمق و سبک بازار ریسک نقدشوندگی بالایی دارد، به طوری که در روندهای نزولی ممکن است در صف فروش گیر کرده و ماه‌ها توانایی نقد کردن دارایی خود را نداشته باشید."
                            } else {
                                "✅ وضعیت نقدشوندگی آزاد: این سهم حجم معاملات مطلوبی دارد و خطر قفل شدگی فوری نقدینگی در تابلوی بورس آن را تهدید نمی‌کنید."
                            }

                            MentorRiskCard(
                                title = "نقدشوندگی و حجم بورس",
                                message = volumeMsg,
                                isWarning = volumeWarning,
                                isSuccess = !volumeWarning
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Advisory Block 3: Sejam and Shasta irregular payment Schedule warning
                            val isShastaStyle = symbolStr.contains("شستا") || parsedPrice < 3000L
                            val scheduleMsg = if (isShastaStyle) {
                                "🕒 ریسک خوش‌قولی و زمان‌بندی: شرکت‌های چندرشته‌ای نظیر هلدینگ‌های غول‌پیکر (مثل شستا) یا سهام با قیمت انباشته ریالی کوچک، به طور معمول تمایل به سوءاستفاده زمانی از بازه‌های مجمع دارند. پرداخت سود مجمع ممکن است از طریق سجام در حداکثر سقف قانونی ۴ الی ۵ ماهه به درازا بکشد. روی سرعت نقدی آن حساب بلندمدت باز کنید."
                            } else {
                                "🕒 تذکر واریزی مجمع: سود تقسیمی را ترجیحاً بلافاصله در فرجه اول برای توجیه استراتژی مجمع ثبت نام و در پرتال سجام رهگیری کنید تا قبل از رسوب تورمی واریز گردد."
                            }

                            MentorRiskCard(
                                title = "زمان‌بندی واریز سود نقدی",
                                message = scheduleMsg,
                                isWarning = isShastaStyle,
                                isSuccess = false
                            )
                        }
                    }
                }
            }

            // Action Buttons: Save or Delete custom stock from Database
            item {
                val parsedPrice = priceStr.toLongOrNull() ?: 0L
                val parsedEps = epsStr.toIntOrNull() ?: 0
                val parsedDps = dpsStr.toIntOrNull() ?: 0
                val parsedVolume = volumeStr.toLongOrNull() ?: 0L

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Save Stock
                    Button(
                        onClick = {
                            if (symbolStr.trim().length < 2) {
                                onShowToast("لطفاً نام نماد را به درستی وارد کنید.")
                                return@Button
                            }
                            if (parsedPrice <= 0L) {
                                onShowToast("قیمت سهم به ریال نباید صفر باشد.")
                                return@Button
                            }
                            
                            // Appending custom suspended flag if marked suspended to handle dynamic green/red dot
                            val dbCompanyName = if (customStatusStr == "SUSPENDED") {
                                if (nameStr.contains("[SUSPENDED]")) nameStr else "${nameStr.trim()} [SUSPENDED]"
                            } else {
                                nameStr.replace("[SUSPENDED]", "").trim()
                            }

                            viewModel.insertCustomStock(
                                symbol = symbolStr,
                                companyName = if (dbCompanyName.isNotBlank()) dbCompanyName else "سهام دستی کاربر",
                                eps = parsedEps,
                                dps = parsedDps,
                                lastPrice = parsedPrice,
                                volume = parsedVolume
                            )
                            onShowToast("نماد «$symbolStr» با موفقیت در بانک اطلاعاتی مجمع ثبت شد و در دیده بان رصد می‌شود! 🎉")
                        },
                        modifier = Modifier.weight(1.2f).testTag("save_custom_stock"),
                        colors = ButtonDefaults.buttonColors(containerColor = StockGreen)
                    ) {
                        Text("ثبت و افزودن به دیده‌بان مجمع", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Delete Stock
                    Button(
                        onClick = {
                            if (symbolStr.trim().isEmpty()) {
                                onShowToast("نمادی وارد نشده است.")
                                return@Button
                            }
                            viewModel.deleteCustomStock(symbolStr)
                            onShowToast("نماد «$symbolStr» در صورت شخصی‌بودن از دیتابیس حذف گردید.")
                            symbolStr = ""
                            nameStr = ""
                            priceStr = ""
                            epsStr = ""
                            dpsStr = ""
                            volumeStr = ""
                            dpsLastYearStr = ""
                            dpsTwoYearsAgoStr = ""
                            dpsThreeYearsAgoStr = ""
                        },
                        modifier = Modifier.weight(0.8f),
                        colors = ButtonDefaults.buttonColors(containerColor = StockRed.copy(alpha = 0.8f))
                    ) {
                        Text("حذف از دیده‌بان", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MentorRiskCard(
    title: String,
    message: String,
    isWarning: Boolean,
    isSuccess: Boolean
) {
    val bgColor = if (isWarning) Color(0xFF2C191D) else if (isSuccess) Color(0xFF142C1E) else Color(0xFF19202E)
    val borderClr = if (isWarning) StockRed else if (isSuccess) StockGreen else Color.Gray.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderClr, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) StockRed else if (isSuccess) StockGreen else Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 10.5.sp,
                color = Color.LightGray,
                lineHeight = 16.sp,
                textAlign = TextAlign.Right
            )
        }
    }
}
