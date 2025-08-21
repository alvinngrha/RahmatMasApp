package com.example.rahmatmas.ui.admin.onlinesale

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OnlineSaleAdminViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnlineSaleAdminViewModel::class.java)) {
            return OnlineSaleAdminViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}