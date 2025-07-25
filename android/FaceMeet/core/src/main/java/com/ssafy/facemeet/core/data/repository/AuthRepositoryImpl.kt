package com.ssafy.facemeet.core.data.repository

import com.ssafy.facemeet.core.data.remote.api.AuthApiService
import com.ssafy.facemeet.core.data.remote.dto.request.OnboardingRequest
import com.ssafy.facemeet.core.data.remote.dto.request.RefreshTokenRequest
import com.ssafy.facemeet.core.data.remote.mapper.toDomain
import com.ssafy.facemeet.core.domain.model.Auth
import com.ssafy.facemeet.core.domain.repository.AuthRepository
import com.ssafy.facemeet.core.util.constant.HttpStatus
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepositoryImpl"

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun Onboarding(request: OnboardingRequest): Result<Auth> {
        return runCatching {
            val response = authApiService.postOnboarding(request)
            if (response.isSuccessful && response.body()?.status == HttpStatus.OK) {
                val resBody = response.body()?.toDomain()
                    ?: return Result.failure(Exception("Empty response"))
                Result.success(resBody)
            } else {
                Result.failure(Exception("Onboarding failed with status: ${response.code()}"))
            }
        }.getOrElse { throwable ->
            throwable.printStackTrace()
            Result.failure(throwable)
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): Result<Auth> {
        return runCatching {
            val response = authApiService.postRefreshToken(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Refresh token failed with status ${response.code()}"))
            }
        }.getOrElse { throwable ->
            throwable.printStackTrace()
            Result.failure(throwable)
        }
    }

    override suspend fun logout(): Result<Auth> {
        return runCatching {
            val response = authApiService.postLogout()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Logout failed with status ${response.code()}"))
            }
        }.getOrElse { throwable ->
            throwable.printStackTrace()
            Result.failure(throwable)
        }
    }
}

