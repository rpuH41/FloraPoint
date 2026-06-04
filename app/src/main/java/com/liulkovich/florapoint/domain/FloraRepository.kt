package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow

interface FloraRepository {

    fun getAllSpecies(): Flow<List<Reference>>
    fun getSpeciesByCategories(categories: Set<String>): Flow<List<Reference>>
    fun getByCategoriesAndName(categories: Set<String>, speciesName: String): Flow<List<Reference>>
    fun getSpeciesByName(speciesName: String): Flow<List<Reference>>
    fun getById(referenceId: Int): Flow<Reference?>
    suspend fun addNewPoint(point: UserPoints): Int
    suspend fun deletePoint(pointId: Int)
    suspend fun editPoint(point: UserPoints)
    suspend fun insertPoint(point: UserPoints)
    suspend fun getPointByCloudId(cloudId: String): UserPoints?
    suspend fun getAllUserPointsList(): List<UserPoints>
     fun getAllUserPoints(): Flow<List<UserPoints>>
    suspend fun updateNotification(id: Int, isEnabled: Int)
    suspend fun getNotificationEnabled(): List<Reference>
    suspend fun hasPointsForSpecies(speciesId: Int): Boolean
    suspend fun getRandomTip(): Tip?
    fun getAllOfflineRegions(): Flow<List<OfflineRegion>>
    suspend fun saveOfflineRegion(region: OfflineRegion)
    suspend fun deleteOfflineRegion(id: String)
    suspend fun countOfflineRegions(): Int
}