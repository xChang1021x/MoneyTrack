package com.example.moneytrack.data.db

import androidx.room.*
import com.example.moneytrack.data.model.SplitGroup
import com.example.moneytrack.data.model.SplitItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {

    // ─── SplitGroup ────────────────────────────────────────────────

    @Query("SELECT * FROM split_group ORDER BY date DESC")
    fun getAllGroups(): Flow<List<SplitGroup>>

    @Query("SELECT * FROM split_group WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): SplitGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: SplitGroup): Long

    @Update
    suspend fun updateGroup(group: SplitGroup)

    @Delete
    suspend fun deleteGroup(group: SplitGroup)

    // ─── SplitItem ─────────────────────────────────────────────────

    @Query("SELECT * FROM split_item WHERE groupId = :groupId ORDER BY id ASC")
    fun getItemsByGroup(groupId: Long): Flow<List<SplitItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SplitItem): Long

    @Update
    suspend fun updateItem(item: SplitItem)

    @Delete
    suspend fun deleteItem(item: SplitItem)
}
