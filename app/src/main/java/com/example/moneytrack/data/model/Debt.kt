package com.example.moneytrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debt")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Double,
    val type: DebtType,          // LENT = 别人欠我；BORROWED = 我欠别人
    val date: Long,              // 记录日期（毫秒时间戳）
    val dueDate: Long? = null,   // 到期日（可选，毫秒时间戳）
    val note: String = "",
    val isSettled: Boolean = false
)
