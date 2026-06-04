package com.liulkovich.florapoint.data

import com.liulkovich.florapoint.domain.FloraRepository
import com.liulkovich.florapoint.domain.OfflineRegion
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.Tip
import com.liulkovich.florapoint.domain.UserPoints
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FloraRepositoryImpl @Inject constructor(

    private val referenceDao: ReferenceDao,
    private val userPointsDao: UserPointsDao,
    private val tipDao: TipDao,
    private val offlineRegionDao: OfflineRegionDao

): FloraRepository {

    override fun getAllSpecies(): Flow<List<Reference>> = referenceDao.getAllSpecies()

    override fun getSpeciesByCategories(categories: Set<String>): Flow<List<Reference>> = referenceDao.getByCategories(categories)

    override fun getByCategoriesAndName(categories: Set<String>, speciesName: String): Flow<List<Reference>> = referenceDao.getByCategoriesAndName(categories, speciesName)

    override fun getSpeciesByName(speciesName: String): Flow<List<Reference>> = referenceDao.getByName(speciesName)

    override fun getById(referenceId: Int): Flow<Reference?> = referenceDao.getById(referenceId)

    override suspend fun addNewPoint(point: UserPoints): Int {
        return userPointsDao.insert(point).toInt()
    }

    override suspend fun insertPoint(point: UserPoints) { userPointsDao.insert(point)}

    override suspend fun getPointByCloudId(cloudId: String): UserPoints? {
        return userPointsDao.getByCloudId(cloudId)
    }

    override suspend fun deletePoint(pointId: Int) = userPointsDao.delete(pointId)

    override suspend fun editPoint(point: UserPoints) = userPointsDao.updateUsers(point)

    override fun getAllUserPoints(): Flow<List<UserPoints>> = userPointsDao.getAll()

    override suspend fun updateNotification(id: Int, isEnabled: Int) = referenceDao.updateNotification(id, isEnabled)

    override suspend fun getNotificationEnabled(): List<Reference> = referenceDao.getNotificationEnabled()

    override suspend fun hasPointsForSpecies(speciesId: Int): Boolean = userPointsDao.countBySpeciesId(speciesId) > 0

    override suspend fun getRandomTip(): Tip? = tipDao.getRandomTip()

    override suspend fun getAllUserPointsList(): List<UserPoints> = userPointsDao.getAllList()

    override fun getAllOfflineRegions(): Flow<List<OfflineRegion>> = offlineRegionDao.getAll()

    override suspend fun saveOfflineRegion(region: OfflineRegion) = offlineRegionDao.insert(region)

    override suspend fun deleteOfflineRegion(id: String) = offlineRegionDao.delete(id)

    override suspend fun countOfflineRegions(): Int = offlineRegionDao.count()


}