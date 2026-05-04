package com.liulkovich.florapoint.data

import androidx.room.Dao
import androidx.room.Query
import com.liulkovich.florapoint.domain.Reference
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferenceDao {

    @Query("SELECT * FROM reference_table")
    fun getAllSpecies(): Flow<List<Reference>>

    @Query("SELECT * FROM reference_table WHERE category IN (:categories)")
    fun getByCategories(categories: Set<String>): Flow<List<Reference>>

    @Query("SELECT * FROM reference_table WHERE category IN (:categories) AND name LIKE :name || '%'")
    fun getByCategoriesAndName(categories: Set<String>, name: String): Flow<List<Reference>>

    @Query("SELECT * FROM reference_table WHERE category = :category AND start_month = :startMonth")
    fun getByCategoryAndStartSeason(category: String, startMonth: Int): Flow<List<Reference>>

    @Query("SELECT * FROM reference_table WHERE name LIKE :name || '%'")
    fun getByName(name: String): Flow<List<Reference>>

    @Query("SELECT * FROM reference_table WHERE id = :referenceId")
    fun getById(referenceId: Int): Flow<Reference?>

    @Query("UPDATE reference_table SET is_notif_enabled = :isEnabled WHERE id = :id")
    suspend fun updateNotification(id: Int, isEnabled: Int)

    @Query("SELECT * FROM reference_table WHERE is_notif_enabled = 1")
    suspend fun getNotificationEnabled(): List<Reference>

}