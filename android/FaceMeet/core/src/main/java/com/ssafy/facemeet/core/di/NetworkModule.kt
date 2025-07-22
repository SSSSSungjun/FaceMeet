package com.ssafy.facemeet.core.di

import com.ssafy.facemeet.core.BuildConfig
import com.ssafy.facemeet.core.network.ApiService
import com.ssafy.facemeet.core.network.PersistentCookieJar
import com.ssafy.facemeet.core.network.interceptor.AuthErrorInterceptor
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

    @Provides
    @Singleton
    fun providePersistentCookieJar(): PersistentCookieJar {
        return PersistentCookieJar()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(
        @Named("isDebug") isDebug: Boolean
    ): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authErrorInterceptor: AuthErrorInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        cookieJar: PersistentCookieJar
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authErrorInterceptor)
            .addInterceptor(loggingInterceptor)
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://your-api-server.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}