package com.example.rahmatmas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rahmatmas.ui.login.LoginAdminScreen
import com.example.rahmatmas.ui.login.LoginCustomerScreen
import com.example.rahmatmas.ui.theme.RahmatMasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RahmatMasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    RahmatMasApp(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it) // Apply padding to avoid content being obscured by the system bars
                    )
                }
            }
        }
    }
}

@Composable
fun RahmatMasApp(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "logincostumer",
        modifier = Modifier
    ) {
        composable("logincostumer") {
            LoginCustomerScreen(
                onAdminClick = { navController.navigate("loginadmin") }
            )
        }
        composable("loginadmin") {
            LoginAdminScreen()
            // If you need to navigate back from Admin screen, you'd pass navController to it
        }
    }
}

