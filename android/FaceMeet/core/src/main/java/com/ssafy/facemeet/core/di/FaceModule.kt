// core/di/FaceModule.kt
package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.data.remote.repository.FaceRepositoryImpl
import com.ssafy.facemeet.core.domain.repository.FaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FaceModule {

    @Binds
    @Singleton
    abstract fun bindFaceRepository(
        faceRepositoryImpl: FaceRepositoryImpl
    ): FaceRepository
}
