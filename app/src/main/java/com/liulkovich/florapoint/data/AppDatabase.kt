package com.liulkovich.florapoint.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints

@Database(entities = [Reference::class, UserPoints::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun referenceDao(): ReferenceDao
    abstract fun userPointsDao(): UserPointsDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flora.db"
                )
                    .createFromAsset("flora.db")
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}