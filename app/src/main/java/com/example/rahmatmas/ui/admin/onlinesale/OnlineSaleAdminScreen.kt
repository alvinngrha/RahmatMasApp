package com.example.rahmatmas.ui.admin.onlinesale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineSaleScreen(
    viewModel: OnlineSaleAdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Penjualan Online") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.orders.isEmpty()) {
                Text(
                    text = if (uiState.isLoading) "Memuat..." else "Belum ada order",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.orders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = order.stock_name, color = MaterialTheme.colorScheme.primary)
                                Text(text = "Penerima: ${order.recipient_name}")
                                Text(text = "Status: ${order.status}")
                                Text(text = "Pengiriman: ${order.shipping_option}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (order.status == "menunggu konfirmasi") {
                                        Button(onClick = {
                                            viewModel.updateStatus(order.id, "sedang diproses")
                                        }) { Text("Proses") }
                                    }
                                    if (order.status == "sedang diproses") {
                                        Button(onClick = {
                                            viewModel.updateStatus(order.id, "akan dikirim")
                                        }) { Text("Kirim") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage ?: "", color = Color.Red, modifier = Modifier.padding(16.dp))
            }
        }
    }
}