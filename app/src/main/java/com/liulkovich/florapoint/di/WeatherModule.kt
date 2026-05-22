package com.liulkovich.florapoint.di

import com.liulkovich.florapoint.data.weather.WeatherService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    @Provides
    @Singleton
    fun provideWeatherService(): WeatherService {
        return WeatherService()
    }
}