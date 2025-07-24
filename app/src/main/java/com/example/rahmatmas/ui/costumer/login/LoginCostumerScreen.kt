package com.example.rahmatmas.ui.costumer.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun LoginCustomerScreen(
    modifier: Modifier = Modifier,
    onAdminClick: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    val viewModel: LoginCostumerViewModel = viewModel()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Navigasi otomatis jika sudah login
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.toko_perhiasan),
                contentDescription = "Login Image",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(350.dp, 300.dp)
                    .padding(top = 32.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "RAHMATMAS",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "login saja dengan akun google anda, lalu anda bisa melihat informasi dan membeli " +
                        "emas secara online tanpa harus datang ke lokasi melalui aplikasi RahmatMas.",
                modifier = Modifier.padding(horizontal = 8.dp),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontWeight = FontWeight.Normal,
            )

            // Error message
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Row {
                Button(
                    onClick = {
                        viewModel.signInWithGoogle(context) {
                            onLoginSuccess()
                        }
                    },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .fillMaxWidth(),
                    shape = CircleShape,
                    border = ButtonDefaults.outlinedButtonBorder,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.logo_google),
                            contentDescription = "Google Sign-In",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (uiState.isLoading) "Loading..." else "Masuk Dengan Google",
                        color = Color.Black,
                    )
                }
            }
            Row {
                Text(
                    text = "login sebagai",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Admin",
                    fontSize = 14.sp,
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { onAdminClick() }
                )
            }
        }
    }
}

