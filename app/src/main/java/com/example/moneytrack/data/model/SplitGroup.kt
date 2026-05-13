package com.example.moneytrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 一次分账会话（如"周末聚餐"） */
@Entity(tableName = "split_group")
data class SplitGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: Long,                   // 创建日期（毫秒时间戳）
    val note: String = "",
    val participants: String = ""     // 参与人列表，逗号分隔，持久保存
)
