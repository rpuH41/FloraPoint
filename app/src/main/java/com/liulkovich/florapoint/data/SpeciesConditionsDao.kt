package com.liulkovich.florapoint.data

import androidx.room.Dao
import androidx.room.Query
import com.liulkovich.florapoint.domain.SpeciesConditions

@Dao
interface SpeciesConditionsDao {
    @Query("SELECT * FROM species_conditions")
    suspend fun getAll(): List<SpeciesConditions>

    @Query("SELECT * FROM species_conditions WHERE species_id = :speciesId")
    suspend fun getBySpeciesId(speciesId: Int): SpeciesConditions?
}