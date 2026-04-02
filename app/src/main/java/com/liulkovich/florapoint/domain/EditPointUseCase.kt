package com.liulkovich.florapoint.domain

class EditPointUseCase(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(point: UserPoints){
        repository.editPoint(point)
    }
}