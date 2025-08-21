package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.data.remote.api.BlockApiService
import com.ssafy.facemeet.core.data.remote.datasource.BlockRemoteDataSource
import com.ssafy.facemeet.core.data.remote.repository.BlockRepositoryImpl
import com.ssafy.facemeet.core.domain.repository.BlockRepository
import com.ssafy.facemeet.core.domain.usecase.DeleteBlockUserUseCase
import com.ssafy.facemeet.core.domain.usecase.GetBlockListUseCase
import com.ssafy.facemeet.core.domain.usecase.PostBlockUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BlockModule {

    @Provides
    @Singleton
    fun provideBlockApiService(retrofit: Retrofit): BlockApiService {
        return retrofit.create(BlockApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBlockRemoteDataSource(
        blockApiService: BlockApiService
    ): BlockRemoteDataSource {
        return BlockRemoteDataSource(blockApiService)
    }

    @Provides
    @Singleton
    fun provideBlockRepository(
        blockRemoteDataSource: BlockRemoteDataSource
    ): BlockRepository {
        return BlockRepositoryImpl(blockRemoteDataSource)
    }

    @Provides
    @Singleton
    fun providePostBlockUserUseCase(
        repository: BlockRepository
    ): PostBlockUserUseCase = PostBlockUserUseCase(repository)

    @Provides
    @Singleton
    fun provideGetBlockListUseCase(
        repository: BlockRepository
    ): GetBlockListUseCase = GetBlockListUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteBlockUserUseCase(
        repository: BlockRepository
    ): DeleteBlockUserUseCase = DeleteBlockUserUseCase(repository)
}