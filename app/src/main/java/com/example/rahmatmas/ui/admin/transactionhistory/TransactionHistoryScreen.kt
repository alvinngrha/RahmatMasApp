package com.example.rahmatmas.ui.admin.transactionhistory

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import com.example.rahmatmas.R
import com.example.rahmatmas.data.local.dao.TransactionEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: TransactionHistoryViewModel = viewModel(
        factory = TransactionHistoryViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Transaksi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Network status indicator
                        Icon(
                            painter = if (uiState.isOnline) painterResource(R.drawable.cloud_on) else painterResource(
                                R.drawable.cloud_off
                            ),
                            contentDescription = if (uiState.isOnline) "Online" else "Offline",
                            tint = if (uiState.isOnline) Color.Green else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                actions = {
                    // Sync button
                    if (uiState.unsyncedCount > 0) {
                        Badge(
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.syncTransactions() },
                                enabled = uiState.isOnline && !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.icon_sync),
                                        contentDescription = "Sync",
                                        tint = if (uiState.isOnline) Color.Blue else Color.Gray
                                    )
                                }
                            }
                            Text(text = "${uiState.unsyncedCount}")
                        }
                    }

                    // Export all to PDF
                    IconButton(
                        onClick = { viewModel.exportAllToPdf() },
                        enabled = !uiState.isLoading && transactions.isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_picture_as_pdf_24),
                            contentDescription = "Export All PDF"
                        )
                    }

                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refreshData() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
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
                .background(Color.White)
                .padding(paddingValues)
        ) {

            // Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isOnline) Color(0xFFE8F5E8) else Color(0xFFFFF3CD)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Transaksi: ${transactions.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (uiState.isOnline) "Mode Online" else "Mode Offline",
                                fontSize = 12.sp,
                                color = if (uiState.isOnline) Color.Green else Color.Red
                            )
                        }

                        if (uiState.unsyncedCount > 0) {
                            Chip(
                                onClick = { viewModel.syncTransactions() },
                                label = { Text("${uiState.unsyncedCount} belum sync") },
                                colors = ChipDefaults.chipColors(
                                    contentColor = Color.Red.copy(alpha = 0.2f),
                                )
                            )
                        }
                    }
                }
            }

            // Transaction List
            if (uiState.isLoading && transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_receipt_24),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada transaksi",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onExportClick = { viewModel.exportSingleToPdf(transaction) },
                            onDeleteClick = { viewModel.deleteTransaction(transaction.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    onExportClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
                        text = transaction.namaBarang,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "ID: ${transaction.id.take(8)}...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sync status indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (transaction.isSynced) Color.Green else Color.Red,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (transaction.isSynced) "Sync" else "Pending",
                        fontSize = 10.sp,
                        color = if (transaction.isSynced) Color.Green else Color.Red
                    )
                }
            }
        }
    }


    Spacer(modifier = Modifier.height(8.dp))

    // Transaction details
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Jenis: ${transaction.jenisTransaksi}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "Kadar: ${transaction.kadarEmas}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "Berat: ${transaction.beratEmas}g",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = currencyFormat.format(transaction.totalHarga).replace("Rp", "Rp "),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFFF9800)
            )
            Text(
                text = dateFormat.format(transaction.createdAt),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

// Action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onExportClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF2196F3)
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_picture_as_pdf_24),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "PDF",
                fontSize = 12.sp
            )
        }

        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Red
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Hapus",
                fontSize = 12.sp
            )
        }
    }
}

