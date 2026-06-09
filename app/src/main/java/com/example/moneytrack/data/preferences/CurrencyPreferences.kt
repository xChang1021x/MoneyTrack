package com.example.moneytrack.data.preferences

import android.content.Context

class CurrencyPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)

    var displayCurrency: String
        get() = prefs.getString(KEY_DISPLAY, "CNY") ?: "CNY"
        set(value) { prefs.edit().putString(KEY_DISPLAY, value).apply() }

    var lastUsedCurrency: String
        get() = prefs.getString(KEY_LAST_USED, "CNY") ?: "CNY"
        set(value) { prefs.edit().putString(KEY_LAST_USED, value).apply() }

    var cachedRatesJson: String
        get() = prefs.getString(KEY_RATES_JSON, "{}") ?: "{}"
        set(value) { prefs.edit().putString(KEY_RATES_JSON, value).apply() }

    var ratesTimestamp: Long
        get() = prefs.getLong(KEY_RATES_TIMESTAMP, 0L)
        set(value) { prefs.edit().putLong(KEY_RATES_TIMESTAMP, value).apply() }

    /** true = 合并模式（换算汇总）; false = 分开模式（仅显示当前货币账单） */
    var isMergeMode: Boolean
        get() = prefs.getBoolean(KEY_MERGE_MODE, true)
        set(value) { prefs.edit().putBoolean(KEY_MERGE_MODE, value).apply() }

    /** 首页货币切换栏中展示的货币代码集合（默认全部启用） */
    var enabledCurrencies: Set<String>
        get() = prefs.getStringSet(KEY_ENABLED_CURRENCIES, null)
            ?: setOf("CNY", "USD", "EUR", "GBP", "JPY", "HKD", "SGD", "KRW", "AUD", "CAD", "MYR")
        set(value) { prefs.edit().putStringSet(KEY_ENABLED_CURRENCIES, value).apply() }

    companion object {
        private const val KEY_DISPLAY             = "display_currency"
        private const val KEY_LAST_USED           = "last_used_currency"
        private const val KEY_RATES_JSON          = "exchange_rates_json"
        private const val KEY_RATES_TIMESTAMP     = "rates_timestamp"
        private const val KEY_MERGE_MODE          = "is_merge_mode"
        private const val KEY_ENABLED_CURRENCIES  = "enabled_currencies"
    }
}
