package com.example.moneytrack.data.model

/** 单个货币的元信息 */
data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String
)

/** App 支持的货币列表（按常用程度排序） */
val SUPPORTED_CURRENCIES = listOf(
    CurrencyInfo("CNY", "人民币",   "¥"),
    CurrencyInfo("USD", "美元",     "$"),
    CurrencyInfo("EUR", "欧元",     "€"),
    CurrencyInfo("GBP", "英镑",     "£"),
    CurrencyInfo("JPY", "日元",     "¥"),
    CurrencyInfo("HKD", "港元",     "HK$"),
    CurrencyInfo("SGD", "新元",     "S$"),
    CurrencyInfo("KRW", "韩元",     "₩"),
    CurrencyInfo("AUD", "澳元",     "A$"),
    CurrencyInfo("CAD", "加元",     "C$"),
    CurrencyInfo("MYR", "马来西亚令吉", "RM"),
)

fun currencySymbol(code: String): String =
    SUPPORTED_CURRENCIES.find { it.code == code }?.symbol ?: code

fun currencyName(code: String): String =
    SUPPORTED_CURRENCIES.find { it.code == code }?.name ?: code

/** 格式化金额（带货币符号；JPY / KRW 显示整数） */
fun formatAmountCurrency(amount: Double, currencyCode: String = "CNY"): String {
    val symbol = currencySymbol(currencyCode)
    return when (currencyCode) {
        "JPY", "KRW" -> "$symbol${"%.0f".format(amount)}"
        else         -> "$symbol${"%.2f".format(amount)}"
    }
}

/**
 * 货币换算：将 [amount] 从 [from] 换算至 [to]。
 * [rates] 是以 CNY 为基准的汇率表（1 CNY = rates[xxx] xxx）。
 * 若汇率缺失则原值返回。
 */
fun convertCurrency(
    amount: Double,
    from: String,
    to: String,
    rates: Map<String, Double>
): Double {
    if (from == to) return amount
    val fromRate = rates[from] ?: return amount   // 1 CNY = fromRate [from]
    val toRate   = rates[to]   ?: return amount   // 1 CNY = toRate [to]
    // amount [from] / fromRate = X CNY; X CNY * toRate = result [to]
    return amount * toRate / fromRate
}
