package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.preferences.CurrencyPreferences
import com.example.moneytrack.data.remote.ExchangeRateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrencyViewModel(private val prefs: CurrencyPreferences) : ViewModel() {

    private val _displayCurrency = MutableStateFlow(prefs.displayCurrency)
    val displayCurrency: StateFlow<String> = _displayCurrency.asStateFlow()

    /** 以 CNY 为基准的汇率表：1 CNY = rates[xxx] xxx */
    private val _exchangeRates = MutableStateFlow(loadCachedRates())
    val exchangeRates: StateFlow<Map<String, Double>> = _exchangeRates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _ratesDate = MutableStateFlow(formatTimestamp(prefs.ratesTimestamp))
    val ratesDate: StateFlow<String> = _ratesDate.asStateFlow()

    /** true = 合并模式（换算汇总）; false = 分开模式（仅显示当前货币账单） */
    private val _isMergeMode = MutableStateFlow(prefs.isMergeMode)
    val isMergeMode: StateFlow<Boolean> = _isMergeMode.asStateFlow()

    /** 首页货币切换栏中启用的货币代码集合 */
    private val _enabledCurrencies = MutableStateFlow(prefs.enabledCurrencies)
    val enabledCurrencies: StateFlow<Set<String>> = _enabledCurrencies.asStateFlow()

    init {
        val staleThreshold = 4L * 60 * 60 * 1000
        if (System.currentTimeMillis() - prefs.ratesTimestamp > staleThreshold) {
            refreshRates()
        }
    }

    fun setDisplayCurrency(code: String) {
        prefs.displayCurrency = code
        _displayCurrency.value = code
    }

    fun setLastUsedCurrency(code: String) { prefs.lastUsedCurrency = code }
    fun getLastUsedCurrency(): String = prefs.lastUsedCurrency

    fun toggleMergeMode() {
        val next = !_isMergeMode.value
        prefs.isMergeMode = next
        _isMergeMode.value = next
    }

    fun setEnabledCurrencies(codes: Set<String>) {
        val safe = if (codes.isEmpty()) setOf("CNY") else codes
        prefs.enabledCurrencies = safe
        _enabledCurrencies.value = safe
        // 若当前展示货币被禁用，切换到 CNY
        if (_displayCurrency.value !in safe) setDisplayCurrency("CNY")
    }

    fun refreshRates() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ExchangeRateService.fetchRates()
            if (result != null) {
                _exchangeRates.value = result
                val json = JSONObject()
                result.forEach { (k, v) -> json.put(k, v) }
                prefs.cachedRatesJson = json.toString()
                prefs.ratesTimestamp  = System.currentTimeMillis()
                _ratesDate.value      = formatTimestamp(prefs.ratesTimestamp)
            }
            _isLoading.value = false
        }
    }

    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount
        val rates    = _exchangeRates.value
        val fromRate = rates[fromCurrency] ?: return amount
        val toRate   = rates[toCurrency]   ?: return amount
        return amount * toRate / fromRate
    }

    private fun loadCachedRates(): Map<String, Double> {
        return try {
            val obj = JSONObject(prefs.cachedRatesJson)
            val map = mutableMapOf("CNY" to 1.0)
            obj.keys().forEach { k -> map[k] = obj.getDouble(k) }
            map
        } catch (_: Exception) {
            mapOf("CNY" to 1.0)
        }
    }

    private fun formatTimestamp(ms: Long): String {
        if (ms == 0L) return ""
        return SimpleDateFormat("MM/dd HH:mm", Locale.CHINESE).format(Date(ms))
    }

    class Factory(private val prefs: CurrencyPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CurrencyViewModel(prefs) as T
    }
}
