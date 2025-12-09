package com.example.rahmatmas.ui.admin.financialreport

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rahmatmas.R
import java.text.NumberFormat
import java.util.Locale

// Enhanced Color Palette
private val PrimaryGreen = Color(0xFF00C896)
private val PrimaryBlue = Color(0xFF4F46E5)
private val AccentOrange = Color(0xFFFF9500)
private val BackgroundGray = Color(0xFFF8FAFC)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1E293B)
private val TextSecondary = Color(0xFF64748B)
private val BorderLight = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportAdminScreen(
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: FinancialReportAdminViewModel = viewModel(
        factory = FinancialReportAdminViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        Log.i("FinanceReportScreen", "Laporan keuangan berhasil dibuka")
    }

    Scaffold(
        topBar = {
            EnhancedTopAppBar(
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BackgroundGray,
                            Color(0xFFF1F5F9)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    // Enhanced Filter Chips
                    EnhancedFilterChips(
                        selectedRange = uiState.selectedRange,
                        onRangeSelected = { viewModel.setRange(it) }
                    )
                }

                item {
                    // Enhanced Summary Card with Animation
                    EnhancedSummaryCard(
                        totalJual = uiState.totalJual,
                        totalBeli = uiState.totalBeli,
                        isLoading = uiState.isLoading
                    )
                }

                item {
                    // Statistics Cards Row
                    StatisticsCardsRow(
                        totalJual = uiState.totalJual,
                        totalBeli = uiState.totalBeli
                    )
                }

                item {
                    Text(
                        text = "Rincian Harian",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(uiState.summaries) { item ->
                    EnhancedDailyReportCard(
                        summary = item,
                        onFormatDate = { viewModel.formatDate(it) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTopAppBar(
    onBackClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "Laporan Keuangan",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Brush.horizontalGradient(
                colors = listOf(AccentOrange, Color(0xFFFF6B35))
            ).let { Color(AccentOrange.toArgb()) }
        )
    )
}

@Composable
private fun EnhancedFilterChips(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit
) {
    val chipData = listOf(
        DateRange.ALL to "Semua",
        DateRange.TODAY to "Hari Ini",
        DateRange.LAST_7_DAYS to "7 Hari",
        DateRange.LAST_30_DAYS to "30 Hari"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chipData.forEach { (range, label) ->
            EnhancedDateChip(
                label = label,
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) }
            )
        }
    }
}

@Composable
private fun EnhancedDateChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                label,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) AccentOrange.copy(alpha = 0.15f) else CardBackground,
            labelColor = if (selected) AccentOrange else TextSecondary
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, AccentOrange.copy(alpha = 0.5f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        }
    )
}

@Composable
private fun EnhancedSummaryCard(
    totalJual: Double,
    totalBeli: Double,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            CardBackground,
                            Color(0xFFFAFBFC)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ringkasan Keuangan",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    if (isLoading) {
                        LoadingIndicator()
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Enhanced Pie Chart
                    Box(modifier = Modifier.size(180.dp)) {
                        EnhancedPieChart(
                            modifier = Modifier.matchParentSize(),
                            jual = totalJual,
                            beli = totalBeli,
                            strokeWidth = 32.dp
                        )

                        // Center content with gradient background
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Total",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    formatCurrency(totalJual + totalBeli),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Enhanced Legend

                    EnhancedLegendItem(
                        color = PrimaryGreen,
                        label = "Penjualan",
                        amount = totalJual,
                        percent = percentage(totalJual, totalBeli),
                        icon = painterResource(R.drawable.baseline_trending_up_24)
                    )

                    EnhancedLegendItem(
                        color = Color.Red,
                        label = "Pembelian",
                        amount = totalBeli,
                        percent = percentage(totalBeli, totalJual),
                        icon = painterResource(R.drawable.baseline_trending_down_24)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsCardsRow(
    totalJual: Double,
    totalBeli: Double
) {
    val profit = totalJual - totalBeli

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Pendapatan",
            value = formatCurrency(profit),
            color = if (profit >= 0) PrimaryGreen else Color(0xFFEF4444),
            icon = if (profit >= 0) painterResource(R.drawable.baseline_trending_up_24) else painterResource(
                R.drawable.baseline_trending_down_24
            )
        )

        StatCard(
            modifier = Modifier.weight(1f),
            title = "Total Transaksi",
            value = formatCurrency(totalJual + totalBeli),
            color = AccentOrange,
            icon = null
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color,
    icon: Painter?
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.05f),
                            CardBackground
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icon?.let {
                        Icon(
                            painter = it,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        title,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    value,
                    color = color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EnhancedDailyReportCard(
    summary: DailySummary,
    onFormatDate: (java.util.Date) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CardBackground,
                            Color(0xFFFDFDFD)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        onFormatDate(summary.date),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentOrange.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            formatCurrency(summary.totalJual + summary.totalBeli),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EnhancedSummaryRow(
                        color = PrimaryGreen,
                        label = "Penjualan",
                        value = summary.totalJual,
                        icon = painterResource(R.drawable.baseline_trending_up_24)
                    )

                    EnhancedSummaryRow(
                        color = Color.Red,
                        label = "Pembelian",
                        value = summary.totalBeli,
                        icon = painterResource(R.drawable.baseline_trending_down_24)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedSummaryRow(
    color: Color,
    label: String,
    value: Double,
    icon: Painter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                label,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }

        Text(
            formatCurrency(value),
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun EnhancedLegendItem(
    color: Color,
    label: String,
    amount: Double,
    percent: String,
    icon: Painter
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    label,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
            Text(
                percent,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            formatCurrency(amount),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun EnhancedPieChart(
    modifier: Modifier = Modifier,
    jual: Double,
    beli: Double,
    strokeWidth: Dp = 32.dp
) {
    val total = (jual + beli).coerceAtLeast(0.0)
    val jualSweep = if (total == 0.0) 0f else (jual / total * 360f).toFloat()
    val beliSweep = if (total == 0.0) 0f else (beli / total * 360f).toFloat()

    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val inset = strokeWidth.toPx() / 2

        withTransform({}) {
            // Background track with gradient effect
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(
                    diameter - strokeWidth.toPx(),
                    diameter - strokeWidth.toPx()
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Sales segment with shadow effect
            drawArc(
                color = PrimaryGreen,
                startAngle = -90f,
                sweepAngle = jualSweep,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(
                    diameter - strokeWidth.toPx(),
                    diameter - strokeWidth.toPx()
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Purchase segment
            drawArc(
                color = Color.Red,
                startAngle = -90f + jualSweep,
                sweepAngle = beliSweep,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(
                    diameter - strokeWidth.toPx(),
                    diameter - strokeWidth.toPx()
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(AccentOrange.copy(alpha = alpha))
    )
}

private fun percentage(part: Double, otherPart: Double): String {
    val total = (part + otherPart)
    if (total <= 0.0) return "0%"
    val pct = part / total * 100.0
    return "${String.format(Locale.getDefault(), "%.0f", pct)}%"
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace("Rp", "Rp ")
}
