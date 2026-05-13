package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.model.Category
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.repository.MoneyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: MoneyRepository) : ViewModel() {

    val allCategories: StateFlow<List<Category>> =
        repository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<Category>> =
        repository.getCategoriesByType(TransactionType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> =
        repository.getCategoriesByType(TransactionType.INCOME)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, icon: String, color: Long, type: TransactionType) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(name = name, icon = icon, color = color, type = type)
            )
        }
    }

    fun deleteCategory(category: Category) {
        if (category.isDefault) return  // 不允许删除预置分类
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
