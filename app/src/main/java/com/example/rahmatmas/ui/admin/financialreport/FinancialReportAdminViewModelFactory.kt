package com.example.rahmatmas.ui.admin.financialreport

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FinancialReportAdminViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialReportAdminViewModel::class.java)) {
            return FinancialReportAdminViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}