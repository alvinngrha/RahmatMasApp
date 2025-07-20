package com.example.rahmatmas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.supabase.AuthManager
import com.example.rahmatmas.ui.home.HomeAdminScreen
import com.example.rahmatmas.ui.home.HomeCostumerScreen
import com.example.rahmatmas.ui.login.LoginAdminScreen
import com.example.rahmatmas.ui.login.LoginAdminViewModel
import com.example.rahmatmas.ui.login.LoginCustomerScreen
import com.example.rahmatmas.ui.login.LoginViewModelFactory
import com.example.rahmatmas.ui.theme.RahmatMasTheme
import kotlinx.coroutines.flow.collectLatest

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
    val authManager = remember { AuthManager() }
    val context = LocalContext.current
    val adminAuthManager = remember { AdminAuthManager(context) }


    LaunchedEffect(Unit) {
        // Check customer login first
        authManager.isUserLoggedIn.collectLatest { isCustomerLoggedIn ->
            if (isCustomerLoggedIn) {
                navController.navigate("homecostumer") {
                    popUpTo("logincostumer") { inclusive = true }
                }
            }
        }
    }

    // Check admin login status and session validity on app start
    LaunchedEffect(Unit) {
        adminAuthManager.isSessionValid().collect { isValid ->
            if (isValid) {
                navController.navigate("homeadmin") {
                    popUpTo(0) { inclusive = true } // Clear back stack
                }
            }else{
                // If session is not valid, navigate to login admin screen
                navController.navigate("loginadmin") {
                    popUpTo(0) { inclusive = true } // Clear back stack
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "logincostumer",
        modifier = modifier
    ) {
        composable("logincostumer") {
            LoginCustomerScreen(
                onAdminClick = {
                    navController.navigate("loginadmin") {
                        popUpTo("logincostumer") { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    navController.navigate("homecostumer") {
                        popUpTo("logincostumer") { inclusive = true }
                    }
                }
            )
        }
        composable("homecostumer") {
            HomeCostumerScreen(
                onLogout = {
                    navController.navigate("logincostumer") {
                        popUpTo("homecostumer") { inclusive = true }
                    }
                }
            )
        }
        composable("loginadmin") {
            val viewModel: LoginAdminViewModel = viewModel(
                factory = LoginViewModelFactory(adminAuthManager)
            )

            // Handle login success
            LaunchedEffect(Unit) {
                viewModel.isAdminLoggedIn().collect {
                    if (it) {
                        navController.navigate("homeadmin") {
                            popUpTo("loginadmin") { inclusive = true }
                        }
                    }
                }
            }
            LoginAdminScreen(
                viewModel = viewModel,
                onCostumerClick = {
                    navController.navigate("logincostumer") {
                        popUpTo("loginadmin") { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    // Navigate to admin dashboard
                    navController.navigate("homeadmin") {
                        popUpTo("loginadmin") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("homeadmin") {
            HomeAdminScreen(
                onLogout = {
                    navController.navigate("loginadmin") {
                        popUpTo("homeadmin") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
