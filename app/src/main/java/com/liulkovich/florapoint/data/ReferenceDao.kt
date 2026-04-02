package com.liulkovich.florapoint.data

import androidx.room.Dao
import androidx.room.Query
import com.liulkovich.florapoint.domain.Reference

@Dao
interface ReferenceDao {

    @Query("SELECT * FROM reference_table WHERE category = :category")
    suspend fun getByCategory(category: String): List<Reference>

    @Query("SELECT * FROM reference_table WHERE category = :category AND name = :name")
    suspend fun getByCategoryAndName(category: String, name: String): List<Reference>

    @Query("SELECT * FROM reference_table WHERE category = :category AND start_month = :startMonth")
    suspend fun getByCategoryAndStartSeason(category: String, startMonth: Int): List<Reference>

    @Query("SELECT * FROM reference_table WHERE id = :id")
    suspend fun getById(id: Int): Reference?


}