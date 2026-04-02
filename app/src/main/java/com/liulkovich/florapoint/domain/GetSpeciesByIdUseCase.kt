package com.liulkovich.florapoint.domain

class GetSpeciesByIdUseCase(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(speciesId: Int){
        repository.getSpeciesById(speciesId)
    }
}