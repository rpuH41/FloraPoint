package com.liulkovich.florapoint.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateNotificationUseCase @Inject constructor(
    private val repository: FloraRepository
) {
    suspend operator fun invoke(id: Int, isEnabled: Int) {
         repository.updateNotification(id, isEnabled)
    }
}