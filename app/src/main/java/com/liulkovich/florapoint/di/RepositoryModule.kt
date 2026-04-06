package com.liulkovich.florapoint.di

import com.liulkovich.florapoint.data.FloraRepositoryImpl
import com.liulkovich.florapoint.domain.FloraRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFloraRepository(
        impl: FloraRepositoryImpl
    ): FloraRepository
}