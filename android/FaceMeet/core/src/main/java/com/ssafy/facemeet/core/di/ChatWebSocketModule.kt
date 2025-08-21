package com.ssafy.facemeet.core.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.ssafy.facemeet.core.data.remote.api.ChatWebSocketService
import com.ssafy.facemeet.core.data.remote.datasource.ChatWebSocketRemoteDataSource
import com.ssafy.facemeet.core.data.remote.repository.socket.ChatWebSocketRepositoryImpl
import com.ssafy.facemeet.core.data.remote.websocket.ChatWebSocketServiceImpl
import com.ssafy.facemeet.core.domain.repository.ChatWebSocketRepository
import com.ssafy.facemeet.core.domain.usecase.ConnectChatWebSocketUseCase
import com.ssafy.facemeet.core.domain.usecase.DisconnectChatWebSocketUseCase
import com.ssafy.facemeet.core.domain.usecase.LeaveChatRoomUseCase
import com.ssafy.facemeet.core.domain.usecase.MarkMessageAsReadUseCase
import com.ssafy.facemeet.core.domain.usecase.ObserveChatEventsUseCase
import com.ssafy.facemeet.core.domain.usecase.SendChatMessageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatWebSocketModule {


    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideChatWebSocketService(
        gson: Gson,
        @Named("ws")wsClient: OkHttpClient,
    ): ChatWebSocketService {
        return ChatWebSocketServiceImpl(gson,wsClient)
    }

    @Provides
    @Singleton
    fun provideChatWebSocketRemoteDataSource(
        chatWebSocketService: ChatWebSocketService
    ): ChatWebSocketRemoteDataSource {
        return ChatWebSocketRemoteDataSource(chatWebSocketService)
    }

    @Provides
    @Singleton
    fun provideChatWebSocketRepository(
        remoteDataSource: ChatWebSocketRemoteDataSource
    ): ChatWebSocketRepository {
        return ChatWebSocketRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideConnectChatWebSocketUseCase(
        repository: ChatWebSocketRepository
    ): ConnectChatWebSocketUseCase {
        return ConnectChatWebSocketUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDisconnectChatWebSocketUseCase(
        repository: ChatWebSocketRepository
    ): DisconnectChatWebSocketUseCase {
        return DisconnectChatWebSocketUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSendChatMessageUseCase(
        repository: ChatWebSocketRepository
    ): SendChatMessageUseCase {
        return SendChatMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkMessageAsReadUseCase(
        repository: ChatWebSocketRepository
    ): MarkMessageAsReadUseCase {
        return MarkMessageAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLeaveChatRoomUseCase(
        repository: ChatWebSocketRepository
    ): LeaveChatRoomUseCase {
        return LeaveChatRoomUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideObserveChatEventsUseCase(
        repository: ChatWebSocketRepository
    ): ObserveChatEventsUseCase {
        return ObserveChatEventsUseCase(repository)
    }
}
