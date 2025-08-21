package com.ssafy.facemeet.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssafy.facemeet.core.BuildConfig
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.remote.api.FaceService
import com.ssafy.facemeet.core.data.remote.interceptor.AuthInterceptor
import com.ssafy.facemeet.core.data.remote.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager
    ): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    @Named("rest")
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        authenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }


    @Provides
    @Singleton
    fun provideRetrofit(@Named("rest") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    @Named("ws")
    fun provideWebSocket(): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .pingInterval(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
    }

    @Provides @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideFaceService(retrofit: Retrofit): FaceService {
        return retrofit.create(FaceService::class.java)
    }


}