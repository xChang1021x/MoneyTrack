package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.TransactionWithCategory
import com.example.moneytrack.data.repository.MoneyRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar

class HomeViewModel(private val repository: MoneyRepository) : ViewModel() {

    private val now = Calendar.getInstance()
    private val startOfMonth: Long
    private val endOfMonth: Long

    init {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        startOfMonth = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        endOfMonth = cal.timeInMillis
    }

    val allMonthlyTransactions: StateFlow<List<TransactionWithCategory>> =
        repository.getTransactionsByDateRange(startOfMonth, endOfMonth)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<TransactionWithCategory>> =
        repository.getTransactionsByDateRange(startOfMonth, endOfMonth)
            .map { it.take(20) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyIncome: StateFlow<Double> =
        repository.getSumByTypeAndDateRange(TransactionType.INCOME, startOfMonth, endOfMonth)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpense: StateFlow<Double> =
        repository.getSumByTypeAndDateRange(TransactionType.EXPENSE, startOfMonth, endOfMonth)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balance: StateFlow<Double> = combine(monthlyIncome, monthlyExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
