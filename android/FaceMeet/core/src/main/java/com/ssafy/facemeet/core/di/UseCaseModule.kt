package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.domain.repository.FaceRepository
import com.ssafy.facemeet.core.domain.usecase.AnalyzeFaceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideAnalyzeFaceUseCase(faceRepository: FaceRepository): AnalyzeFaceUseCase {
        return AnalyzeFaceUseCase(faceRepository)
    }


}
