package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.data.remote.api.FcmApiService
import com.ssafy.facemeet.core.data.remote.datasource.FcmRemoteDataSource
import com.ssafy.facemeet.core.data.remote.repository.FcmRepositoryImpl
import com.ssafy.facemeet.core.domain.repository.FcmRepository
import com.ssafy.facemeet.core.domain.usecase.DeleteDeviceUseCase
import com.ssafy.facemeet.core.domain.usecase.DeleteSubscriptionUseCase
import com.ssafy.facemeet.core.domain.usecase.GetDeviceTokensUseCase
import com.ssafy.facemeet.core.domain.usecase.GetSubscriptionsUseCase
import com.ssafy.facemeet.core.domain.usecase.PostSubscriptionUseCase
import com.ssafy.facemeet.core.domain.usecase.RegisterDeviceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FcmModule {

    @Provides
    @Singleton
    fun provideFcmApiService(retrofit: Retrofit): FcmApiService {
        return retrofit.create(FcmApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFcmRemoteDataSource(apiService: FcmApiService): FcmRemoteDataSource {
        return FcmRemoteDataSource(apiService)
    }

    @Provides
    @Singleton
    fun provideFcmRepository(remoteDataSource: FcmRemoteDataSource): FcmRepository {
        return FcmRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideGetDeviceTokensUseCase(repository: FcmRepository): GetDeviceTokensUseCase {
        return GetDeviceTokensUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRegisterDeviceUseCase(repository: FcmRepository): RegisterDeviceUseCase {
        return RegisterDeviceUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteDeviceUseCase(repository: FcmRepository): DeleteDeviceUseCase {
        return DeleteDeviceUseCase(repository)
    }

    @Provides
    @Singleton
    fun providePostSubscriptionUseCase(repository: FcmRepository): PostSubscriptionUseCase {
        return PostSubscriptionUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteSubscriptionUseCase(repository: FcmRepository): DeleteSubscriptionUseCase {
        return DeleteSubscriptionUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetSubscriptionsUseCase(repository: FcmRepository): GetSubscriptionsUseCase {
        return GetSubscriptionsUseCase(repository)
    }
}