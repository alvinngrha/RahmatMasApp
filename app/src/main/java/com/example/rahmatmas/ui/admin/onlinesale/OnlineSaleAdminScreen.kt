package com.example.rahmatmas.ui.admin.onlinesale

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun OnlineSaleScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: OnlineSaleAdminViewModel = viewModel(
        factory = OnlineSaleAdminViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    // Load initial data
    LaunchedEffect(uiState.isOnline) {
        if (uiState.isOnline) {
            viewModel.loadOrders()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Penjualan Online",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Network status indicator
                        Icon(
                            painter = if (uiState.isOnline) painterResource(R.drawable.cloud_on)
                            else painterResource(R.drawable.cloud_off),
                            contentDescription = if (uiState.isOnline) "Online" else "Offline",
                            tint = if (uiState.isOnline) Color.Green else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadOrders() },
                        enabled = !uiState.isLoading && uiState.isOnline
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        }
    ) { paddingValues ->

        if (!uiState.isOnline) {
            // Offline state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cloud_off),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tidak ada koneksi internet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Periksa koneksi internet Anda",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(paddingValues)
            ) {
                // Tab Row
                val tabTitles = listOf(
                    "Menunggu" to "${viewModel.getOrdersByStatus(OrderStatus.PENDING).size}",
                    "Diproses" to "${viewModel.getOrdersByStatus(OrderStatus.PROCESSING).size}",
                    "Dikirim" to "${viewModel.getOrdersByStatus(OrderStatus.SHIPPING).size}",
                    "Selesai" to "${viewModel.getOrdersByStatus(OrderStatus.COMPLETED).size}",
                    "Dibatalkan" to "${viewModel.getOrdersByStatus(OrderStatus.CANCELLED).size}"
                )

                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    contentColor = Color(0xFFFF9800),
                    edgePadding = 0.dp
                ) {
                    tabTitles.forEachIndexed { index, (title, count) ->
                        Tab(
                            selected = uiState.selectedTabIndex == index,
                            onClick = { viewModel.selectTab(index) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                                if (count.toInt() > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge(
                                        containerColor = if (uiState.selectedTabIndex == index)
                                            Color(0xFFFF9800) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Text(
                                            text = count,
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Orders List
                val currentOrders = viewModel.getCurrentTabOrders()

                if (uiState.isLoading && uiState.allOrders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Memuat pesanan...",
                                color = Color.Gray
                            )
                        }
                    }
                } else if (currentOrders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.logo_inventory),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tidak ada pesanan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Text(
                                text = getEmptyStateMessage(uiState.selectedTabIndex),
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentOrders) { order ->
                            OrderCard(
                                order = order,
                                stockDetail = viewModel.getStockDetail(
                                    order.items.firstOrNull()?.id_stock ?: ""
                                ),
                                onStatusUpdate = { newStatus ->
                                    viewModel.updateOrderStatus(order.id, newStatus)
                                },
                                onCancelClick = {
                                    viewModel.showCancelDialog(order)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Cancel Dialog
        if (uiState.showCancelDialog && uiState.selectedOrderForCancel != null) {
            CancelOrderDialog(
                order = uiState.selectedOrderForCancel!!,
                onConfirm = { reason ->
                    viewModel.cancelOrder(uiState.selectedOrderForCancel!!.id, reason)
                },
                onDismiss = { viewModel.hideCancelDialog() }
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: SupabaseOrderWithItems,
    stockDetail: SupabaseStock?,
    onStatusUpdate: (OrderStatus) -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = order.status.toOrderStatus()
    val statusColor = getStatusColor(status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
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

                // Status Badge
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

            Spacer(modifier = Modifier.height(16.dp))

            // Product Info
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Product Image
                if (stockDetail?.photo_path != null) {
                    Card(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(stockDetail.photo_path),
                            contentDescription = "Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
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

                // Product Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    order.items.firstOrNull()?.let { item ->
                        Text(
                            text = order.items.firstOrNull()?.nama_stock ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF2C3E50),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )


                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kadar: ${item.kadar_emas} (${item.kadar_persen})",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Berat: ${item.berat_emas}g",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Jumlah order: ${item.jumlah_order}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing Info
            order.items.firstOrNull()?.let { item ->
                Text(
                    text = "Harga dasar emas saat order: ${formatCurrency(item.harga_emas_hariini)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Total harga: ${formatCurrency(order.items.sumOf { it.total_harga })}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF2C3E50)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Customer Info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Detail Pelanggan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF2C3E50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow("Nama", order.recipient_name)
                    InfoRow("No. HP", order.phone)
                    InfoRow("Alamat", order.address)
                    InfoRow("Pengiriman", order.shipping_option.replaceFirstChar { it.uppercase() })

                    if (!order.note.isNullOrBlank()) {
                        InfoRow("Catatan", order.note!!)
                    }

                    if (status == OrderStatus.CANCELLED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Alasan Pembatalan",
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
                                    Text(
                                        text = "Dibatalkan oleh: ${order.cancelled_by}",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (status) {
                    OrderStatus.PENDING -> {
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_cancel_24),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tolak", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onStatusUpdate(OrderStatus.PROCESSING) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Terima", fontSize = 12.sp)
                        }
                    }

                    OrderStatus.PROCESSING -> {
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_cancel_24),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Batal", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onStatusUpdate(OrderStatus.SHIPPING) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_local_shipping_24),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kirim", fontSize = 12.sp)
                        }
                    }

                    OrderStatus.SHIPPING -> {
                        Button(
                            onClick = { onStatusUpdate(OrderStatus.COMPLETED) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tandai Selesai", fontSize = 12.sp)
                        }
                    }

                    OrderStatus.COMPLETED, OrderStatus.CANCELLED -> {
                        // No actions for completed or cancelled orders
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color(0xFF2C3E50),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.7f),
            textAlign = TextAlign.End
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun CancelOrderDialog(
    order: SupabaseOrderWithItems,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var cancelReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.baseline_cancel_24),
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Batalkan Pesanan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Apakah Anda yakin ingin membatalkan pesanan ini?",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pesanan: ${order.items.firstOrNull()?.nama_stock}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Pelanggan: ${order.recipient_name}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = cancelReason,
                    onValueChange = { cancelReason = it },
                    label = { Text("Alasan pembatalan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(cancelReason) },
                enabled = cancelReason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Text("Batalkan Pesanan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
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

private fun getEmptyStateMessage(tabIndex: Int): String {
    return when (tabIndex) {
        0 -> "Belum ada pesanan yang menunggu konfirmasi"
        1 -> "Tidak ada pesanan yang sedang diproses"
        2 -> "Tidak ada pesanan yang sedang dikirim"
        3 -> "Belum ada pesanan yang selesai"
        4 -> "Tidak ada pesanan yang dibatalkan"
        else -> ""
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "Tanggal tidak diketahui"

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