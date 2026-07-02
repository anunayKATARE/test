package com.lifeos.app.feature.category.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isArchived = 0 AND isHidden = 0 ORDER BY orderIndex ASC")
    fun observeVisible(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY orderIndex ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Upsert
    suspend fun upsert(entity: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE categories SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("UPDATE categories SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: String, hidden: Boolean)

    @Query("UPDATE categories SET orderIndex = :orderIndex WHERE id = :id")
    suspend fun setOrderIndex(id: String, orderIndex: Int)

    @Delete
    suspend fun delete(entity: CategoryEntity)

    @Query("DELETE FROM categories WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)

    @Query("SELECT * FROM categories WHERE profileId IS NULL")
    suspend fun getAllReal(): List<CategoryEntity>

    @Query("DELETE FROM categories WHERE profileId IS NULL")
    suspend fun deleteAllReal()

    @Query("SELECT * FROM categories")
    suspend fun getAllForBackup(): List<CategoryEntity>

    @Query("DELETE FROM categories")
    suspend fun deleteAllForRestore()
}
