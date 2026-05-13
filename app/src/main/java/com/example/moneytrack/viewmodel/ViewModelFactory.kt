package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneytrack.data.repository.MoneyRepository

class ViewModelFactory(private val repository: MoneyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository) as T
            modelClass.isAssignableFrom(AddTransactionViewModel::class.java) ->
                AddTransactionViewModel(repository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(repository) as T
            modelClass.isAssignableFrom(ChartViewModel::class.java) ->
                ChartViewModel(repository) as T
            modelClass.isAssignableFrom(CategoryViewModel::class.java) ->
                CategoryViewModel(repository) as T
            modelClass.isAssignableFrom(BudgetViewModel::class.java) ->
                BudgetViewModel(repository) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(repository) as T
            modelClass.isAssignableFrom(DebtViewModel::class.java) ->
                DebtViewModel(repository) as T
            modelClass.isAssignableFrom(SplitViewModel::class.java) ->
                SplitViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
