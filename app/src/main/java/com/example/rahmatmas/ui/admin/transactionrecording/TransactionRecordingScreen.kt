package com.example.rahmatmas.ui.admin.transactionrecording

import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rahmatmas.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionRecordingScreen(
    modifier: Modifier = Modifier,
    onOpenCameraClick: () -> Unit,
    onOpenGalleryClick: () -> Unit,
) {

    val viewModel: TransactionRecordingViewModel = viewModel()
    val transactionUiState by viewModel.transactionUiState.collectAsState()
//    var expanded by rememberSaveable { mutableStateOf(false) }
    val optionsMenuKadar = listOf("700", "833", "999")

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pencatatan Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        }) { innerPadding ->

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
                Spacer(modifier = modifier.height(16.dp))

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_insert_photo),
                        contentDescription = "Gallery Icon",
                        modifier = modifier
                            .size(40.dp)
                            .border(
                                width = 1.dp,
                                color = Color.Gray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onOpenGalleryClick() },
                    )
                    Spacer(modifier = modifier.width(16.dp))

                    Image(
                        painter = painterResource(id = R.drawable.outline_camera),
                        contentDescription = "Gallery Icon",
                        modifier = modifier
                            .size(40.dp)
                            .border(
                                width = 1.dp,
                                color = Color.Gray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onOpenCameraClick() }
                    )
                }
                Spacer(modifier = modifier.height(24.dp))

                Text(
                    text = "ID Transaksi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.idTransaksi,
                    onValueChange = { viewModel.updateIdTransaksi(it) },
                    placeholder = { Text(text = "Masukkan Id Transaksi", fontSize = 12.sp) },
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
                        modifier = modifier
                            .background(Color.White)
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

                Text(
                    text = "Ongkos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.ongkos,
                    onValueChange = { viewModel.updateOngkos(it) },
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

                Text(
                    text = "Harga Dasar Emas per Gram",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.hargaDasarPerGram,
                    onValueChange = { viewModel.updateHargaDasarPerGram(it) },
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

                Button(
                    onClick = { //jika field ada yang kosong, tampilkan snackbar
                        if (transactionUiState.idTransaksi.isEmpty() ||
                            transactionUiState.namaBarang.isEmpty() ||
                            transactionUiState.kadarEmas.isEmpty() ||
                            transactionUiState.beratEmas.isEmpty() ||
                            transactionUiState.ongkos.isEmpty() ||
                            transactionUiState.hargaDasarPerGram.isEmpty()
                        ) {
                            Toast.makeText(
                                context,
                                "Mohon lengkapi semua field sebelum menyimpan transaksi.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.simpanTransaksi()
                        }
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .shadow(24.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB300),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Simpan Transaksi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
