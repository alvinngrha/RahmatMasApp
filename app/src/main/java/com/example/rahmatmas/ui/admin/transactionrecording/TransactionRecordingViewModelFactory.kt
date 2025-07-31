package com.example.rahmatmas.ui.admin.transactionrecording

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TransactionRecordingViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionRecordingViewModel::class.java)) {
            return TransactionRecordingViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}