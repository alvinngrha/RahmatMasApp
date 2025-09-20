package com.example.rahmatmas.ui.admin.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.local.db.AppDatabase
import com.example.rahmatmas.data.network.NetworkMonitor
import com.example.rahmatmas.data.repository.GoldPriceRepository
import com.example.rahmatmas.data.repository.OrderRepository
import com.example.rahmatmas.data.repository.StockRepository

class HomeAdminViewModelFactory(
    private val context: Context,
    private val adminAuthManager: AdminAuthManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeAdminViewModel::class.java)) {
            val appContext = context.applicationContext
            val database = AppDatabase.getDatabase(appContext)
            val networkMonitor = NetworkMonitor(appContext)
            val goldPriceRepository = GoldPriceRepository()
            val stockRepository = StockRepository(networkMonitor, appContext)
            val orderRepository = OrderRepository()

            return HomeAdminViewModel(
                adminAuthManager = adminAuthManager,
                goldPriceRepository = goldPriceRepository,
                transactionDao = database.transactionDao(),
                stockRepository = stockRepository,
                orderRepository = orderRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
