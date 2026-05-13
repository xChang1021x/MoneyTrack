package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.TransactionWithCategory
import com.example.moneytrack.data.repository.MoneyRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class CategoryStat(val categoryName: String, val color: Long, val amount: Double)

class ChartViewModel(private val repository: MoneyRepository) : ViewModel() {

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedYear: StateFlow<Int> = _selectedYear
    val selectedMonth: StateFlow<Int> = _selectedMonth

    private val dateRange = combine(_selectedYear, _selectedMonth) { year, month ->
        getMonthRange(year, month)
    }

    val monthlyTransactions: StateFlow<List<TransactionWithCategory>> =
        dateRange.flatMapLatest { (start, end) ->
            repository.getTransactionsByDateRange(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyIncome: StateFlow<Double> =
        dateRange.flatMapLatest { (start, end) ->
            repository.getSumByTypeAndDateRange(TransactionType.INCOME, start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpense: StateFlow<Double> =
        dateRange.flatMapLatest { (start, end) ->
            repository.getSumByTypeAndDateRange(TransactionType.EXPENSE, start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // 支出按分类统计
    val expenseCategoryStats: StateFlow<List<CategoryStat>> =
        monthlyTransactions.map { list ->
            list.filter { it.transaction.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .map { (cat, items) ->
                    CategoryStat(
                        categoryName = cat?.name ?: "未知",
                        color = cat?.color ?: 0xFF78909C,
                        amount = items.sumOf { it.transaction.amount }
                    )
                }
                .sortedByDescending { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun previousMonth() {
        val m = _selectedMonth.value
        val y = _selectedYear.value
        if (m == 1) { _selectedMonth.value = 12; _selectedYear.value = y - 1 }
        else _selectedMonth.value = m - 1
    }

    fun nextMonth() {
        val m = _selectedMonth.value
        val y = _selectedYear.value
        if (m == 12) { _selectedMonth.value = 1; _selectedYear.value = y + 1 }
        else _selectedMonth.value = m + 1
    }

    private fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }
}
