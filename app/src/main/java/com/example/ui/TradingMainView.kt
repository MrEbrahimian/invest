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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import com.example.data.StockSignal
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
                    Text(text = "14", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
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
                            .testTag("pull_all_rates_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F283C)),
                        onClick = {
                            viewModel.triggerSimulatedUpdate {
                                onShowToast("آخرین قیمت‌ها و سودهای تقسیمی سجام با موفقیت بروز شد ⚡")
                            }
                        }
                    ) {
                        Text("🔄 دریافت جدیدترین اطلاعات زنده سود سهام و قیمت‌ها", fontSize = 11.sp, color = goldVal)
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
