package com.example.moneytrack.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction as RoomTransaction
import com.example.moneytrack.data.model.Transaction
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @RoomTransaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>>

    @RoomTransaction
    @Query("""
        SELECT * FROM transactions
        WHERE date >= :startMs AND date <= :endMs
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(startMs: Long, endMs: Long): Flow<List<TransactionWithCategory>>

    @RoomTransaction
    @Query("""
        SELECT * FROM transactions
        WHERE type = :type
        ORDER BY date DESC
    """)
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>>

    @RoomTransaction
    @Query("""
        SELECT * FROM transactions
        WHERE categoryId = :categoryId
        AND date >= :startMs AND date <= :endMs
        ORDER BY date DESC
    """)
    fun getTransactionsByCategoryAndDateRange(
        categoryId: Long,
        startMs: Long,
        endMs: Long
    ): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = :type AND date >= :startMs AND date <= :endMs
    """)
    fun getSumByTypeAndDateRange(
        type: TransactionType,
        startMs: Long,
        endMs: Long
    ): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'EXPENSE' AND categoryId = :categoryId
        AND date >= :startMs AND date <= :endMs
    """)
    fun getSpentByCategoryAndDateRange(
        categoryId: Long,
        startMs: Long,
        endMs: Long
    ): Flow<Double>

    @RoomTransaction
    @Query("""
        SELECT * FROM transactions
        WHERE note LIKE '%' || :keyword || '%'
        ORDER BY date DESC
    """)
    fun searchTransactions(keyword: String): Flow<List<TransactionWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}
