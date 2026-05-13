package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.TransactionWithCategory
import com.example.moneytrack.data.repository.MoneyRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: MoneyRepository) : ViewModel() {

    private val _keyword = MutableStateFlow("")
    val keyword: StateFlow<String> = _keyword

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType: StateFlow<TransactionType?> = _filterType

    val searchResults: StateFlow<List<TransactionWithCategory>> =
        _keyword
            .debounce(300)
            .flatMapLatest { kw ->
                if (kw.isBlank()) repository.getAllTransactions()
                else repository.searchTransactions(kw)
            }
            .combine(_filterType) { list, type ->
                if (type == null) list else list.filter { it.transaction.type == type }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setKeyword(kw: String) { _keyword.value = kw }
    fun setFilterType(type: TransactionType?) { _filterType.value = type }
}
