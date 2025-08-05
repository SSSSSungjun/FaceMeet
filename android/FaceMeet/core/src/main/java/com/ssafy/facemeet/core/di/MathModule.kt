package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.data.remote.api.MatchApiService
import com.ssafy.facemeet.core.data.remote.datasource.MatchRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MatchModule {

    @Provides
    @Singleton
    fun provideMatchApi(retrofit: Retrofit): MatchApiService {
        return retrofit.create(MatchApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMatchRemoteDataSource(
        matchApi: MatchApiService
    ): MatchRemoteDataSource {
        return MatchRemoteDataSource(matchApi)
    }
}
