package com.example.moneytrack.data.db

import androidx.room.TypeConverter
import com.example.moneytrack.data.model.DebtType
import com.example.moneytrack.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromDebtType(type: DebtType): String = type.name

    @TypeConverter
    fun toDebtType(value: String): DebtType = DebtType.valueOf(value)
}
