package com.example.rahmatmas.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rahmatmas.R

@Composable
fun LoginCustomerScreen(
    modifier: Modifier = Modifier,
    onAdminClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
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
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { onAdminClick() }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.toko_perhiasan),
            contentDescription = "Login Image",
            modifier = Modifier
                .size(400.dp, 350.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "RAHMATMAS",
            style = typography.bodyLarge,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "login saja dengan akun google anda, lalu anda bisa melihat informasi dan membeli " +
                    "emas secara online tanpa harus datang ke lokasi melalui aplikasi RahmatMas.",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
        )
        Spacer(modifier = Modifier.weight(1f))

        Row {
            Button(
                onClick = { /* TODO: Implement Google Sign-In */ },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .fillMaxWidth(),
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_google),
                    contentDescription = "Google Sign-In",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Masuk Dengan Google",
                    color = Color.Black,
                )

            }
        }
    }
}


@Composable
fun LoginAdminScreen(
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            textAlign = TextAlign.Center,
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

        TextField( // Or use TextField / BasicTextField for different looks
            value = username, // Link to the state variable
            onValueChange = { newValue -> username = newValue }, // Update state when text changes
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            placeholder = { Text(text = "Masukkan Username") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Gray, // Underline color when focused
                unfocusedIndicatorColor = Color.LightGray, // Underline color when not focused
                disabledIndicatorColor = Color.Gray,
                errorIndicatorColor = Color.Red
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Password",
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.SemiBold
        )

        TextField( // Or use TextField / BasicTextField for different looks
            value = password, // Link to the state variable
            onValueChange = { newValue -> password = newValue }, // Update state when text changes
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            placeholder = { Text(text = "Masukkan Password") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Gray, // Underline color when focused
                unfocusedIndicatorColor = Color.LightGray, // Underline color when not focused
                disabledIndicatorColor = Color.Gray,
                errorIndicatorColor = Color.Red
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Implement Admin Login */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(IntrinsicSize.Min),
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            colors = ButtonDefaults.buttonColors(Color(0xFFFF9800))
        ) {
            Text(
                text = "Login",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

