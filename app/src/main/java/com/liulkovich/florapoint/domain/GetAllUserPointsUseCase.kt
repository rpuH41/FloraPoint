package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow

class GetAllUserPointsUseCase(
    private val repository: FloraRepository
) {
     operator fun invoke(): Flow<List<UserPoints>> {
       return repository.getAllUserPoints()
    }
}