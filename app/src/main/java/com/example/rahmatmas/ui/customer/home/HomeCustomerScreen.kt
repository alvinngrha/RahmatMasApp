package com.example.rahmatmas.ui.customer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rahmatmas.R
import com.example.rahmatmas.data.repository.GoldPriceRepository

@Composable
fun HomeCustomerScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToOrderStatus: () -> Unit,
    onGoToHistory: () -> Unit = {}
) {
    val viewModel: HomeCustomerViewModel = viewModel(
        factory = HomeCustomerViewModelFactory(
            goldPriceRepository = GoldPriceRepository()
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val goldPriceStateCustomer by viewModel.goldPriceStateCustomer.collectAsState()
    val user = uiState.user

    // State untuk dialog logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE9ECEF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header dengan gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFB74D),
                                Color(0xFFFF9800),
                                Color(0xFFF57C00)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Selamat datang! 👋",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = user?.userMetadata?.get("name")?.toString()
                                ?: user?.email?.substringBefore("@")
                                ?: "User",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Profile picture dengan shadow
                    user?.userMetadata?.get("avatar_url")?.let { avatarUrl ->
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.email?.take(1)?.uppercase() ?: "U"),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }

            // Main content
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Brand name dengan styling menarik
                Text(
                    text = "✨ RAHMATMAS ✨",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF2E3440),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Investasi Emas Terpercaya untuk Masa Depan Cerah",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF5E6572),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Harga Emas Harian Section - hanya tampil jika tidak loading dan tidak ada error
                if (!goldPriceStateCustomer.isLoading && goldPriceStateCustomer.error == null) {
                    GoldPricesSection(goldPriceStateCustomer, viewModel)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Tampilkan loading indicator
                if (goldPriceStateCustomer.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Tampilkan pesan error jika ada
                goldPriceStateCustomer.error?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Gagal memuat harga: tidak ada koneksi internet",
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { viewModel.refreshData() }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_refresh_24),
                                contentDescription = "Refresh",
                                tint = Color(0xFF2E3440),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Menu Section
                Text(
                    text = "Layanan Kami",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E3440),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Menu cards dengan design modern
                MenuCard(
                    title = "Katalog Emas",
                    subtitle = "Jelajahi koleksi emas berkualitas tinggi",
                    icon = painterResource(R.drawable.baseline_shopping_cart_24),
                    color = Color(0xFFFF9800),
                    onClick = onGoToCatalog
                )

                Spacer(modifier = Modifier.height(12.dp))

                MenuCard(
                    title = "Status Pesanan",
                    subtitle = "Pantau progress pesanan kamu",
                    icon = painterResource(R.drawable.baseline_timeline_24),
                    color = Color(0xFF4CAF50),
                    onClick = onGoToOrderStatus
                )

                Spacer(modifier = Modifier.height(12.dp))

                MenuCard(
                    title = "Riwayat Pembelian",
                    subtitle = "Lihat history transaksi kamu",
                    icon = painterResource(R.drawable.logo_history),
                    color = Color(0xFF2196F3),
                    onClick = onGoToHistory
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Logout button dengan design menarik
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE57373)
                    )
                ) {
                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Alert dialog untuk konfirmasi logout
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text(
                        "Konfirmasi Logout",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E3440)
                    )
                },
                text = {
                    Text(
                        "Apakah kamu yakin ingin keluar dari akun?",
                        color = Color(0xFF5E6572)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.logout {
                                onLogout()
                            }
                            showLogoutDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE57373)
                        )
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showLogoutDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF78909C)
                        )
                    ) {
                        Text("Batal")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun GoldPricesSection(goldPrice: GoldPriceUiStateCustomer, viewModel: HomeCustomerViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Harga Emas Hari Ini (Antam)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E3440)
                )

                IconButton(onClick = { viewModel.refreshData() }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_refresh_24),
                        contentDescription = "Refresh",
                        tint = Color(0xFF2E3440),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                GoldPriceItem(title = "Harga Beli", price = goldPrice.buyPrice)
                GoldPriceItem(title = "Harga Jual", price = goldPrice.sellPrice)
            }
        }
    }
}

@Composable
fun GoldPriceItem(title: String, price: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E3440)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = price,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD4AF37) // Warna emas
        )
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: Painter,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}