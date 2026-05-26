package com.liulkovich.florapoint.domain

enum class SyncState {
    LOCAL,
    PENDING_UPLOAD,
    SYNCED,
    UPLOAD_FAILED
}