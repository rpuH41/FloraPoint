package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSpeciesByCategoryUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    operator fun invoke(category: String): Flow<List<Reference>> {
        return repository.getSpeciesByCategory(category)
    }
}