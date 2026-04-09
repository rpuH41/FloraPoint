package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSpeciesByIdUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    operator fun invoke(referenceId: Int): Flow<Reference?> {
        return repository.getById(referenceId)
    }
}