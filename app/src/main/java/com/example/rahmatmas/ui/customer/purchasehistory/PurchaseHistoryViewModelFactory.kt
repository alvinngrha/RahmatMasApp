package com.example.rahmatmas.ui.customer.purchasehistory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PurchaseHistoryViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaseHistoryViewModel::class.java)) {
            return PurchaseHistoryViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}