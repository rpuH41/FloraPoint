package com.liulkovich.florapoint.domain

import javax.inject.Inject

class GetRandomTipUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(): Tip? = repository.getRandomTip()
}