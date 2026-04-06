package com.liulkovich.florapoint.data

import androidx.room.Dao
import androidx.room.Query
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferenceDao {

    @Query("SELECT * FROM reference_table")
    fun getAllSpecies(): Flow<List<Reference>>

    @Query("SELECT * FROM reference_table WHERE category = :category")
    fun getByCategory(category: String): Flow<List<Reference>>

    @Query("SELECT * FROM reference_table WHERE category = :category AND name = :name")
    fun getByCategoryAndName(category: String, name: String): Flow<List<Reference>>

    @Query("SELECT * FROM reference_table WHERE category = :category AND start_month = :startMonth")
    fun getByCategoryAndStartSeason(category: String, startMonth: Int): Flow<List<Reference>>

    @Query("SELECT * FROM reference_table WHERE name = :name")
    fun getByName(name: String): Flow<List<Reference>>


}