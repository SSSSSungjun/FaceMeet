package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.data.remote.api.ReportApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportModule {

    @Provides
    @Singleton
    fun provideReportApiService(retrofit: Retrofit): ReportApiService {
        return retrofit.create(ReportApiService::class.java)
    }
}