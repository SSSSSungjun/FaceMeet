package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.data.remote.api.MatchApiService
import com.ssafy.facemeet.core.data.remote.datasource.MatchRemoteDataSource
import com.ssafy.facemeet.core.data.repository.MatchRepositoryImpl
import com.ssafy.facemeet.core.domain.repository.MatchRepository
import com.ssafy.facemeet.core.domain.usecase.GetMatchRemainUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MatchApiModule {

    @Provides
    @Singleton
    fun provideMatchApiService(retrofit: Retrofit): MatchApiService =
        retrofit.create(MatchApiService::class.java)

    @Provides
    @Singleton
    fun provideMatchRemoteDataSource(
        api: MatchApiService
    ): MatchRemoteDataSource = MatchRemoteDataSource(api)

    @Provides
    @Singleton
    fun provideMatchRepository(
        remote: MatchRemoteDataSource
    ): MatchRepository = MatchRepositoryImpl(remote)

    @Provides
    @Singleton
    fun provideGetMatchRemainUseCase(
        repo: MatchRepository
    ): GetMatchRemainUseCase = GetMatchRemainUseCase(repo)
}
