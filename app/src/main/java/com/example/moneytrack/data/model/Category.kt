package com.example.moneytrack.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Long,
    val type: TransactionType,
    val isDefault: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val sortOrder: Int = 0
)
