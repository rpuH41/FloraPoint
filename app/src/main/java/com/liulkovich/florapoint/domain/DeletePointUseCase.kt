package com.liulkovich.florapoint.domain

import javax.inject.Inject

class DeletePointUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(pointId: Int){
        repository.deletePoint(pointId)
    }
}