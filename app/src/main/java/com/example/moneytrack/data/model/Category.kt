package com.example.moneytrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,           // Material Icon 名称，如 "restaurant"
    val color: Long,            // ARGB 颜色值
    val type: TransactionType,  // 收入 or 支出分类
    val isDefault: Boolean = false, // 是否为预置分类
    val sortOrder: Int = 0          // 排列顺序（越小越靠前）
)
