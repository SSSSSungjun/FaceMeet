package com.ssafy.facemeet.di

import com.ssafy.facemeet.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    @Named("isDebug")
    fun provideIsDebug(): Boolean = BuildConfig.DEBUG
}