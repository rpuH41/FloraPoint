package com.liulkovich.florapoint.data

import com.liulkovich.florapoint.domain.FloraRepository
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import kotlinx.coroutines.flow.Flow

class FloraRepositoryImpl(
    private val referenceDao: ReferenceDao,
    private val userPointsDao: UserPointsDao
): FloraRepository {
    override suspend fun getSpeciesByCategory(category: String): List<Reference> = referenceDao.getByCategory(category)

    override suspend fun getSpeciesById(speciesId: Int): Reference? = referenceDao.getById(speciesId)

    override suspend fun addNewPoint(point: UserPoints) = userPointsDao.insert(point)

    override suspend fun deletePoint(pointId: Int) = userPointsDao.delete(pointId)

    override suspend fun editPoint(point: UserPoints) = userPointsDao.updateUsers(point)

    override fun getAllUserPoints(): Flow<List<UserPoints>> = userPointsDao.getAll()


}