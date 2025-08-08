package com.example.rahmatmas.ui.admin.stock.stockinput

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StockInputViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockInputViewModel::class.java)) {
            return StockInputViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}