package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.data.remote.api.PartnerApiService
import com.ssafy.facemeet.core.data.remote.api.UserApiService
import com.ssafy.facemeet.core.data.remote.datasource.UserRemoteDataSource
import com.ssafy.facemeet.core.data.repository.UserRepositoryImpl
import com.ssafy.facemeet.core.domain.repository.UserRepository
import com.ssafy.facemeet.core.domain.usecase.DeleteUserUseCase
import com.ssafy.facemeet.core.domain.usecase.GetPartnerFaceInfoUseCase
import com.ssafy.facemeet.core.domain.usecase.GetUserInfoUseCase
import com.ssafy.facemeet.core.domain.usecase.SetUserOfflineUseCase
import com.ssafy.facemeet.core.domain.usecase.SetUserOnlineUseCase
import com.ssafy.facemeet.core.domain.usecase.UpdateUserInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserInfoApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
        userApiService: UserApiService
    ): UserRemoteDataSource {
        return UserRemoteDataSource(userApiService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userRemoteDataSource: UserRemoteDataSource
    ): UserRepository {
        return UserRepositoryImpl(userRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideSetUserOnlineUseCase(userRepository: UserRepository): SetUserOnlineUseCase {
        return SetUserOnlineUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideSetUserOfflineUseCase(userRepository: UserRepository): SetUserOfflineUseCase {
        return SetUserOfflineUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserInfoUseCase(userRepository: UserRepository): GetUserInfoUseCase {
        return GetUserInfoUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteUserUseCase(userRepository: UserRepository): DeleteUserUseCase {
        return DeleteUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserInfoUseCase(userRepository: UserRepository): UpdateUserInfoUseCase {
        return UpdateUserInfoUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun providePartnerApiService(retrofit: Retrofit): PartnerApiService {
        return retrofit.create(PartnerApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGetPartnerFaceInfoUseCase(userRepository: UserRepository): GetPartnerFaceInfoUseCase {
        return GetPartnerFaceInfoUseCase(userRepository)
    }
}
