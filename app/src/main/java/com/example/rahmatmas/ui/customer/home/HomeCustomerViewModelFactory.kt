package com.example.rahmatmas.ui.customer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rahmatmas.data.repository.GoldPriceRepository

class HomeCustomerViewModelFactory(private val goldPriceRepository: GoldPriceRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeCustomerViewModel::class.java)) {
            return HomeCustomerViewModel(goldPriceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}