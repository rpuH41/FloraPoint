package com.liulkovich.florapoint.di

import android.content.Context
import androidx.room.Room
import com.liulkovich.florapoint.data.AppDatabase
import com.liulkovich.florapoint.data.ReferenceDao
import com.liulkovich.florapoint.data.TipDao
import com.liulkovich.florapoint.data.UserPointsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "flora.db"
        )
            .createFromAsset("flora.db")
            //.fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideReferenceDao(db: AppDatabase): ReferenceDao = db.referenceDao()

    @Provides
    fun provideUserPointsDao(db: AppDatabase): UserPointsDao = db.userPointsDao()
    @Provides
    fun provideTipDao(db: AppDatabase): TipDao = db.tipDao()
}