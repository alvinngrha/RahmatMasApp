package com.example.rahmatmas.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.supabase.authgoogle.AuthManager
import com.example.rahmatmas.data.supabase.db.SupabaseStock
import com.example.rahmatmas.ui.admin.financialreport.FinancialReportAdminScreen
import com.example.rahmatmas.ui.admin.home.HomeAdminScreen
import com.example.rahmatmas.ui.admin.login.LoginAdminScreen
import com.example.rahmatmas.ui.admin.login.LoginAdminViewModel
import com.example.rahmatmas.ui.admin.login.LoginViewModelFactory
import com.example.rahmatmas.ui.admin.navigation.AdminBottomNavItem
import com.example.rahmatmas.ui.admin.onlinesale.OnlineSaleScreen
import com.example.rahmatmas.ui.admin.profile.ProfileScreen
import com.example.rahmatmas.ui.admin.stock.stockinput.StockInputScreen
import com.example.rahmatmas.ui.admin.stock.stocklist.StockListScreen
import com.example.rahmatmas.ui.admin.transactionhistory.TransactionHistoryScreen
import com.example.rahmatmas.ui.admin.transactionrecording.TransactionRecordingScreen
import com.example.rahmatmas.ui.customer.catalog.CatalogScreen
import com.example.rahmatmas.ui.customer.catalogdetail.CatalogDetailScreen
import com.example.rahmatmas.ui.customer.checkout.CheckoutScreen
import com.example.rahmatmas.ui.customer.home.HomeCustomerScreen
import com.example.rahmatmas.ui.customer.login.LoginCustomerScreen
import com.example.rahmatmas.ui.customer.orderstatus.OrderStatusScreen
import kotlinx.coroutines.flow.first

