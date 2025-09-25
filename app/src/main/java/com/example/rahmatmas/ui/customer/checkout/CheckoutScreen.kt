package com.example.rahmatmas.ui.customer.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import com.lottiefiles.dotlottie.core.compose.runtime.DotLottieController
import com.lottiefiles.dotlottie.core.compose.runtime.DotLottiePlayerState
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    stock: SupabaseStock,
    quantity: Int,
    onBackClick: () -> Unit,
    onOrderPlaced: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var shippingOption by remember { mutableStateOf("diantar") }

    val uiState by viewModel.uiState.collectAsState()

    // Auto-fill name from Google account
    LaunchedEffect(uiState.userName) {
        if (uiState.userName.isNotBlank() && name.isBlank()) {
            name = uiState.userName
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Show user info from Google account
            if (uiState.userName.isNotBlank()) {
                OutlinedTextField(
                    value = uiState.userName,
                    onValueChange = { },
                    label = { Text("Akun Google") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    shape = RoundedCornerShape(16.dp),
                    supportingText = { Text("Data dari akun Google: ${uiState.userEmail}") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Penerima") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Masukkan nama penerima") },
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Masukkan alamat pengiriman") },
                minLines = 2,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor HP") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                placeholder = { Text("Contoh: 08123456789") },
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan (opsional)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Catatan tambahan untuk pesanan") },
                minLines = 2,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Opsi Pengiriman")
            Column {
                RowOption(
                    selected = shippingOption == "diantar",
                    label = "Diantar ke Alamat",
                    onClick = { shippingOption = "diantar" }
                )
                RowOption(
                    selected = shippingOption == "ambil",
                    label = "Ambil ke Toko",
                    onClick = { shippingOption = "ambil" }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.placeOrder(
                        stock,
                        quantity,
                        name,
                        address,
                        phone,
                        note.ifBlank { null },
                        shippingOption
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(text = "Buat Pesanan")
                }
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = uiState.errorMessage ?: "", color = Color.Red)
            }
        }
    }

    if (uiState.isSuccess) {
        CheckoutSuccessDialog(
            orderId = uiState.successOrderId,
            onAnimationFinished = {
                viewModel.resetState()
                onOrderPlaced()
            }
        )
    }
}

@Composable
private fun RowOption(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Text(text = label)
    }
}

@Composable
private fun CheckoutSuccessDialog(
    orderId: String?,
    onAnimationFinished: () -> Unit
) {
    val controller = remember { DotLottieController() }
    val playerState by controller.currentState.collectAsState(initial = DotLottiePlayerState.INITIAL)
    var hasFinished by remember { mutableStateOf(false) }

    LaunchedEffect(playerState) {
        if (!hasFinished && (playerState == DotLottiePlayerState.COMPLETED || playerState == DotLottiePlayerState.ERROR)) {
            hasFinished = true
            onAnimationFinished()
        }
    }

    LaunchedEffect(Unit) {
        delay(4000)
        if (!hasFinished) {
            hasFinished = true
            onAnimationFinished()
        }
    }

    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 28.dp)
                    .widthIn(max = 320.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DotLottieAnimation(
                    modifier = Modifier.size(160.dp),
                    source = DotLottieSource.Asset("Checkout_Success.lottie"),
                    autoplay = true,
                    loop = false,
                    controller = controller
                )

                Text(
                    text = "Checkout berhasil!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!orderId.isNullOrBlank()) {
                    Text(
                        text = "Kode pesanan: $orderId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
