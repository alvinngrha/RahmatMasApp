package com.example.rahmatmas.ui.admin.transactionhistory

import android.app.DatePickerDialog
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.rahmatmas.data.local.dao.TransactionEntity
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
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

    // State for editing dialog
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

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
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Network status indicator
                        Surface(
                            shape = CircleShape,
                            color = if (uiState.isOnline) Color.Green else Color.Red,
                            modifier = Modifier.size(12.dp)
                        ) {}
                    }
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
                    // Sync button with badge
                    if (uiState.unsyncedCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "${uiState.unsyncedCount}",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { viewModel.syncTransactions() },
                                enabled = uiState.isOnline && !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.icon_sync),
                                        contentDescription = "Sync",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Selection mode actions
                    if (uiState.selectionMode) {
                        IconButton(
                            onClick = { viewModel.exportSelectedToPdf() },
                            enabled = !uiState.isLoading && uiState.selectedIds.isNotEmpty()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_picture_as_pdf_24),
                                contentDescription = "Export Selected PDF",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = { viewModel.selectAllVisible() },
                            enabled = !uiState.isLoading && transactions.isNotEmpty()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_select_all_24),
                                contentDescription = "Pilih Semua",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Mode Pilih",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.exportAllToPdf() },
                            enabled = !uiState.isLoading && transactions.isNotEmpty()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_picture_as_pdf_24),
                                contentDescription = "Export All PDF",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleSelectionMode() },
                            enabled = !uiState.isLoading && transactions.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Mode Pilih",
                                tint = Color.White
                            )
                        }
                    }

                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refreshData() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
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

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Status & Controls Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Status Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Transaksi",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${transactions.size}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFFFF9800)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (uiState.isOnline) Color(0xFFE8F5E8) else Color(
                                    0xFFFFF3CD
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (uiState.isOnline) Color.Green else Color.Red,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.isOnline) "Online" else "Offline",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (uiState.unsyncedCount > 0) {
                                Button(
                                    onClick = { viewModel.syncTransactions() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red.copy(alpha = 0.1f),
                                        contentColor = Color.Red
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "${uiState.unsyncedCount} Pending",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date Filter Section
                        Text(
                            text = "Filter Tanggal",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val dateLabelFormat =
                            remember { SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val cal = java.util.Calendar.getInstance().apply {
                                        time = uiState.startDate ?: java.util.Date()
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val picked = java.util.Calendar.getInstance().apply {
                                                set(java.util.Calendar.YEAR, y)
                                                set(java.util.Calendar.MONTH, m)
                                                set(java.util.Calendar.DAY_OF_MONTH, d)
                                            }.time
                                            viewModel.setStartDate(picked)
                                        },
                                        cal.get(java.util.Calendar.YEAR),
                                        cal.get(java.util.Calendar.MONTH),
                                        cal.get(java.util.Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = uiState.startDate?.let { dateLabelFormat.format(it) }
                                        ?: "Dari Tanggal",
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val cal = java.util.Calendar.getInstance().apply {
                                        time = uiState.endDate ?: java.util.Date()
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val picked = java.util.Calendar.getInstance().apply {
                                                set(java.util.Calendar.YEAR, y)
                                                set(java.util.Calendar.MONTH, m)
                                                set(java.util.Calendar.DAY_OF_MONTH, d)
                                            }.time
                                            viewModel.setEndDate(picked)
                                        },
                                        cal.get(java.util.Calendar.YEAR),
                                        cal.get(java.util.Calendar.MONTH),
                                        cal.get(java.util.Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = uiState.endDate?.let { dateLabelFormat.format(it) }
                                        ?: "Sampai Tanggal",
                                    fontSize = 12.sp
                                )
                            }

                            if (uiState.startDate != null || uiState.endDate != null) {
                                OutlinedButton(
                                    onClick = { viewModel.clearDateFilter() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Text(text = "Reset", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pencarian
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                },
                                placeholder = { Text("Cari transaksi...", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Transaction List (part of LazyColumn)
            if (uiState.isLoading && transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Memuat data...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_receipt_24),
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum ada transaksi",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Transaksi yang sudah dibuat akan muncul di sini",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            } else {
                items(items = transactions, key = { it.id }) { transaction ->
                    // Horizontal padding to match header card padding
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        TransactionItem(
                            transaction = transaction,
                            isSelectable = uiState.selectionMode,
                            isSelected = uiState.selectedIds.contains(transaction.id),
                            onSelectedChange = { viewModel.toggleSelect(transaction.id) },
                            onExportClick = { viewModel.exportSingleToPdf(transaction) },
                            onEditClick = { editingTransaction = transaction }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // Edit dialog
    editingTransaction?.let { tx ->
        EditTransactionDialog(
            initial = tx,
            onDismiss = { editingTransaction = null },
            onSave = { updated ->
                viewModel.updateTransaction(updated)
                editingTransaction = null
            }
        )
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    isSelectable: Boolean,
    isSelected: Boolean,
    onSelectedChange: () -> Unit,
    onExportClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                4.dp,
                shape = RoundedCornerShape(16.dp),
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo thumbnail
                Card(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    val hasPhoto = !transaction.photoPath.isNullOrEmpty()
                    if (hasPhoto) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = if (transaction.photoPath!!.startsWith("http"))
                                    transaction.photoPath else File(transaction.photoPath!!)
                            ),
                            contentDescription = "Foto transaksi",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_receipt_24),
                                contentDescription = null,
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transaction.namaBarang,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                text = "ID: ${transaction.id.take(8)}...",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Text(
                                text = currencyFormat.format(transaction.totalHarga)
                                    .replace("Rp", "Rp "),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFFF9800)
                            )
                            Text(
                                text = dateFormat.format(transaction.createdAt),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (isSelectable) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onSelectedChange() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailItem(label = "Jenis", value = transaction.jenisTransaksi)
                    DetailItem(label = "Kadar", value = transaction.kadarEmas)
                    DetailItem(label = "Berat", value = "${transaction.beratEmas} gram")
                    DetailItem(label = "Jumlah", value = "${transaction.jumlahBarang} pcs")
                    DetailItem(
                        label = "ongkos/gram",
                        value = currencyFormat.format(transaction.ongkos).replace("Rp", "Rp ")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sync status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (transaction.isSynced)
                        Color(0xFFDCFCE7) else Color(0xFFFEF2F2)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (transaction.isSynced) Color.Green else Color.Red,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (transaction.isSynced) "Sync" else "Pending",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (transaction.isSynced) Color.Green else Color.Red
                        )
                    }
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onExportClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2563EB)
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_picture_as_pdf_24),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "PDF", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onEditClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF0D9488)
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Edit", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}


@Composable
private fun EditTransactionDialog(
    initial: TransactionEntity,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    var nama by remember { androidx.compose.runtime.mutableStateOf(initial.namaBarang) }
    var jumlah by remember { androidx.compose.runtime.mutableStateOf(initial.jumlahBarang.toString()) }
    var kadar by remember { androidx.compose.runtime.mutableStateOf(initial.kadarEmas) }
    var jenis by remember { androidx.compose.runtime.mutableStateOf(initial.jenisTransaksi) }
    var berat by remember { androidx.compose.runtime.mutableStateOf(initial.beratEmas.toString()) }
    var ongkos by remember { androidx.compose.runtime.mutableStateOf(initial.ongkos.toString()) }
    var hargaDasar by remember { androidx.compose.runtime.mutableStateOf(initial.hargaDasarPerGram.toString()) }

    val computedTotal = run {
        val h = hargaDasar.toDoubleOrNull() ?: initial.hargaDasarPerGram
        val b = berat.toDoubleOrNull() ?: initial.beratEmas
        val j = jumlah.toIntOrNull() ?: initial.jumlahBarang
        h * b * j
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaksi") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Barang") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = jumlah,
                    onValueChange = { jumlah = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = kadar,
                    onValueChange = { kadar = it },
                    label = { Text("Kadar Emas") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = jenis,
                    onValueChange = { jenis = it },
                    label = { Text("Jenis Transaksi") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = berat,
                    onValueChange = { new ->
                        berat = new.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Berat (gram)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ongkos,
                    onValueChange = { ongkos = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Ongkos/gram") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hargaDasar,
                    onValueChange = { hargaDasar = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Harga Dasar/gram") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Total (otomatis): ${currencyFormat.format(computedTotal).replace("Rp", "Rp ")}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = initial.copy(
                    namaBarang = nama.ifBlank { initial.namaBarang },
                    jumlahBarang = jumlah.toIntOrNull() ?: initial.jumlahBarang,
                    kadarEmas = kadar.ifBlank { initial.kadarEmas },
                    jenisTransaksi = jenis.ifBlank { initial.jenisTransaksi },
                    beratEmas = berat.toDoubleOrNull() ?: initial.beratEmas,
                    ongkos = ongkos.toDoubleOrNull() ?: initial.ongkos,
                    hargaDasarPerGram = hargaDasar.toDoubleOrNull() ?: initial.hargaDasarPerGram,
                    totalHarga = computedTotal
                )
                onSave(updated)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$label: $value",
        fontSize = 12.sp,
        color = Color(0xFF6B7280),
        modifier = modifier.padding(vertical = 1.dp)
    )
}
