package com.liulkovich.florapoint.domain

class DeletePointUseCase(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(pointId:Int){
        repository.deletePoint(pointId)
    }
}