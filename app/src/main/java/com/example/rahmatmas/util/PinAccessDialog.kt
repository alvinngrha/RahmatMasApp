package com.example.rahmatmas.ui.admin.stock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rahmatmas.R
import kotlinx.coroutines.launch

@Composable
fun PinAccessDialog(
    title: String = "Masukkan PIN Akses",
    message: String = "Masukkan PIN untuk melanjutkan",
    onDismiss: () -> Unit,
    onPinConfirmed: () -> Unit,
    onPinValidation: (String) -> Boolean
) {
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(0) }
    val maxAttempts = 3

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Lock
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color(0xFFFF9800).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // PIN Input Field
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            pinInput = it
                            errorMessage = ""
                        }
                    },
                    label = { Text("PIN Akses") },
                    placeholder = { Text("Masukkan PIN 4-6 digit") },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = errorMessage.isNotEmpty(),
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                painter = if (showPin) painterResource(R.drawable.baseline_visibility_24) else painterResource(R.drawable.baseline_visibility_off_24),
                                contentDescription = if (showPin) "Sembunyikan PIN" else "Tampilkan PIN",
                                tint = if (showPin) Color(0xFFFF9800) else Color.Gray
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        errorBorderColor = Color.Red,
                        focusedLabelColor = Color(0xFFFF9800),
                        cursorColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Error Message
                if (errorMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Attempts Counter
                if (attempts > 0 && attempts < maxAttempts) {
                    Text(
                        text = "Percobaan tersisa: ${maxAttempts - attempts}",
                        fontSize = 11.sp,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Security Note
                Text(
                    text = "🔒 PIN ini hanya diketahui oleh admin",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinInput.isEmpty()) {
                        errorMessage = "PIN tidak boleh kosong"
                        return@Button
                    }

                    if (pinInput.length < 4) {
                        errorMessage = "PIN minimal 4 digit"
                        return@Button
                    }

                    if (onPinValidation(pinInput)) {
                        // PIN benar
                        onPinConfirmed()
                        onDismiss()
                    } else {
                        // PIN salah
                        attempts++
                        if (attempts >= maxAttempts) {
                            errorMessage = "Terlalu banyak percobaan gagal. Akses ditolak!"
                            // Auto close after 2 seconds
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(2000)
                                onDismiss()
                            }
                        } else {
                            errorMessage = "PIN salah! Silakan coba lagi."
                            pinInput = ""
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = pinInput.isNotEmpty() && attempts < maxAttempts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Konfirmasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Batal",
                    fontSize = 16.sp
                )
            }
        }
    )
}