package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import com.example.data.StockSignal
import com.example.data.database.DiscrepancyLogEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- PART 4 - Quant Audit Debug Screen ---
 * 
 * Elegant hidden system debugging console triggered by long-pressing the App Logo for 5 seconds.
 * Features:
 * 1. Side-by-side comparison of Raw and Adjusted quantities.
 * 2. Visual verification of compliance thresholds (Green / Yellow / Red badges).
 * 3. Raw SQLite Audit logs display (DiscrepancyLogEntity records).
 * 4. Simulation buttons to force test various discrepancy/adjustment states.
 */
@Composable
fun QuantAuditDebugDialog(
    viewModel: TradingViewModel,
    onDismiss: () -> Unit
) {
    val allStocks by viewModel.allStocks.collectAsState()
    val allLogs by viewModel.allDiscrepancyLogs.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    val scope = rememberCoroutineScope()
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)

    // Palette definition
    val darkBg = Color(0xFF0F1420)
    val lightBg = Color(0xFFFBF9F4)
    val cardBg = if (isDarkMode) Color(0xFF161C2C) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkMode) Color(0xFFEEEEEE) else Color(0xFF1E283C)
    val textSecondary = if (isDarkMode) Color(0xFF90A4AE) else Color(0xFF5E6E7A)
    val goldClr = Color(0xFFECC94B)
    val successClr = Color(0xFF38A169)
    val warningClr = Color(0xFFDD6B20)
    val errorClr = Color(0xFFE53E3E)

    var simulatedDiscrepancySymbol by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkMode) darkBg else lightBg,
            border = BorderStroke(1.5.dp, goldClr.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = textPrimary
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "کنسول کوانت و بازرسی یکپارچه داده (TSE Core)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = goldClr,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Divider(color = textSecondary.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Live Stocks Validation Status Panel
                    item {
                        Text(
                            text = "🔍 بررسی مغایرت زنده (Raw vs. Adjusted) – لایه ۳ و ۴",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldClr,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    items(allStocks) { stock ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(
                                1.dp, 
                                when (stock.auditStatusColor) {
                                    "Red" -> errorClr.copy(alpha = 0.6f)
                                    "Yellow" -> warningClr.copy(alpha = 0.6f)
                                    else -> successClr.copy(alpha = 0.3f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Title Line
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = when (stock.auditStatusColor) {
                                                    "Red" -> errorClr.copy(alpha = 0.12f)
                                                    "Yellow" -> warningClr.copy(alpha = 0.12f)
                                                    else -> successClr.copy(alpha = 0.12f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = when (stock.auditStatusColor) {
                                                "Red" -> "مغایرت بحرانی (بسته شدن کوانت)"
                                                "Yellow" -> "نیاز به بررسی مجمع"
                                                else -> "وضعیت تایید شده"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (stock.auditStatusColor) {
                                                "Red" -> errorClr
                                                "Yellow" -> warningClr
                                                else -> successClr
                                            }
                                        )
                                    }

                                    Text(
                                        text = "${stock.symbol} - ${stock.name}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Grid values
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "قیمت خام: ${stock.rawPrice}", fontSize = 10.sp, color = textSecondary)
                                        Text(text = "سود (EPS) خام: ${stock.rawEps}", fontSize = 10.sp, color = textSecondary)
                                        Text(
                                            text = "پی به ای (P/E) تابلوی TSETMC: ${String.format(Locale.US, "%.2f", stock.pe)}", 
                                            fontSize = 10.sp, 
                                            color = textSecondary
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "قیمت تعدیل‌شده: ${stock.price}", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                                        Text(text = "سود (EPS) تعدیل‌شده: ${stock.eps}", fontSize = 10.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "پی به ای (P/E) محاسباتی سایه: ${String.format(Locale.US, "%.2f", stock.manualPe)}", 
                                            fontSize = 10.sp, 
                                            color = goldClr,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = textSecondary.copy(alpha = 0.08f), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "آخرین همگام‌سازی مراجع کدال: ${format.format(Date(stock.lastCodalSyncTime))}",
                                        fontSize = 8.sp,
                                        color = textSecondary
                                    )

                                    Text(
                                        text = "میزان تفاوت: ${String.format(Locale.US, "%.1f", stock.differencePercent)}٪",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (stock.differencePercent > 5f) errorClr else if (stock.differencePercent > 3f) warningClr else successClr
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                // Detail log msg
                                Text(
                                    text = stock.auditMessage,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (stock.isAuditSuspended) errorClr else textSecondary,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Interactive Simulator Controls
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = goldClr.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, goldClr.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ شبیه‌ساز مهندسی کوانت (ایجاد خرابی و تعلیق داده)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = goldClr,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "برای اثبات پایداری سیستم، می‌توانید به صورت مصنوعی مغایرت قیمت بیش از ۵٪ روی نمادهای بورس اعمال کنید تا متوقف شدن محاسبات (Temporary Quant Suspension) را فوراً در اپلیکیشن تماشا کنید:",
                                    fontSize = 10.sp,
                                    color = textSecondary,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                // Seed a Capital increase report on Fazar with abnormal values to force discrepancy > 10%
                                                viewModel.marketDataRepository.quantDao.insertCorporateAction(
                                                    com.example.data.database.CorporateActionEntity(
                                                        symbol = "فزر",
                                                        date = "2026-06-01",
                                                        type = "Capital Increase",
                                                        percentage = 150f, // huge capital increase
                                                        source = "تجدید ارزیابی دارایی‌ها"
                                                    )
                                                )
                                                viewModel.fetchMarketData()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = errorClr),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "مغایرت شدید فزر 💣", 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                // Reset Fazar actions back to normal parameters
                                                viewModel.marketDataRepository.quantDao.insertCorporateAction(
                                                    com.example.data.database.CorporateActionEntity(
                                                        symbol = "فزر",
                                                        date = "2026-06-01",
                                                        type = "Capital Increase",
                                                        percentage = 80.0f, // standard normal 80%
                                                        source = "سود انباشته"
                                                    )
                                                )
                                                viewModel.fetchMarketData()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = successClr),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "رفع مغایرت و ریست 🟢", 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Room SQLite Audit Logs
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "پاکسازی",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = errorClr,
                                modifier = Modifier
                                    .clickable {
                                        scope.launch {
                                            viewModel.marketDataRepository.quantDao.deleteAllDiscrepancyLogs()
                                        }
                                    }
                                    .padding(4.dp)
                            )

                            Text(
                                text = "🗄️ گزارشات عدم تطابق در دیتابیس محلی لایسنس (SQLite)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldClr
                            )
                        }
                    }

                    if (allLogs.isEmpty()) {
                        item {
                            Text(
                                text = "دیتابیس مراجع بدون موارد هشدار (خطایی ثبت نشده است)",
                                fontSize = 10.sp,
                                color = successClr,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(allLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, textSecondary.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "انحراف: ${String.format(Locale.US, "%.1f", log.differencePercent)}٪",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = errorClr
                                        )
                                        Text(
                                            text = "${log.symbol} (${log.status})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = log.message,
                                        fontSize = 9.sp,
                                        color = textSecondary,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "زمان ذخیره: ${format.format(Date(log.lastUpdated))}",
                                        fontSize = 7.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = goldClr),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("خروج از پنل بازرسی", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}
