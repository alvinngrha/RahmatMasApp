package com.example.rahmatmas.ui.admin.transactionrecording

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.rahmatmas.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionRecordingScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: TransactionRecordingViewModel = viewModel(
        factory = TransactionRecordingViewModelFactory(context)
    )
    val transactionUiState by viewModel.transactionUiState.collectAsState()

    val optionsMenuKadar = listOf(
        "700",
        "833",
        "999"
    )

    val optionsJenisTransaksi = listOf(
        "Jual",
        "Beli"
    )
    // State untuk menyimpan URI foto kamera
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages
    LaunchedEffect(transactionUiState.snackbarMessage) {
        transactionUiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }


    // Launcher untuk mengambil foto dari galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setPhotoUri(uri)
        }
    }

    // Launcher untuk mengambil foto dari kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            viewModel.setPhotoUri(cameraImageUri.value)
        }
    }

    // Permission launcher (untuk kamera)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Mulai kamera jika permission diberikan
            val photoFile = File.createTempFile(
                "IMG_",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                photoFile
            )
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        }
    }

    // Handler klik galeri
    fun handleOpenGallery() {
        galleryLauncher.launch("image/*")
    }

    // Handler klik kamera
    fun handleOpenCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            val photoFile = File.createTempFile(
                "IMG_",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                photoFile
            )
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
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
                            text = "Pencatatan Transaksi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Network status indicator
                        Icon(
                            painter = if (transactionUiState.isOnline) painterResource(R.drawable.cloud_on) else painterResource(R.drawable.cloud_off),
                            contentDescription = if (transactionUiState.isOnline) "Online" else "Offline",
                            tint = if (transactionUiState.isOnline) Color.Green else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                actions = {
                    // Sync button
                    if (transactionUiState.unsyncedCount > 0) {
                        Badge(
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.syncTransactions() },
                                enabled = transactionUiState.isOnline && !transactionUiState.isLoading
                            ) {
                                if (transactionUiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.icon_sync),
                                        contentDescription = "Sync",
                                        tint = if (transactionUiState.isOnline) Color.Blue else Color.Gray
                                    )
                                }
                            }
                            Text(text = "${transactionUiState.unsyncedCount}")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        }
    ) { innerPadding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            Column(
                modifier = modifier
                    .padding(start = 16.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                // Status indicator card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (transactionUiState.isOnline) Color(0xFFE8F5E8) else Color(0xFFFFF3CD)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = if (transactionUiState.isOnline) painterResource(R.drawable.cloud_on) else painterResource(R.drawable.cloud_off),
                            contentDescription = null,
                            tint = if (transactionUiState.isOnline) Color.Green else Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (transactionUiState.isOnline) {
                                "Mode Online - Data akan langsung disinkronkan"
                            } else {
                                "Mode Offline - Data akan disinkronkan saat online (${transactionUiState.unsyncedCount} belum tersinkron)"
                            },
                            fontSize = 12.sp,
                            color = if (transactionUiState.isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }

                // Foto
                if (transactionUiState.photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(transactionUiState.photoUri),
                        contentDescription = "Selected Photo",
                        modifier = modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .border(
                                width = 1.dp,
                                color = Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_insert_photo),
                        contentDescription = "Insert Photo",
                        modifier = modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .border(
                                width = 1.dp,
                                color = Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = modifier.height(16.dp))

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { handleOpenGallery() }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_insert_photo),
                            contentDescription = "Gallery Icon",
                            modifier = modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = modifier.width(16.dp))

                    IconButton(
                        onClick = { handleOpenCamera() }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_camera),
                            contentDescription = "Camera Icon",
                            modifier = modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = modifier.height(24.dp))

                // Form fields (sama seperti sebelumnya)
                Text(
                    text = "ID Transaksi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.idTransaksi,
                    readOnly = true,
                    onValueChange = {},
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                    modifier = modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = modifier.height(16.dp))

                // Nama Barang
                Text(
                    text = "Nama Barang",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.namaBarang,
                    onValueChange = { viewModel.updateNamaBarang(it) },
                    placeholder = { Text(text = "Masukkan Nama Barang", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Spacer(modifier = modifier.height(16.dp))

                // Jumlah Barang
                Text(
                    text = "Jumlah Barang",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.jumlahBarang,
                    onValueChange = { viewModel.updateJumlahBarang(it) },
                    placeholder = { Text(text = "Masukkan Jumlah Barang", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                if (transactionUiState.jumlahBarangError != null) {
                    Text(
                        text = transactionUiState.jumlahBarangError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = modifier.height(16.dp))

                // Kadar Emas
                Text(
                    text = "Kadar Emas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                ExposedDropdownMenuBox(
                    expanded = transactionUiState.isKadarEmasExpanded,
                    onExpandedChange = { newState -> viewModel.updateKadarEmasExpanded(newState) },
                    modifier = modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                ) {
                    TextField(
                        readOnly = true,
                        value = transactionUiState.kadarEmas,
                        onValueChange = {},
                        placeholder = { Text("Pilih Kadar Emas", fontSize = 12.sp) },
                        modifier = modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = transactionUiState.isKadarEmasExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = transactionUiState.isKadarEmasExpanded,
                        onDismissRequest = { viewModel.updateKadarEmasExpanded(false) },
                        modifier = modifier.background(Color.White)
                    ) {
                        optionsMenuKadar.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateKadarEmas(option)
                                    viewModel.updateKadarEmasExpanded(false)
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = modifier.height(16.dp))

                // Jenis Transaksi
                Text(
                    text = "Jenis Transaksi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                ExposedDropdownMenuBox(
                    expanded = transactionUiState.isJenisTransaksiExpanded,
                    onExpandedChange = { newState -> viewModel.updateJenisTransaksiExpanded(newState) },
                    modifier = modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                ) {
                    TextField(
                        readOnly = true,
                        value = transactionUiState.jenisTransaksi,
                        onValueChange = {},
                        placeholder = { Text("Pilih Jenis Transaksi", fontSize = 12.sp) },
                        modifier = modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = transactionUiState.isJenisTransaksiExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = transactionUiState.isJenisTransaksiExpanded,
                        onDismissRequest = { viewModel.updateJenisTransaksiExpanded(false) },
                        modifier = modifier.background(Color.White)
                    ) {
                        optionsJenisTransaksi.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateJenisTransaksi(option)
                                    viewModel.updateJenisTransaksiExpanded(false)
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = modifier.height(16.dp))

                // Berat Emas
                Text(
                    text = "Berat Emas (Gram)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.beratEmas,
                    onValueChange = { viewModel.updateBeratEmas(it) },
                    placeholder = { Text(text = "Masukkan Berat Emas", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                if (transactionUiState.beratError != null) {
                    Text(
                        text = transactionUiState.beratError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = modifier.height(16.dp))

                // Ongkos
                Text(
                    text = "Ongkos per Gram",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.ongkos,
                    onValueChange = { viewModel.updateOngkos(it) },
                    placeholder = { Text(text = "Masukkan Ongkos", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                if (transactionUiState.ongkosError != null) {
                    Text(
                        text = transactionUiState.ongkosError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = modifier.height(16.dp))

                // Harga Dasar Emas per Gram
                Text(
                    text = "Harga Dasar Emas per Gram",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.hargaDasarPerGram,
                    onValueChange = { viewModel.updateHargaDasarPerGram(it) },
                    placeholder = {
                        Text(
                            text = "Masukkan Harga Dasar Emas per Gram",
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    modifier = modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                if (transactionUiState.hargaDasarError != null) {
                    Text(
                        text = transactionUiState.hargaDasarError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = modifier.height(16.dp))

                // Total Harga
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                ) {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = "Total Harga",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = modifier.height(8.dp))

                        Text(
                            text = viewModel.formatCurrency(transactionUiState.totalHarga),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                    }
                }
                Spacer(modifier = modifier.height(16.dp))

                // Buttons Row
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export PDF Button
                    Button(
                        onClick = { viewModel.exportToPdf() },
                        modifier = modifier
                            .weight(1f)
                            .shadow(24.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3),
                            contentColor = Color.White
                        ),
                        enabled = !transactionUiState.isLoading
                    ) {
                        if (transactionUiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.baseline_picture_as_pdf_24),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PDF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Save Button
                    Button(
                        onClick = { viewModel.simpanTransaksi() },
                        modifier = modifier
                            .weight(2f)
                            .shadow(24.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300),
                            contentColor = Color.White
                        ),
                        enabled = !transactionUiState.isSaving
                    ) {
                        if (transactionUiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (transactionUiState.isSaving) "Menyimpan..." else "Simpan Transaksi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = modifier.height(16.dp))

                // Offline info card
                if (!transactionUiState.isOnline && transactionUiState.unsyncedCount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3CD)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.cloud_off),
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mode Offline",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${transactionUiState.unsyncedCount} transaksi menunggu sinkronisasi. Data akan otomatis tersinkron saat terhubung internet.",
                                fontSize = 12.sp,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
            }
        }
    }
}
