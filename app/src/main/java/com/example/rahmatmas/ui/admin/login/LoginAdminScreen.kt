package com.example.rahmatmas.ui.admin.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rahmatmas.R
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.supabase.AuthResponse

@Composable
fun LoginAdminScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit,
    onCostumerClick: () -> Unit,
    viewModel: LoginAdminViewModel = viewModel(
        factory = LoginViewModelFactory(
            adminAuthManager = AdminAuthManager(LocalContext.current)
        )
    )
) {
    val username by viewModel.username.collectAsState(initial = "")
    val password by viewModel.password.collectAsState(initial = "")
    val loginState by viewModel.loginState.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    // Handle login state changes
    LaunchedEffect(loginState) {
        try {
            when (loginState) {
                is AuthResponse.Success -> {
                    onLoginSuccess()
                    viewModel.resetLoginState()
                    viewModel.clearForm()
                }

                is AuthResponse.Error -> {
                    snackbarMessage =
                        (loginState as AuthResponse.Error).message ?: "Terjadi kesalahan"
                    showSnackbar = true
                }

                null -> { /* No action needed */
                }
            }
        } catch (e: Exception) {
            snackbarMessage = "Terjadi kesalahan: ${e.message}"
            showSnackbar = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Admin Image",
                modifier = Modifier
                    .size(200.dp, 200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 48.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "LOGIN ADMIN",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Username",
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.SemiBold
            )

            TextField(
                value = username,
                onValueChange = { viewModel.updateUsername(it) }, // Update state when text changes
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                placeholder = { Text(text = "Masukkan Username", fontSize = 12.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Password",
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.SemiBold
            )


            TextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) }, // Update state when text changes
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
                placeholder = { Text(text = "Masukkan Password", fontSize = 12.sp) },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.loginAdmin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp),
                shape = RoundedCornerShape(15.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.5.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = "Masuk",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Login sebagai",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Pelanggan",
                    fontSize = 14.sp,
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onCostumerClick() }
                        .padding(start = 4.dp)
                )

            }
        }

        // Snackbar for error messages
        if (showSnackbar) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter),
                action = {
                    Text(
                        text = "OK",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showSnackbar = false }
                    )
                }
            ) {
                Text(text = snackbarMessage)
            }
        }
    }
}