@Composable
fun RahmatMasApp(
    modifier: Modifier,
    navController: NavHostController = rememberNavController()
) {

    MaterialTheme(
        colorScheme = lightColorScheme(
            surface = Color.White,
            background = Color.White,
            primary = Color(0xFFFF9800),
            onSurface = Color.Black,
            onBackground = Color.Black,
        )
    ) {
        val authManager = remember { AuthManager() }
        val context = LocalContext.current
        val adminAuthManager = remember { AdminAuthManager(context) }
        val viewModel: LoginAdminViewModel = viewModel(
            factory = LoginViewModelFactory(adminAuthManager)
        )
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBarRoutes = listOf("homeadmin", "onlinesaleadmin", "profileadmin")

        //di halaman login tidak ada bottom bar
        Scaffold(
            bottomBar = {
                if (currentRoute in showBottomBarRoutes) {
                    AdminBottomBar(
                        navController = navController,
                        modifier = Modifier,
                    )
                }
            },
        ) { paddingValues ->
            // Determine initial destination based on login status
            LaunchedEffect(Unit) {
                // Check admin session first (priority)
                val isAdminSessionValid = viewModel.isSessionValid().first()
                if (isAdminSessionValid) {
                    navController.navigate("homeadmin") {
                        popUpTo(0) { inclusive = true }
                    }
                    return@LaunchedEffect
                }

                // Then check customer login
                val isCustomerLoggedIn = authManager.isUserLoggedIn.first()
                if (isCustomerLoggedIn) {
                    navController.navigate("homecostumer") {
                        popUpTo(0) { inclusive = true }
                    }
                    return@LaunchedEffect
                }
            }

            NavHost(
                navController = navController,
                startDestination = "logincostumer",
                modifier = modifier.padding(paddingValues)
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
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("homecostumer") {
                    HomeCustomerScreen(
                        onLogout = {
                            navController.navigate("logincostumer") {
                                popUpTo("homecostumer") { inclusive = true }
                            }
                        },
                        onGoToCatalog = {
                            // Navigate to catalog screen
                            // Assuming you have a catalog screen, replace with actual navigation
                            navController.navigate("catalogcostumer") {
                                launchSingleTop = true
                            }
                        },
                        onGoToOrderStatus = {
                            // Navigate to order status screen
                            // Assuming you have an order status screen, replace with actual navigation
                            navController.navigate("orderstatuscustomer") {
                                launchSingleTop = true
                            }
                        },
                        onGoToHistory = {
                            navController.navigate("purchasehistorycustomer") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("orderstatuscustomer") {
                    OrderStatusScreen(
                        onBackClick = {
                            navController.navigateUp()
                        },
                    )
                }
                composable("purchasehistorycustomer") {
                    com.example.rahmatmas.ui.customer.purchasehistory.PurchaseHistoryScreen(
                        onBackClick = { navController.navigateUp() }
                    )
                }
                composable("loginadmin") {
                    LoginAdminScreen(
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
                            // Handle logout logic here
                            // For example, navigate to login screen or clear session
                            navController.navigate("logincostumer") {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                            }
                        },
                        onGoToRecording = {
                            navController.navigate("transactionadmin") {
                                launchSingleTop = true
                            }
                        },
                        onGoToTransactionHistory = {
                            navController.navigate("transactionhistory") {
                                launchSingleTop = true
                            }
                        },
                        onGoToStockManagement = {
                            navController.navigate("stocklistadmin") {
                                launchSingleTop = true
                            }
                        },
                        onGoToFinancialReport = {
                            navController.navigate("financialreportadmin") { launchSingleTop = true }
                        },
                        onGoToOnlineSale = {
                            navController.navigate("onlinesaleadmin") { launchSingleTop = true }
                        }
                    )
                }

                composable("stockinputadmin") {
                    val stockToEdit = navController.previousBackStackEntry?.savedStateHandle?.get<SupabaseStock>("editStock")
                    StockInputScreen(
                        onBackClick = {
                            navController.navigateUp()
                        },
                        stockToEdit = stockToEdit
                    )
                }

                composable("stocklistadmin") {
                    StockListScreen(
                        onAddStockClick = {
                            // Clear any previous edit stock key to ensure fresh create mode
                            navController.currentBackStackEntry?.savedStateHandle?.remove<SupabaseStock>("editStock")
                            navController.navigate("stockinputadmin") {
                                launchSingleTop = true
                            }
                        },
                        onBackClick = {
                            navController.navigateUp()
                        },
                        onEditStockClick = { stock ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("editStock", stock)
                            navController.navigate("stockinputadmin") { launchSingleTop = true }
                        }
                    )
                }

                composable("transactionadmin") {
                    TransactionRecordingScreen(
                        onBackClick = {
                            navController.navigateUp()
                        },
                    )
                }

                composable("transactionhistory") {
                    TransactionHistoryScreen(
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable("onlinesaleadmin") {
                    OnlineSaleScreen()
                }

                composable("financialreportadmin") {
                    FinancialReportAdminScreen(
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable("profileadmin") {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate("logincostumer") {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                }


                composable("catalogcostumer") {
                    CatalogScreen(
                        onProductClick = { stock ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("stock", stock)
                            navController.navigate("catalogdetailcostumer")
                        },
                        onBackClick = { navController.navigateUp() }

                    )
                }

                composable("catalogdetailcostumer"){
                    val stock = navController.previousBackStackEntry?.savedStateHandle?.get<SupabaseStock>("stock")
                    stock?.let {
                        CatalogDetailScreen(
                            stock = it,
                            onBackClick = { navController.navigateUp() },
                            onOrderClick = { selected, qty ->
                                navController.currentBackStackEntry?.savedStateHandle?.set("checkoutStock", selected)
                                navController.currentBackStackEntry?.savedStateHandle?.set("checkoutQty", qty)
                                navController.navigate("checkoutcostumer")
                            }
                        )
                    }
                }

                composable("checkoutcostumer") {
                    val stockCheckout = navController.previousBackStackEntry?.savedStateHandle?.get<SupabaseStock>("checkoutStock")
                    val qty = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("checkoutQty") ?: 1
                    stockCheckout?.let { stockItem ->
                        CheckoutScreen(
                            stock = stockItem,
                            quantity = qty,
                            onBackClick = { navController.navigateUp() },
                            onOrderPlaced = {
                                navController.navigate("homecostumer") {
                                    popUpTo("homecostumer") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}



// Bottom Navigation Component
@Composable
private fun AdminBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val navigationItems = listOf(
            AdminBottomNavItem(
                route = "homeadmin",
                title = "Beranda",
                icon = Icons.Default.Home,
            ),
            AdminBottomNavItem(
                route = "onlinesaleadmin",
                title = "Penjualan Online",
                icon = Icons.Default.DateRange,
            ),
            AdminBottomNavItem(
                route = "profileadmin",
                title = "Profil",
                icon = Icons.Default.AccountCircle,
            )
        )

        navigationItems.map { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.title, fontSize = 11.sp) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
