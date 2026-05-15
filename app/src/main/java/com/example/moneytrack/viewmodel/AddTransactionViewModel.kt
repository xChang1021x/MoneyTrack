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

    val expenseCategories: StateFlow<List<Category>> =
        repository.getCategoriesByType(TransactionType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> =
        repository.getCategoriesByType(TransactionType.INCOME)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── 编辑模式：加载已有账单 ─────────────────────────────────────────
    private val _loadedTransaction = MutableStateFlow<Transaction?>(null)
    val loadedTransaction: StateFlow<Transaction?> = _loadedTransaction.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _loadedTransaction.value = repository.getTransactionById(id)
        }
    }

    // ── 新增 ────────────────────────────────────────────────────────
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
                Transaction(amount = amount, type = type,
                            categoryId = categoryId, note = note, date = date)
            )
            onSuccess()
        }
    }

    // ── 修改 ────────────────────────────────────────────────────────
    fun updateTransaction(
        original: Transaction,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        note: String,
        date: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                original.copy(amount = amount, type = type,
                              categoryId = categoryId, note = note, date = date)
            )
            onSuccess()
        }
    }

    // ── 删除 ────────────────────────────────────────────────────────
    fun deleteTransaction(transaction: Transaction, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            onSuccess()
        }
    }
}
