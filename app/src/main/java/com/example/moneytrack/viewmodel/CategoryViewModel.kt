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
            // 新分类排在当前同类型末尾
            val currentList = if (type == TransactionType.EXPENSE)
                expenseCategories.value else incomeCategories.value
            val maxOrder = currentList.maxOfOrNull { it.sortOrder } ?: -1
            repository.insertCategory(
                Category(name = name, icon = icon, color = color, type = type,
                         sortOrder = maxOrder + 1)
            )
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    /**
     * 将 category 在其类型列表中上移一位。
     * 先将整个列表规范化为连续序号（修复迁移后全为 0 的问题），
     * 再交换目标项与前一项的 sortOrder，一次性写回数据库。
     */
    fun moveUp(category: Category, list: List<Category>) {
        val idx = list.indexOfFirst { it.id == category.id }
        if (idx <= 0) return
        viewModelScope.launch {
            val normalized = list.mapIndexed { i, cat -> cat.copy(sortOrder = i) }.toMutableList()
            normalized[idx]     = normalized[idx].copy(sortOrder = idx - 1)
            normalized[idx - 1] = normalized[idx - 1].copy(sortOrder = idx)
            repository.updateCategories(normalized)
        }
    }

    /**
     * 将 category 在其类型列表中下移一位。
     * 同上，先规范化序号，再交换目标项与后一项的 sortOrder。
     */
    fun moveDown(category: Category, list: List<Category>) {
        val idx = list.indexOfFirst { it.id == category.id }
        if (idx < 0 || idx >= list.lastIndex) return
        viewModelScope.launch {
            val normalized = list.mapIndexed { i, cat -> cat.copy(sortOrder = i) }.toMutableList()
            normalized[idx]     = normalized[idx].copy(sortOrder = idx + 1)
            normalized[idx + 1] = normalized[idx + 1].copy(sortOrder = idx)
            repository.updateCategories(normalized)
        }
    }
}
