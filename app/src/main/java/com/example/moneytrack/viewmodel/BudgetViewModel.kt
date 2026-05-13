package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.model.Budget
import com.example.moneytrack.data.model.Category
import com.example.moneytrack.data.repository.MoneyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class BudgetWithSpent(
    val budget: Budget,
    val category: Category?,
    val spent: Double
)

class BudgetViewModel(private val repository: MoneyRepository) : ViewModel() {

    private val now = Calendar.getInstance()
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH) + 1

    val budgets: StateFlow<List<Budget>> =
        repository.getBudgetsByMonth(currentYear, currentMonth)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<Category>> =
        repository.getCategoriesByType(com.example.moneytrack.data.model.TransactionType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            val existing = repository.getBudgetByCategoryAndMonth(categoryId, currentYear, currentMonth)
            if (existing != null) {
                repository.updateBudget(existing.copy(amount = amount))
            } else {
                repository.insertBudget(
                    Budget(categoryId = categoryId, amount = amount, month = currentMonth, year = currentYear)
                )
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { repository.deleteBudget(budget) }
    }

    fun getMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth - 1, 1, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return Pair(start, cal.timeInMillis)
    }

    fun getSpentFlow(categoryId: Long): Flow<Double> {
        val (start, end) = getMonthRange()
        return repository.getSpentByCategoryAndDateRange(categoryId, start, end)
    }
}
