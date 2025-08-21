// core/di/MatchTicketModule.kt
package com.ssafy.facemeet.core.di

import com.google.gson.Gson
import com.ssafy.facemeet.core.data.remote.api.MatchTicketApi
import com.ssafy.facemeet.core.data.remote.datasource.MatchTicketRemoteDataSource
import com.ssafy.facemeet.core.data.remote.datasource.MatchTicketRemoteDataSourceImpl
import com.ssafy.facemeet.core.data.remote.repository.MatchTicketRepositoryImpl
import com.ssafy.facemeet.core.domain.repository.MatchTicketRepository
import com.ssafy.facemeet.core.domain.usecase.TakeMatchTicketUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MatchTicketModule {

    @Provides @Singleton
    fun provideMatchTicketApi(retrofit: Retrofit): MatchTicketApi =
        retrofit.create(MatchTicketApi::class.java)

    @Provides @Singleton
    fun provideMatchTicketRemoteDataSource(api: MatchTicketApi): MatchTicketRemoteDataSource =
        MatchTicketRemoteDataSourceImpl(api)

    @Provides @Singleton
    fun provideMatchTicketRepository(
        remote: MatchTicketRemoteDataSource,
        gson: Gson                                             // ✅ 주입
    ): MatchTicketRepository = MatchTicketRepositoryImpl(remote, gson)

    @Provides @Singleton
    fun provideTakeMatchTicketUseCase(repo: MatchTicketRepository) =
        TakeMatchTicketUseCase(repo)
}
