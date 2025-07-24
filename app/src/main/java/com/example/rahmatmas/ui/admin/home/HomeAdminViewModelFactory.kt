package com.example.rahmatmas.ui.admin.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rahmatmas.data.datastore.AdminAuthManager
import com.example.rahmatmas.data.repository.GoldPriceRepository

class HomeAdminViewModelFactory(
    private val adminAuthManager: AdminAuthManager, private val goldPriceRepository: GoldPriceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeAdminViewModel::class.java)) {
            return HomeAdminViewModel(adminAuthManager, goldPriceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
