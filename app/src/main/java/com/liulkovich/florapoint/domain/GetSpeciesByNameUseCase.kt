package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSpeciesByNameUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    operator fun invoke(speciesName: String): Flow<List<Reference>> {
        return repository.getSpeciesByName(speciesName)
    }
}