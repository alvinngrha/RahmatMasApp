package com.example.rahmatmas.ui.admin.stock.stockinput

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun StockInputScreen(
    onBackClick: () -> Unit,
    stockToEdit: com.example.rahmatmas.data.supabase.db.SupabaseStock? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: StockInputViewModel = viewModel(
        factory = StockInputViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Load edit data if provided
    LaunchedEffect(stockToEdit) {
        stockToEdit?.let { viewModel.loadForEdit(it) }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    // State untuk menyimpan URI foto kamera
    var cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    // Photo selection dialog
    var showPhotoDialog by remember { mutableStateOf(false) }

    // Gallery and camera launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateSelectedPhoto(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.updateSelectedPhoto(cameraImageUri.value)
        } else {
            // Handle camera failure if needed
            Toast.makeText(context, "Camera error", Toast.LENGTH_SHORT).show()
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
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState.isEdit) "Edit Stok" else "Input Stok") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFF9800),
                    titleContentColor = Color.Black
                ),
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable { onBackClick() }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photo Upload Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Foto Barang",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(
                                    2.dp,
                                    Color.Gray.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showPhotoDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.selectedPhotoUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(uiState.selectedPhotoUri),
                                    contentDescription = "Selected Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (uiState.existingPhotoUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(uiState.existingPhotoUrl),
                                    contentDescription = "Existing Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_insert_photo),
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap untuk memilih foto",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Input Fields
                OutlinedTextField(
                    value = uiState.namaBarang,
                    onValueChange = viewModel::updateNamaBarang,
                    label = { Text("Nama Barang") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.validationErrors.containsKey("namaBarang"),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        uiState.validationErrors["namaBarang"]?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                    )
                )

                OutlinedTextField(
                    value = uiState.jumlahStok,
                    onValueChange = viewModel::updateJumlahStok,
                    label = { Text("Jumlah Stok") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.validationErrors.containsKey("jumlahStok"),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        uiState.validationErrors["jumlahStok"]?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                    )
                )

                // Kadar Emas Dropdown
                var kadarEmasExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = kadarEmasExpanded,
                    onExpandedChange = { kadarEmasExpanded = !kadarEmasExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.kadarEmas,
                        onValueChange = { },
                        label = { Text("Kadar Emas") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { kadarEmasExpanded = !kadarEmasExpanded },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        isError = uiState.validationErrors.containsKey("kadarEmas"),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            uiState.validationErrors["kadarEmas"]?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF9800),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                        )
                    )

                    DropdownMenu(
                        expanded = kadarEmasExpanded,
                        onDismissRequest = { kadarEmasExpanded = false }
                    ) {
                        viewModel.kadarEmasOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateKadarEmas(option)
                                    kadarEmasExpanded = false
                                }
                            )
                        }
                    }
                }

                // Kadar Persen Dropdown
                var kadarPersenExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = kadarPersenExpanded,
                    onExpandedChange = { kadarPersenExpanded = !kadarPersenExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.kadarPersen,
                        onValueChange = { },
                        label = { Text("Kadar Persen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable {
                                if (uiState.kadarEmas.isNotEmpty()) {
                                    kadarPersenExpanded = !kadarPersenExpanded
                                }
                            },
                        readOnly = true,
                        enabled = uiState.kadarEmas.isNotEmpty(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        isError = uiState.validationErrors.containsKey("kadarPersen"),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            uiState.validationErrors["kadarPersen"]?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF9800),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                        )
                    )

                    DropdownMenu(
                        expanded = kadarPersenExpanded,
                        onDismissRequest = { kadarPersenExpanded = false }
                    ) {
                        viewModel.kadarPersenOptions[uiState.kadarEmas]?.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateKadarPersen(option)
                                    kadarPersenExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.beratEmas,
                    onValueChange = viewModel::updateBeratEmas,
                    label = { Text("Berat Emas (gram)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = uiState.validationErrors.containsKey("beratEmas"),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        uiState.validationErrors["beratEmas"]?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                    )
                )

                OutlinedTextField(
                    value = uiState.ongkosPerGram,
                    onValueChange = viewModel::updateOngkos,
                    label = { Text("Ongkos Per Gram") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = uiState.validationErrors.containsKey("ongkos"),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        uiState.validationErrors["ongkos"]?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                    )
                )

                // Save Button
                Button(
                    onClick = viewModel::saveStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && uiState.isOnline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isOnline) Color(0xFFFF9800) else Color.Gray
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menyimpan...")
                    } else {
                        Text(
                            text = if (uiState.isOnline) (if (uiState.isEdit) "Update Stok" else "Simpan Stok") else "Memerlukan Koneksi Internet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!uiState.isOnline) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = "Fitur input stok memerlukan koneksi internet untuk menyimpan data ke server.",
                                fontSize = 14.sp,
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }
            }

            // Photo selection dialog
            if (showPhotoDialog) {
                AlertDialog(
                    onDismissRequest = { showPhotoDialog = false },
                    title = { Text("Pilih Foto Barang") },
                    text = {
                        Column {
                            TextButton(onClick = {
                                handleOpenGallery()
                                showPhotoDialog = false
                            }) {
                                Text("Dari Galeri")
                            }
                            TextButton(onClick = {
                                handleOpenCamera()
                                showPhotoDialog = false
                            }) {
                                Text("Dari Kamera")
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showPhotoDialog = false }) {
                            Text("Tutup")
                        }
                    }
                )
            }
        }
    }

    // Success Dialog
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissSuccessDialog()
                viewModel.resetForm()
            },
            title = {
                Text(
                    text = "Berhasil!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            },
            text = {
                Text("Stok barang berhasil disimpan")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                        viewModel.resetForm()
                    }
                ) {
                    Text("OK", color = Color(0xFFFF9800))
                }
            }
        )
    }

    // Show snackbar for error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }
}
