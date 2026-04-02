package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow

interface FloraRepository {

    suspend fun getSpeciesByCategory(category: String): List<Reference>

    suspend fun getSpeciesById(speciesId: Int): Reference?

    suspend fun addNewPoint(point: UserPoints)

    suspend fun deletePoint(pointId: Int)

    suspend fun editPoint(point: UserPoints)

     fun getAllUserPoints(): Flow<List<UserPoints>>

}