package com.liulkovich.florapoint.domain

import javax.inject.Inject

class AddNewPointUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(point: UserPoints){
        repository.addNewPoint(point)
    }
}