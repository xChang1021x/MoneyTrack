package com.example.moneytrack.data.repository

import com.example.moneytrack.data.db.BudgetDao
import com.example.moneytrack.data.db.CategoryDao
import com.example.moneytrack.data.db.DebtDao
import com.example.moneytrack.data.db.SplitDao
import com.example.moneytrack.data.db.TransactionDao
import com.example.moneytrack.data.model.*
import kotlinx.coroutines.flow.Flow

class MoneyRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val debtDao: DebtDao,
    private val splitDao: SplitDao
) {
    // ─── Transaction ───────────────────────────────────────────────

    fun getAllTransactions(): Flow<List<TransactionWithCategory>> =
        transactionDao.getAllTransactionsWithCategory()

    fun getTransactionsByDateRange(startMs: Long, endMs: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsByDateRange(startMs, endMs)

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsByType(type)

    fun getTransactionsByCategoryAndDateRange(
        categoryId: Long, startMs: Long, endMs: Long
    ): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsByCategoryAndDateRange(categoryId, startMs, endMs)

    fun getSumByTypeAndDateRange(type: TransactionType, startMs: Long, endMs: Long): Flow<Double> =
        transactionDao.getSumByTypeAndDateRange(type, startMs, endMs)

    fun getSpentByCategoryAndDateRange(categoryId: Long, startMs: Long, endMs: Long): Flow<Double> =
        transactionDao.getSpentByCategoryAndDateRange(categoryId, startMs, endMs)

    fun searchTransactions(keyword: String): Flow<List<TransactionWithCategory>> =
        transactionDao.searchTransactions(keyword)

    suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction)

    // ─── Category ──────────────────────────────────────────────────

    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories()

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: Category): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: Category) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category)

    // ─── Budget ────────────────────────────────────────────────────

    fun getBudgetsByMonth(year: Int, month: Int): Flow<List<Budget>> =
        budgetDao.getBudgetsByMonth(year, month)

    suspend fun getBudgetByCategoryAndMonth(categoryId: Long, year: Int, month: Int): Budget? =
        budgetDao.getBudgetByCategoryAndMonth(categoryId, year, month)

    suspend fun insertBudget(budget: Budget): Long =
        budgetDao.insertBudget(budget)

    suspend fun updateBudget(budget: Budget) =
        budgetDao.updateBudget(budget)

    suspend fun deleteBudget(budget: Budget) =
        budgetDao.deleteBudget(budget)

    // ─── Debt ──────────────────────────────────────────────────────

    fun getAllDebts(): Flow<List<Debt>> = debtDao.getAllDebts()
    fun getUnsettledDebts(): Flow<List<Debt>> = debtDao.getUnsettledDebts()
    fun getAllPersonNames(): Flow<List<String>> = debtDao.getAllPersonNames()
    suspend fun insertDebt(debt: Debt): Long = debtDao.insertDebt(debt)
    suspend fun updateDebt(debt: Debt) = debtDao.updateDebt(debt)
    suspend fun deleteDebt(debt: Debt) = debtDao.deleteDebt(debt)

    // ─── Split ─────────────────────────────────────────────────────

    fun getAllSplitGroups(): Flow<List<SplitGroup>> = splitDao.getAllGroups()
    suspend fun getSplitGroupById(id: Long): SplitGroup? = splitDao.getGroupById(id)
    suspend fun insertSplitGroup(group: SplitGroup): Long = splitDao.insertGroup(group)
    suspend fun updateSplitGroup(group: SplitGroup) = splitDao.updateGroup(group)
    suspend fun deleteSplitGroup(group: SplitGroup) = splitDao.deleteGroup(group)

    fun getSplitItemsByGroup(groupId: Long): Flow<List<SplitItem>> = splitDao.getItemsByGroup(groupId)
    suspend fun insertSplitItem(item: SplitItem): Long = splitDao.insertItem(item)
    suspend fun updateSplitItem(item: SplitItem) = splitDao.updateItem(item)
    suspend fun deleteSplitItem(item: SplitItem) = splitDao.deleteItem(item)
}
