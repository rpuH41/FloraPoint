package com.liulkovich.florapoint.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liulkovich.florapoint.domain.OfflineRegion
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineRegionDao {

    @Query("SELECT * FROM offline_regions ORDER BY created_at DESC")
    fun getAll(): Flow<List<OfflineRegion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(region: OfflineRegion)

    @Query("DELETE FROM offline_regions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM offline_regions")
    suspend fun count(): Int
}