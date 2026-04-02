package com.liulkovich.florapoint.domain

class GetSpeciesByCategoryUseCase(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(category: String): List<Reference>{
        return repository.getSpeciesByCategory(category)
    }
}