package com.example.rahmatmas.ui.admin.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rahmatmas.R
import com.example.rahmatmas.data.datastore.AdminAuthManager

@Composable
fun HomeAdminScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onGoToRecording: () -> Unit,
    onGoToTransactionHistory: () -> Unit,
    onGoToStockManagement: () -> Unit,
    onGoToFinancialReport: () -> Unit,
    onGoToOnlineSale: () -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val adminAuthManager = remember { AdminAuthManager(appContext) }
    val adminUsername by adminAuthManager.getAdminUsername().collectAsState(initial = "")

    val viewModel: HomeAdminViewModel = viewModel(
        factory = HomeAdminViewModelFactory(
            context = appContext,
            adminAuthManager = adminAuthManager
        )
    )
    val goldPriceState by viewModel.goldPriceState.collectAsState()
    val recentActivityState by viewModel.recentActivityState.collectAsState()

    // State untuk dialog logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .height(200.dp)
                    .shadow(4.dp,
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                        clip = false
                    )
                    .background(
                        color = Color(0xFFFF9800),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Selamat datang!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "logout Icon",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(24.dp)
                                .clickable { showLogoutDialog = true },
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = adminUsername.ifEmpty { "Admin" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(
                                color = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(15.dp)
                            ),
                    ) {
                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = modifier
                            ) {
                                Text(
                                    text = "Harga Emas Hari Ini (antam)",
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Harga Jual: ${goldPriceState.sellPrice}",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 8.sp
                                )
                                Text(
                                    text = "Harga Beli: ${goldPriceState.buyPrice}",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }


                            IconButton(
                                onClick = { viewModel.refreshData() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.CenterVertically),

                                ) {
                                if (goldPriceState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh prices",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.CenterHorizontally)
                    .shadow(4.dp,
                        shape = RoundedCornerShape(15.dp),
                        clip = false
                    )
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(15.dp)
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(4.dp,
                                    shape = RoundedCornerShape(15.dp),
                                    clip = false
                                )
                                .background(
                                    color = Color(0xFF2563EB),
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .clickable { onGoToRecording() }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.logo_note),
                                    contentDescription = "pencatatan transaksi Icon",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                        .padding(4.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Pencatatan Transaksi",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(24.dp))

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(4.dp,
                                    shape = RoundedCornerShape(15.dp),
                                    clip = false)
                                .background(
                                    color = Color(0xFF10B981),
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .clickable { onGoToTransactionHistory() },
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.logo_history),
                                    contentDescription = "riwayat transaksi Icon",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                        .padding(4.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Riwayat Transaksi",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(24.dp))

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(4.dp,
                                    shape = RoundedCornerShape(15.dp),
                                    clip = false)
                                .background(
                                    color = Color(0xFF9333EA),
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .clickable { onGoToStockManagement() },
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.logo_inventory),
                                    contentDescription = "stok barang Icon",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .background(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = CircleShape,
                                        )
                                        .padding(4.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Stok Barang",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(4.dp,
                                    shape = RoundedCornerShape(15.dp),
                                    clip = false)
                                .background(
                                    color = Color(0xFFEA580C),
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .clickable { onGoToOnlineSale() },
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "penjualan online Icon",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .background(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                        .padding(4.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Penjualan Online",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(24.dp))

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(4.dp,
                                    shape = RoundedCornerShape(15.dp),
                                    clip = false,)
                                .background(
                                    color = Color(0xFFD4AF37),
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .clickable { onGoToFinancialReport() },
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.logo_chart),
                                    contentDescription = "laporan keuangan Icon",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .background(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                        .padding(4.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Laporan Keuangan",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Terbaru",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(
                    onClick = { viewModel.refreshActivities() },
                    enabled = !recentActivityState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat ulang aktivitas",
                        tint = if (recentActivityState.isLoading) Color.Gray.copy(alpha = 0.6f) else Color(0xFFFF9800)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            RecentActivityCard(
                state = recentActivityState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            recentActivityState.error?.let { errorMessage ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
            }
        }

        // Alert dialog untuk konfirmasi logout
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Konfirmasi Keluar") },
                text = { Text("Apakah Anda yakin ingin keluar dari akun admin?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.logoutAdmin {
                                onLogout()
                            }
                            showLogoutDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Keluar", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
private fun RecentActivityCard(
    state: RecentActivityUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(18.dp), clip = false)
            .background(Color.White, shape = RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            if (state.activities.isEmpty()) {
                Text(
                    text = if (state.isLoading) "Memuat aktivitas..." else "Belum ada aktivitas yang bisa ditampilkan",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                state.activities.forEachIndexed { index, activity ->
                    ActivityRow(activity)
                    if (index < state.activities.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color(0xFFE5E7EB)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(3.dp),
                color = Color(0xFFFF9800),
                trackColor = Color(0xFFFBE8D5)
            )
        }
    }
}

@Composable
private fun ActivityRow(activity: RecentActivityItem) {
    val accentColor = activityAccentColor(activity.type)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ActivityIcon(activity, accentColor)

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = activity.subtitle,
                fontSize = 11.sp,
                color = Color(0xFF6B7280),
                lineHeight = 14.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            activity.highlight?.let {
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            activity.timestamp?.let {
                Text(
                    text = it,
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(top = if (activity.highlight != null) 6.dp else 0.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityIcon(activity: RecentActivityItem, accentColor: Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(accentColor.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (activity.type) {
            ActivityType.TRANSACTION -> Icon(
                painter = painterResource(R.drawable.logo_note),
                contentDescription = activity.title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )

            ActivityType.STOCK -> Icon(
                painter = painterResource(R.drawable.logo_inventory),
                contentDescription = activity.title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )

            ActivityType.ONLINE_ORDER -> Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = activity.title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun activityAccentColor(type: ActivityType): Color {
    return when (type) {
        ActivityType.TRANSACTION -> Color(0xFF2563EB)
        ActivityType.STOCK -> Color(0xFF10B981)
        ActivityType.ONLINE_ORDER -> Color(0xFFEA580C)
    }
}
