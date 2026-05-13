package com.example.moneytrack.data.preferences

import android.content.Context

/**
 * 存储用户自定义底部导航栏的路由顺序。
 * Profile（更多）始终固定在末位，此处只管理其余 1-4 个槽位。
 */
class NavPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("nav_prefs", Context.MODE_PRIVATE)

    fun getRoutes(): List<String> {
        val saved = prefs.getString(KEY, DEFAULT_ROUTES)
        return saved!!.split(",").filter { it.isNotBlank() }
    }

    fun saveRoutes(routes: List<String>) {
        prefs.edit().putString(KEY, routes.joinToString(",")).apply()
    }

    companion object {
        private const val KEY = "bottom_nav_routes"
        const val DEFAULT_ROUTES = "home,history,add,chart"
        const val MAX_SLOTS = 4   // Profile 固定，用户最多配置 4 个
    }
}
