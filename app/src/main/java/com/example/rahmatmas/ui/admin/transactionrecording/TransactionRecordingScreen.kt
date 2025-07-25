package com.example.rahmatmas.ui.admin.transactionrecording

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionRecordingScreen(
    modifier: Modifier = Modifier
) {

    val viewModel: TransactionRecordingViewModel = viewModel()
    val transactionUiState by viewModel.transactionUiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val optionsMenuKadar = listOf("700", "833", "999")

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
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

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
                    expanded = expanded,
                    modifier = modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    onExpandedChange = {
                        expanded = !expanded
                    }
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
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = modifier
                            .background(Color.White)
                    ) {
                        optionsMenuKadar.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateKadarEmas(option)
                                    expanded = false
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
                Spacer(modifier = modifier.height(16.dp))

                Text(
                    text = "Ongkos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.ongkos.toString(),
                    onValueChange = { viewModel.updateOngkos(it.toIntOrNull() ?: 0) },
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
                    text = "Harga Dasar Emas per Gram",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                TextField(
                    value = transactionUiState.hargaDasarPerGram.toString(),
                    onValueChange = { viewModel.updateHargaDasarPerGram(it.toIntOrNull() ?: 0) },
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

                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                ){
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
                    onClick = { },
                    modifier = modifier
                        .fillMaxWidth()
                        .shadow(16.dp),
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
                Spacer(modifier = modifier.height(8.dp))

//                Button(
//                    onClick = { viewModel.hitungTotal() },
//                    modifier = modifier
//                        .fillMaxWidth(),
//                    shape = RoundedCornerShape(15.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFFF3F4F6).copy(alpha = 0.5f),
//                        contentColor = Color.White
//                    )
//                ) {
//                    Text(
//                        text = "Hitung Total",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        textAlign = TextAlign.Center,
//                        color = Color.Black,
//                        modifier = modifier
//                            .padding(16.dp)
//                            .fillMaxWidth()
//                    )
//                }
            }
        }
    }
}
