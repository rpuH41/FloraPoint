package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSpeciesUseCase @Inject constructor (
    private val repository: FloraRepository
) {
    operator fun invoke(): Flow<List<Reference>> {
        return repository.getAllSpecies()
    }
}