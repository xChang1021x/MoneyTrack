package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.model.Category
import com.example.moneytrack.data.model.Transaction
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.repository.MoneyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddTransactionViewModel(private val repository: MoneyRepository) : ViewModel() {

    val allCategories: StateFlow<List<Category>> =
        repository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<Category>> =
        repository.getCategoriesByType(TransactionType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> =
        repository.getCategoriesByType(TransactionType.INCOME)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        note: String,
        date: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    note = note,
                    date = date
                )
            )
            onSuccess()
        }
    }
}
