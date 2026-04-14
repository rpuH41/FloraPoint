package com.liulkovich.florapoint.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints

@Database(entities = [Reference::class, UserPoints::class], version = 2)
abstract class AppDatabase: RoomDatabase() {
    abstract fun referenceDao(): ReferenceDao
    abstract fun userPointsDao(): UserPointsDao

}