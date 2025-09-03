package com.example.rahmatmas.ui.admin.financialreport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rahmatmas.R
import java.text.NumberFormat
import java.util.Locale

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Laporan Keuangan", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSortOrder() }) {
                        Icon(painter = painterResource(R.drawable.baseline_sort_24), contentDescription = "Urutkan", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {
            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangeChip(
                    label = "Semua",
                    selected = uiState.selectedRange == DateRange.ALL,
                    onClick = { viewModel.setRange(DateRange.ALL) }
                )
                DateRangeChip(
                    label = "Hari ini",
                    selected = uiState.selectedRange == DateRange.TODAY,
                    onClick = { viewModel.setRange(DateRange.TODAY) }
                )
                DateRangeChip(
                    label = "7 hari",
                    selected = uiState.selectedRange == DateRange.LAST_7_DAYS,
                    onClick = { viewModel.setRange(DateRange.LAST_7_DAYS) }
                )
                DateRangeChip(
                    label = "30 hari",
                    selected = uiState.selectedRange == DateRange.LAST_30_DAYS,
                    onClick = { viewModel.setRange(DateRange.LAST_30_DAYS) }
                )
            }

            // Summary + Pie chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ringkasan", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(160.dp)) {
                            PieChart(
                                modifier = Modifier.matchParentSize(),
                                jual = uiState.totalJual,
                                beli = uiState.totalBeli,
                                strokeWidth = 28.dp
                            )
                            val total = uiState.totalJual + uiState.totalBeli
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total", color = Color(0xFF6B7280))
                                Text(
                                    formatCurrency(total),
                                    color = Color(0xFF111827),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(Modifier.size(16.dp))
                        Column(Modifier.weight(1f)) {
                            LegendRow(
                                color = Color(0xFF10B981),
                                label = "Penjualan (Jual)",
                                amount = uiState.totalJual,
                                percent = percentage(uiState.totalJual, uiState.totalBeli)
                            )
                            Spacer(Modifier.height(10.dp))
                            LegendRow(
                                color = Color(0xFF3B82F6),
                                label = "Pembelian (Beli)",
                                amount = uiState.totalBeli,
                                percent = percentage(uiState.totalBeli, uiState.totalJual)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Rincian Per Tanggal",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            // Daily list sorted by date
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.summaries) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(viewModel.formatDate(item.date), fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Spacer(Modifier.height(8.dp))
                            SummaryRow(color = Color(0xFF10B981), label = "Penjualan", value = item.totalJual)
                            Spacer(Modifier.height(6.dp))
                            SummaryRow(color = Color(0xFF3B82F6), label = "Pembelian", value = item.totalBeli)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(color: Color, label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.size(8.dp))
            Text(label, color = Color(0xFF111827))
        }
        Text(formatCurrency(value), fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
    }
}

@Composable
private fun DateRangeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color(0xFFFFE0B2) else Color(0xFFF3F4F6),
            labelColor = if (selected) Color(0xFFB45309) else Color(0xFF374151)
        ),
        border = null
    )
}

@Composable
private fun PieChart(
    modifier: Modifier = Modifier,
    jual: Double,
    beli: Double,
    strokeWidth: Dp = 24.dp
) {
    val total = (jual + beli).coerceAtLeast(0.0)
    val jualSweep = if (total == 0.0) 0f else (jual / total * 360f).toFloat()
    val beliSweep = if (total == 0.0) 0f else (beli / total * 360f).toFloat()

    val jualColor = Color(0xFF10B981)
    val beliColor = Color(0xFF3B82F6)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val diameter = size.minDimension
            val radius = diameter / 2
            val inset = strokeWidth.toPx() / 2

            withTransform({}) {
                // Background track
                drawArc(
                    color = Color(0xFFE5E7EB),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(diameter - strokeWidth.toPx(), diameter - strokeWidth.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )

                // Jual segment
                drawArc(
                    color = jualColor,
                    startAngle = -90f,
                    sweepAngle = jualSweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(diameter - strokeWidth.toPx(), diameter - strokeWidth.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )

                // Beli segment
                drawArc(
                    color = beliColor,
                    startAngle = -90f + jualSweep,
                    sweepAngle = beliSweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(diameter - strokeWidth.toPx(), diameter - strokeWidth.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, amount: Double, percent: String) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.size(8.dp))
                Text(label, color = Color(0xFF111827), fontWeight = FontWeight.Medium)
            }
            Text(percent, color = Color(0xFF6B7280))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            formatCurrency(amount),
            color = Color(0xFF111827),
            fontWeight = FontWeight.SemiBold
        )
    }
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
