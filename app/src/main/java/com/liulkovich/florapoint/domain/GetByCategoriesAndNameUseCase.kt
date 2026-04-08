package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetByCategoriesAndNameUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    operator fun invoke(categories: Set<String>, speciesName: String): Flow<List<Reference>> {
        return repository.getByCategoriesAndName(categories, speciesName)
    }
}