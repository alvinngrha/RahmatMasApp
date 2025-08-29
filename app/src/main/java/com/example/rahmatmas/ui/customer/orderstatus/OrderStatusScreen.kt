package com.example.rahmatmas.ui.customer.orderstatus

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rahmatmas.R
import com.example.rahmatmas.data.supabase.db.OrderStatus
import com.example.rahmatmas.data.supabase.db.SupabaseOrderWithItems
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import com.example.rahmatmas.data.supabase.db.toOrderStatus
import coil.compose.rememberAsyncImagePainter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStatusScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: OrderStatusViewModel = viewModel(
        factory = OrderStatusViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-load user orders when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadUserOrders()
    }

    // Show error messages in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pesanan Saya",
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
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshOrders() },
                        enabled = !uiState.isLoading
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // User info card
            if (uiState.userEmail.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Akun Google:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            if (uiState.userName.isNotBlank()) {
                                Text(
                                    text = uiState.userName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF2C3E50)
                                )
                            }
                            Text(
                                text = uiState.userEmail,
                                fontSize = 13.sp,
                                color = Color(0xFF2C3E50)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Orders content
            if (uiState.isLoading && uiState.orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Memuat pesanan...", color = Color.Gray)
                    }
                }
            } else if (!uiState.isLoading && uiState.orders.isEmpty()) {
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
                            text = "Belum ada pesanan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Pesanan Anda akan muncul di sini setelah checkout",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = { viewModel.refreshOrders() }
                        ) {
                            Text("Muat Ulang", color = Color(0xFFFF9800))
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.orders) { order ->
                        val stockDetail = viewModel.getStockDetail(order.items.firstOrNull()?.id_stock ?: "")
                        CustomerOrderCard(
                            order = order,
                            stockDetail = stockDetail,
                            onCancelClick = {
                                viewModel.showCancelDialog(order)
                            }
                        )
                    }
                }
            }
        }

        // Cancel Dialog for customer
        if (uiState.showCancelDialog && uiState.selectedOrderForCancel != null) {
            CustomerCancelOrderDialog(
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
private fun CustomerOrderCard(
    order: SupabaseOrderWithItems,
    stockDetail: SupabaseStock?,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = order.status.toOrderStatus()
    val statusColor = getStatusColor(status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with order number and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
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

            // Product info
            Row(modifier = Modifier.fillMaxWidth()) {
                val item = order.items.firstOrNull()
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item?.nama_stock ?: "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF2C3E50)
                    )

                    if (item != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jumlah: ${item.jumlah_order}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OrderInfoRow("Penerima", order.recipient_name)
                    OrderInfoRow("Alamat", order.address)
                    OrderInfoRow("No. HP", order.phone)
                    OrderInfoRow("Pengiriman", order.shipping_option.replaceFirstChar { it.uppercase() })

                    if (!order.note.isNullOrBlank()) {
                        OrderInfoRow("Catatan", order.note!!)
                    }

                    if (status == OrderStatus.CANCELLED) {
                        Spacer(modifier = Modifier.height(12.dp))
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

            // Progress indicator for non-cancelled orders
            if (status != OrderStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(16.dp))
                OrderProgressIndicator(currentStatus = status)
            }

            // Cancel button for pending orders
            if (status == OrderStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_cancel_24),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Batalkan Pesanan", fontSize = 14.sp)
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
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color(0xFF2C3E50),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun OrderProgressIndicator(
    currentStatus: OrderStatus,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        OrderStatus.PENDING to "Menunggu Konfirmasi",
        OrderStatus.PROCESSING to "Sedang Diproses",
        OrderStatus.SHIPPING to "Sedang Dikirim",
        OrderStatus.COMPLETED to "Selesai"
    )

    val currentStepIndex = steps.indexOfFirst { it.first == currentStatus }

    Column(modifier = modifier) {
        Text(
            text = "Progress Pesanan",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        steps.forEachIndexed { index, (status, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Circle indicator
                val isActive = index <= currentStepIndex
                val circleColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFE0E0E0)

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(circleColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Label
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = if (isActive) Color(0xFF2C3E50) else Color.Gray,
                    fontWeight = if (index == currentStepIndex) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Vertical line (except for last item)
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .width(2.dp)
                        .height(16.dp)
                        .background(
                            if (index < currentStepIndex) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                        )
                )
            }
        }
    }
}

@Composable
private fun CustomerCancelOrderDialog(
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = cancelReason,
                    onValueChange = { cancelReason = it },
                    label = { Text("Alasan pembatalan (opsional)") },
                    placeholder = { Text("Contoh: Berubah pikiran, salah pesan, dll") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(cancelReason.ifBlank { "Dibatalkan oleh pelanggan" })
                },
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