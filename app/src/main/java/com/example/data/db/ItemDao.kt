package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE isArchived = 0 ORDER BY isPinned DESC, createdAtEpochMillis DESC")
    fun getAllActiveItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isArchived = 0 AND isCompleted = 0 ORDER BY isPinned DESC, createdAtEpochMillis DESC")
    fun getInboxItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isArchived = 0 AND itemType = 'NOTE' ORDER BY isPinned DESC, updatedAtEpochMillis DESC")
    fun getAllNotes(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isArchived = 0 AND itemType = :type ORDER BY createdAtEpochMillis DESC")
    fun getItemsByType(type: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isArchived = 1 ORDER BY updatedAtEpochMillis DESC")
    fun getArchivedItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE projectId = :projectId AND isArchived = 0")
    fun getItemsForProject(projectId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): ItemEntity?

    @Query("SELECT * FROM items WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchItems(query: String): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>): List<Long>

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM items")
    suspend fun clearAll()
}
