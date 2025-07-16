package com.example.rahmatmas.ui.login

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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

@Composable
fun LoginCustomerScreen(
    modifier: Modifier = Modifier,
    onAdminClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Admin",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .clickable { onAdminClick() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.toko_perhiasan),
                        contentDescription = "Login Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "RAHMATMAS",
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "login saja dengan akun google anda, lalu anda bisa melihat informasi dan membeli " +
                                "emas secara online tanpa harus datang ke lokasi melalui aplikasi RahmatMas.",
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
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
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
                }
            }
}


@Composable
fun LoginAdminScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

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
            onValueChange = { newvalue -> username = newvalue }, // Update state when text changes
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            maxLines = 1,
            placeholder = { Text(text = "Masukkan Username", fontSize = 12.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.LightGray,
                unfocusedContainerColor = Color.LightGray,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray
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
            onValueChange = { newvalue -> password = newvalue }, // Update state when text changes
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            maxLines = 1,
            shape = RoundedCornerShape(15.dp),
            placeholder = { Text(text = "Masukkan Password", fontSize = 12.sp) },
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.LightGray,
                unfocusedContainerColor = Color.LightGray,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray,
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Implement Admin Login */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(IntrinsicSize.Min),
            shape = RoundedCornerShape(15.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFFFF9800))
        ) {
            Text(
                text = "Masuk",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

