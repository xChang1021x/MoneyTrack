package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneytrack.data.preferences.NavPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavViewModel(private val prefs: NavPreferences) : ViewModel() {

    private val _routes = MutableStateFlow(prefs.getRoutes())
    val routes: StateFlow<List<String>> = _routes.asStateFlow()

    fun updateRoutes(routes: List<String>) {
        prefs.saveRoutes(routes)
        _routes.value = routes
    }

    fun resetToDefault() {
        updateRoutes(NavPreferences.DEFAULT_ROUTES.split(","))
    }

    /** 独立工厂，不依赖 MoneyRepository */
    class Factory(private val prefs: NavPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NavViewModel(prefs) as T
    }
}
