package com.example.rahmatmas.ui.customer.purchasehistory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.rahmatmas.R
import com.example.rahmatmas.data.supabase.db.OrderStatus
import com.example.rahmatmas.data.supabase.db.SupabaseOrderWithItems
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import com.example.rahmatmas.data.supabase.db.toOrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseHistoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: PurchaseHistoryViewModel = viewModel(
        factory = PurchaseHistoryViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Riwayat Pembelian",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.purchases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (!uiState.isLoading && uiState.purchases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.logo_history),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Belum ada riwayat pembelian", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.purchases) { order ->
                    val stock = viewModel.getStockDetail(order.items.firstOrNull()?.id_stock ?: "")
                    PurchaseCard(order, stock)
                }
            }
        }
    }
}

@Composable
private fun PurchaseCard(
    order: SupabaseOrderWithItems,
    stockDetail: SupabaseStock?,
    modifier: Modifier = Modifier
) {
    val item = order.items.firstOrNull()
    val status = order.status.toOrderStatus()
    val statusColor = getStatusColor(status)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order ID + date + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order #${order.id.takeLast(8)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2C3E50)
                    )
                    Text(
                        text = formatDate(order.created_at),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Status badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(getStatusIcon(status)),
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Product info
            Row(modifier = Modifier.fillMaxWidth()) {
                val photoUrl = item?.photo_path ?: stockDetail?.photo_path
                if (photoUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(photoUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8F9FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.logo_inventory),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item?.nama_stock ?: "Produk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF2C3E50),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Jumlah: ${item.jumlah_order}", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "Kadar: ${item.kadar_emas}", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "Berat: ${item.berat_emas}g", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total price
            val totalPrice = order.items.sumOf { it.total_harga }
            Text(
                text = "Total Harga: ${formatCurrency(totalPrice)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Order details
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OrderInfoRow("Penerima", order.recipient_name)
                    OrderInfoRow("Alamat", order.address)
                    OrderInfoRow("No. HP", order.phone)
                    OrderInfoRow(
                        "Pengiriman",
                        order.shipping_option.replaceFirstChar { it.uppercase() }
                    )
                    if (!order.note.isNullOrBlank()) {
                        OrderInfoRow("Catatan", order.note!!)
                    }
                    if (status == OrderStatus.CANCELLED) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pesanan Dibatalkan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = order.cancel_reason ?: "Tidak ada alasan yang diberikan",
                                    fontSize = 12.sp,
                                    color = Color(0xFF424242)
                                )
                                if (!order.cancelled_by.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Dibatalkan oleh: ${if (order.cancelled_by == "admin") "Toko" else "Anda"}",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
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

@Composable
private fun OrderInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color(0xFF2C3E50),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.PENDING -> Color(0xFFFF9800)
        OrderStatus.PROCESSING -> Color(0xFF2196F3)
        OrderStatus.SHIPPING -> Color(0xFF9C27B0)
        OrderStatus.COMPLETED -> Color(0xFF4CAF50)
        OrderStatus.CANCELLED -> Color(0xFFD32F2F)
    }
}

private fun getStatusIcon(status: OrderStatus): Int {
    return when (status) {
        OrderStatus.PENDING -> R.drawable.baseline_schedule_24
        OrderStatus.PROCESSING -> R.drawable.baseline_refresh_24
        OrderStatus.SHIPPING -> R.drawable.baseline_local_shipping_24
        OrderStatus.COMPLETED -> R.drawable.baseline_check_circle_24
        OrderStatus.CANCELLED -> R.drawable.baseline_cancel_24
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace("Rp", "Rp ")
}
