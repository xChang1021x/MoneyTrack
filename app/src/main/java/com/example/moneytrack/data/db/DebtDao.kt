package com.example.moneytrack.data.db

import androidx.room.*
import com.example.moneytrack.data.model.Debt
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Query("SELECT * FROM debt ORDER BY date DESC")
    fun getAllDebts(): Flow<List<Debt>>

    @Query("SELECT * FROM debt WHERE isSettled = 0 ORDER BY date ASC")
    fun getUnsettledDebts(): Flow<List<Debt>>

    /** 取所有曾经出现过的联系人名称（去重），供分账计算器复用 */
    @Query("SELECT DISTINCT personName FROM debt ORDER BY personName ASC")
    fun getAllPersonNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt): Long

    @Update
    suspend fun updateDebt(debt: Debt)

    @Delete
    suspend fun deleteDebt(debt: Debt)
}
