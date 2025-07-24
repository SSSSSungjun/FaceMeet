package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.data.remote.api.AuthApiService
import com.ssafy.facemeet.core.data.repository.AuthRepositoryImpl
import com.ssafy.facemeet.core.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApiService: AuthApiService
    ): AuthRepository {
        return AuthRepositoryImpl(authApiService)
    }
}
