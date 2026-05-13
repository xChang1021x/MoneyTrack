package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.model.Debt
import com.example.moneytrack.data.repository.MoneyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DebtViewModel(private val repository: MoneyRepository) : ViewModel() {

    val allDebts = repository.getAllDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personNames = repository.getAllPersonNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDebt(debt: Debt) = viewModelScope.launch {
        repository.insertDebt(debt)
    }

    fun updateDebt(debt: Debt) = viewModelScope.launch {
        repository.updateDebt(debt)
    }

    fun deleteDebt(debt: Debt) = viewModelScope.launch {
        repository.deleteDebt(debt)
    }

    fun settleDebt(debt: Debt) = viewModelScope.launch {
        repository.updateDebt(debt.copy(isSettled = true))
    }
}
