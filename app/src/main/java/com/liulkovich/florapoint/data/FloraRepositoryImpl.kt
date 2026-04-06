package com.liulkovich.florapoint.data

import com.liulkovich.florapoint.domain.FloraRepository
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FloraRepositoryImpl @Inject constructor(

    private val referenceDao: ReferenceDao,
    private val userPointsDao: UserPointsDao

): FloraRepository {

    override fun getAllSpecies(): Flow<List<Reference>> = referenceDao.getAllSpecies()

    override fun getSpeciesByCategory(category: String): Flow<List<Reference>> = referenceDao.getByCategory(category)

    override fun getSpeciesByName(speciesName: String): Flow<List<Reference>> = referenceDao.getByName(speciesName)

    override suspend fun addNewPoint(point: UserPoints) = userPointsDao.insert(point)

    override suspend fun deletePoint(pointId: Int) = userPointsDao.delete(pointId)

    override suspend fun editPoint(point: UserPoints) = userPointsDao.updateUsers(point)

    override fun getAllUserPoints(): Flow<List<UserPoints>> = userPointsDao.getAll()


}