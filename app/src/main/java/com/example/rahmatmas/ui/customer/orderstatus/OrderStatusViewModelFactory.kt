package com.example.rahmatmas.ui.customer.orderstatus

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OrderStatusViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderStatusViewModel::class.java)) {
            return OrderStatusViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}