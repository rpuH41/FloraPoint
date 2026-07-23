package com.liulkovich.florapoint.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.liulkovich.florapoint.domain.OfflineRegion
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.SpeciesConditions
import com.liulkovich.florapoint.domain.Tip
import com.liulkovich.florapoint.domain.UserPoints

@Database(entities = [
    Reference::class,
    UserPoints::class,
    Tip::class,
    OfflineRegion::class,
    SpeciesConditions::class
    ],
    version = 19,
    exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun referenceDao(): ReferenceDao
    abstract fun userPointsDao(): UserPointsDao
    abstract fun tipDao(): TipDao

    abstract fun offlineRegionDao(): OfflineRegionDao

    abstract fun speciesConditionsDao(): SpeciesConditionsDao

}