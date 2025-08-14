package com.example.rahmatmas.ui.costumer.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.rahmatmas.data.supabase.db.SupabaseStock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (SupabaseStock) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: CatalogViewModel = viewModel(
        factory = CatalogViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val stocks by viewModel.stocks.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedKadarFilter by remember { mutableStateOf("") }

    // Trigger initial data load
    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    // Handle search
    LaunchedEffect(searchQuery) {
        viewModel.searchStocks(searchQuery)
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
                            text = "Katalog Barang",
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
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Sort button
                    IconButton(
                        onClick = { showSortMenu = true },
                        enabled = stocks.isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_sort_24),
                            contentDescription = "Sort"
                        )
                    }

                    // Sort dropdown menu
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nama A-Z") },
                            onClick = {
                                viewModel.sortStocks(SortOption.NAME_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Nama Z-A") },
                            onClick = {
                                viewModel.sortStocks(SortOption.NAME_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Kadar Rendah-Tinggi") },
                            onClick = {
                                viewModel.sortStocks(SortOption.KADAR_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Kadar Tinggi-Rendah") },
                            onClick = {
                                viewModel.sortStocks(SortOption.KADAR_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Berat Ringan-Berat") },
                            onClick = {
                                viewModel.sortStocks(SortOption.WEIGHT_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Berat Berat-Ringan") },
                            onClick = {
                                viewModel.sortStocks(SortOption.WEIGHT_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Terbaru") },
                            onClick = {
                                viewModel.sortStocks(SortOption.NEWEST)
                                showSortMenu = false
                            }
                        )
                    }

                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refreshCatalog() },
                        enabled = !uiState.isLoading && uiState.isOnline
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

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari barang...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = uiState.isOnline && stocks.isNotEmpty()
            )

            // Filter by Kadar Emas
            if (stocks.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = {
                                selectedKadarFilter = ""
                                viewModel.filterByKadarEmas("")
                            },
                            label = { Text("Semua") },
                            selected = selectedKadarFilter.isEmpty()
                        )
                    }

                    items(viewModel.getAvailableKadarEmas()) { kadar ->
                        FilterChip(
                            onClick = {
                                selectedKadarFilter = kadar
                                viewModel.filterByKadarEmas(kadar)
                            },
                            label = { Text(kadar) },
                            selected = selectedKadarFilter == kadar
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Status Card
            if (!uiState.isOnline) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cloud_off),
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tidak ada koneksi internet. Katalog tidak dapat dimuat.",
                            fontSize = 14.sp,
                            color = Color(0xFF856404)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Content
            when {
                uiState.isLoading && stocks.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                !uiState.isOnline -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.cloud_off),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Memerlukan koneksi internet",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                stocks.isEmpty() -> {
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
                                text = if (searchQuery.isNotEmpty()) "Tidak ada barang yang ditemukan" else "Belum ada barang tersedia",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        columns = GridCells.Fixed(2),
                        userScrollEnabled = true,
                    ) {
                        items(stocks) { stock ->
                            CatalogItem(
                                stock = stock,
                                onItemClick = { onProductClick(stock) },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogItem(
    stock: SupabaseStock,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                4.dp,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onItemClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (!stock.photo_path.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(stock.photo_path),
                        contentDescription = "Product Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = stock.nama_barang,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Kadar ${stock.kadar_emas}",
                fontSize = 10.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Price info and order button
            val calculatedPrice = viewModel.calculatePrice(stock)

            if (calculatedPrice != null) {
                Text(
                    text = viewModel.formatCurrency(calculatedPrice),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            } else {
                Text(
                    text = "Memuat harga...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}