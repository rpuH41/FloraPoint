package com.liulkovich.florapoint.domain

class AddNewPointUseCase(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(point: UserPoints){
        repository.addNewPoint(point)
    }
}