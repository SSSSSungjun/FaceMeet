package com.ssafy.facemeet.core.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.api.ChatApiService
import com.ssafy.facemeet.core.data.remote.datasource.ChatRemoteDataSource
import com.ssafy.facemeet.core.data.repository.ChatRepositoryImpl
import com.ssafy.facemeet.core.domain.repository.ChatRepository
import com.ssafy.facemeet.core.domain.usecase.GetChattingListUseCase
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesCurrentUseCase
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesLastUseCase
import com.ssafy.facemeet.core.domain.usecase.PostChattingLeaveUseCase
import com.ssafy.facemeet.core.domain.usecase.PostChattingLikeUseCase
import com.ssafy.facemeet.core.domain.usecase.PostChattingListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideChatRemoteDataSource(
        chatApiService: ChatApiService
    ): ChatRemoteDataSource {
        return ChatRemoteDataSource(chatApiService)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideChatRepository(
        chatRemoteDataSource: ChatRemoteDataSource
    ): ChatRepository {
        return ChatRepositoryImpl(chatRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideGetChattingListUseCase(
        repository: ChatRepository
    ): GetChattingListUseCase = GetChattingListUseCase(repository)

    @Provides
    @Singleton
    fun providePostChattingListUseCase(
        repository: ChatRepository
    ): PostChattingListUseCase = PostChattingListUseCase(repository)

    @Provides
    @Singleton
    fun providePostChattingLikeUseCase(
        repository: ChatRepository
    ): PostChattingLikeUseCase = PostChattingLikeUseCase(repository)

    @Provides
    @Singleton
    fun providePostChattingLeaveUseCase(
        repository: ChatRepository
    ): PostChattingLeaveUseCase = PostChattingLeaveUseCase(repository)

    @Provides
    @Singleton
    fun provideGetChattingMessagesLastUseCase(
        repository: ChatRepository
    ): GetChattingMessagesLastUseCase = GetChattingMessagesLastUseCase(repository)

//    @Provides
//    @Singleton
//    fun provideGetChattingMessagesAllUseCase(
//        repository: ChatRepository
//    ): GetChattingMessagesAllUseCase = GetChattingMessagesAllUseCase(repository)

    @Provides
    @Singleton
    fun provideGetChattingMessagesCurrentUseCase(
        repository: ChatRepository
    ): GetChattingMessagesCurrentUseCase = GetChattingMessagesCurrentUseCase(repository)
}
