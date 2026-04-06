package com.liulkovich.florapoint.domain

import javax.inject.Inject

class EditPointUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(point: UserPoints){
        repository.editPoint(point)
    }
}