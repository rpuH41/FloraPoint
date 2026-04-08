package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSpeciesByCategoryUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    operator fun invoke(categories: Set<String>): Flow<List<Reference>> {
        return repository.getSpeciesByCategories(categories)
    }
}