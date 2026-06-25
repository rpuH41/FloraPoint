package com.liulkovich.florapoint.domain

import javax.inject.Inject

class GetSpeciesByExactNameUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(name: String): Reference? =
        repository.getSpeciesByExactName(name)
}