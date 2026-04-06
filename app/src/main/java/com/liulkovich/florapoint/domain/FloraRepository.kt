package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow

interface FloraRepository {

     fun getAllSpecies(): Flow<List<Reference>>
     fun getSpeciesByCategory(category: String): Flow<List<Reference>>

     fun getSpeciesByName(speciesName: String): Flow<List<Reference>>

    suspend fun addNewPoint(point: UserPoints)

    suspend fun deletePoint(pointId: Int)

    suspend fun editPoint(point: UserPoints)

     fun getAllUserPoints(): Flow<List<UserPoints>>

}