package com.example.moneytrack.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 通过 Frankfurter (https://api.frankfurter.app) 获取实时汇率，无需 API Key。
 * 数据来源：欧洲中央银行（ECB），每日更新。
 *
 * 返回以 CNY 为基准的汇率表：1 CNY = rates[xxx] xxx
 * 例如：{"USD":0.138, "EUR":0.126, ..., "CNY":1.0}
 */
object ExchangeRateService {

    private const val BASE_URL = "https://api.frankfurter.app/latest"
    private const val TARGETS  = "USD,EUR,GBP,JPY,HKD,SGD,KRW,AUD,CAD,MYR"

    /**
     * 从网络获取最新汇率。
     * @return 以 CNY 为基准的汇率 Map，或 null（网络失败 / 解析失败时）
     */
    suspend fun fetchRates(): Map<String, Double>? = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL?from=CNY&to=$TARGETS")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout    = 10_000
                requestMethod  = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode != 200) return@withContext null

            val body      = conn.inputStream.bufferedReader().readText()
            val ratesJson = JSONObject(body).getJSONObject("rates")

            val result = mutableMapOf("CNY" to 1.0)
            TARGETS.split(",").forEach { code ->
                if (ratesJson.has(code)) result[code] = ratesJson.getDouble(code)
            }
            result
        } catch (_: Exception) {
            null
        }
    }
}
