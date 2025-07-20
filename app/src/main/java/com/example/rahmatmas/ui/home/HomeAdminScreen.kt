package com.example.rahmatmas.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeAdminScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    viewModel: HomeAdminViewModel = viewModel()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .height(200.dp),
                color = Color(0xFFFF9800),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                shadowElevation = 4.dp
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
                            text = "Selamat datang, Admin!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "logout Icon",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(24.dp)
                                .clickable { onLogout() },
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier
                            .width(300.dp)
                            .height(IntrinsicSize.Max)
                            .align(Alignment.CenterHorizontally),
                        color = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        ) {
                            Text(
                                text = "Harga Emas Hari Ini",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "1.500.000",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .width(300.dp)
                    .height(IntrinsicSize.Max)
                    .align(Alignment.CenterHorizontally),
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row {
                        Surface(
                            modifier = Modifier
                                .width(80.dp)
                                .height(IntrinsicSize.Max),
                            color = Color(0xFF2563EB).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(15.dp),
                            tonalElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "pencatatan transaksi Icon",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.CenterHorizontally),
                                    tint = Color.White
                                )
                                Text(
                                    text = "Pencatatan Transaksi",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}